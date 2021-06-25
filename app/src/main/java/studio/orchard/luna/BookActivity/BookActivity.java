package studio.orchard.luna.BookActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Message;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import studio.orchard.blurview.BlurView;
import studio.orchard.luna.BookActivity.Adapter.BookViewPagerAdapter;
import studio.orchard.luna.BookActivity.Fragment.BookFragmentContent;
import studio.orchard.luna.BookActivity.Fragment.BookFragmentInfo;
import studio.orchard.luna.Component.BaseActivity;
import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.DataHolder.BookShelfDataHolder;
import studio.orchard.luna.Component.DataHolder.DataHolder;
import studio.orchard.luna.Component.Resolver.BookCoverResolver;
import studio.orchard.luna.Component.Resolver.BookDownloader;
import studio.orchard.luna.Component.Resolver.BookInfoResolver;
import studio.orchard.luna.Component.SerializedClass.BookItemInfo;
import studio.orchard.luna.Component.SerializedClass.v0.BookShelf;
import studio.orchard.luna.R;
import studio.orchard.luna.ViewerActivity.ViewerActivity;


public class BookActivity extends BaseActivity
        implements BookFragmentContent.BookFragmentContentListener, BookFragmentInfo.BookFragmentInfoListener {

    private int fragmentShowingID;
    private BlurView blurView;
    private View divider;
    private ViewPager viewPager;
    private BookFragmentInfo bookFragmentInfo;
    private BookFragmentContent bookFragmentContent;
    private boolean isBookFragmentInfoCreated;
    private boolean isBookFragmentContentCreated;

    private PopupWindow popupWindow;

    private ImageView background;
    private Bitmap backgroundBlurredCover;
    private Bitmap contentBlurredCover;

    private Handler bookActivityHandler;
    private Handler mainBookShelfHandler;
    private BookInfoResolver bookInfoResolver;
    private BookDownloader bookDownloader;

    private BookShelf.BookSeries bookSeries;
    private BookItemInfo bookItemInfo;

    public BookActivity(){

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarNavigationBar(
                Constants.ActivityMode.NORMAL,
                Constants.ActivityMode.LIGHT_STATUS_BAR,
                Constants.ActivityMode.LIGHT_NAVIGATION_BAR);

        setContentView(R.layout.book_activity);
        initData();
        initView();
        initPopupWindow();
    }


    private void initView(){
        fragmentShowingID = 0;
        isBookFragmentInfoCreated = false;
        isBookFragmentContentCreated = false;

        setTitle("");
        AppBarLayout appbar = findViewById(R.id.book_appbar);
        Toolbar toolbar = findViewById(R.id.book_toolbar);
        divider = findViewById(R.id.book_divider);
        blurView = findViewById(R.id.book_blurview);
        blurView.setTarget(findViewById(R.id.book_targetview))
                .setBinding(appbar)
                .setMask(getColorDrawable("#80FFFFFF"))
                .setOpacity(0.00f)
                .enable();
        divider.setAlpha(0.00f);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }
        background = findViewById(R.id.book_background);
        TextView title = findViewById(R.id.book_title);
        title.setTextColor(getResources().getColor(R.color.color_text_black, getTheme()));
        title.setText(bookItemInfo.bookName);
        viewPager = findViewById(R.id.book_viewpager);

        Bundle bundle = new Bundle();
        bundle.putInt("marginTop", getAppBarHeight());
        BookFragmentInfo bookFragmentInfo = new BookFragmentInfo();
        bookFragmentInfo.setArguments(bundle);
        BookFragmentContent bookFragmentContent = new BookFragmentContent();
        bookFragmentContent.setArguments(bundle);

        List<Fragment> fragmentList = new ArrayList<>(
                Arrays.asList(
                        bookFragmentInfo,
                        bookFragmentContent));
        BookViewPagerAdapter bookPagerAdapter = new BookViewPagerAdapter(
                getSupportFragmentManager(),
                fragmentList.size(),
                fragmentList);

        viewPager.setAdapter(bookPagerAdapter);
        viewPager.setOffscreenPageLimit(fragmentList.size());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(bookFragmentContent.getAppbarAlpha() > 0.00f){
                    if(position == 0){
                        blurView.setAlpha(positionOffset * bookFragmentContent.getAppbarAlpha());
                        divider.setAlpha(positionOffset * bookFragmentContent.getAppbarAlpha());
                    }else{
                        blurView.setAlpha((1 - positionOffset) * bookFragmentContent.getAppbarAlpha());
                        divider.setAlpha((1 - positionOffset) * bookFragmentContent.getAppbarAlpha());
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                fragmentShowingID = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setCurrentItem(0);
    }

    @SuppressLint("InflateParams")
    private void initPopupWindow(){
        View popupWindowView = getLayoutInflater().inflate(R.layout.book_popup_content, null);
        popupWindow = new PopupWindow(popupWindowView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setClippingEnabled(false);
        popupWindow.setAnimationStyle(R.style.AnimationFade);
    }
    /*
    @Override
    public void popupWindow(final BookShelf.BookSeries bookSeries, final int index){
        if(popupWindow != null && !popupWindow.isShowing()){
            final Bitmap screenShot = getActivityScreenShot();
            final Bitmap blurredScreenShot = BlurView.process(screenShot, 6f, 0.2f);
            ImageView backgroundCover = popupWindowView.findViewById(R.id.book_popup_img_background_cover);
            backgroundCover.setImageBitmap(blurredScreenShot);

            Button closeButton = popupWindowView.findViewById(R.id.book_popup_btn_close);
            closeButton.setOnClickListener(view -> {
                popupWindow.dismiss();
                screenShot.recycle();
                blurredScreenShot.recycle();

            });
            closeButton.setOnLongClickListener(view -> true);
            Button downloadButton = popupWindowView.findViewById(R.id.book_popup_btn_download_this);
            downloadButton.setOnClickListener(view -> bookDownloader.downloadBook(index, 3));

            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(BookActivity.this).inflate(R.layout.book_activity, null);
            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            screenShot.recycle();
        }
    }
     */

    @Override
    public void bookFragmentInfoCreated(BookFragmentInfo bookFragmentInfo){
        this.bookFragmentInfo = bookFragmentInfo;
        isBookFragmentInfoCreated = true;
        //if(bookItemInfo != null)
            bookFragmentInfo.setBookCover(bookItemInfo.bookCover);
        loadData();
    }

    @Override
    public void bookFragmentContentCreated(BookFragmentContent bookFragmentContent){
        this.bookFragmentContent = bookFragmentContent;
        isBookFragmentContentCreated = true;
        loadData();
    }


    private void initData(){
        bookActivityHandler = new BookActivityHandler(this);
        mainBookShelfHandler = (Handler)DataHolder.getInstance().getData("mainBookShelfHandler");
        bookItemInfo = (BookItemInfo)DataHolder.getInstance().getData("item");
        bookInfoResolver = new BookInfoResolver(bookItemInfo);
        bookInfoResolver.setBookInfoResolverListener(new BookInfoResolver.BookInfoResolverListener() {
            @Override
            public void onExceptionThrown(Exception e) { }

            @Override
            public void onBookInfoResolveFinished(BookShelf.BookSeries bookSeries) {
                setBookInfo(bookSeries);
            }
        });
    }

    private void loadData(){
        if(isBookFragmentInfoCreated && isBookFragmentContentCreated) {
            bookInfoResolver.getBookInfo();
            if(bookItemInfo.isBookCoverResolved) {
                setBookCover(bookItemInfo.bookCover);
            }else{
                new Thread(new BookCoverResolver(bookItemInfo.bookCoverAddress,
                        bookActivityHandler, 0)).start();
            }
        }
    }

    public static final class BookActivityHandler extends Handler{
        BookActivity bookActivity;
        private BookActivityHandler(BookActivity bookActivity){
            this.bookActivity = bookActivity;
        }
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case Constants.MessageType.BOOK_COVER:
                    //加载书籍封面
                    bookActivity.setBookCover((Bitmap)msg.obj);
                    break;

                case Constants.MessageType.BOOK_PROGRESS:
                    //阅读完毕，更新进度
                    Bundle bundle = (Bundle)msg.obj;
                    bookActivity.updateReadingProgress(bundle.getInt("bookIndex"),
                            bundle.getInt("chapterIndex"), bundle.getFloat("progress"));
                    bundle.clear();
                    break;
            }
        }
    }

    private void setBookInfo(final BookShelf.BookSeries bookSeries){
        this.bookSeries = bookSeries;
        bookFragmentInfo.loadData(bookSeries);
        bookFragmentContent.loadData(bookSeries, contentBlurredCover);
        bookDownloader = new BookDownloader(BookActivity.this, bookSeries);
        bookDownloader.setBookDownloaderListener(new BookDownloader.BookDownloaderListener() {
            @Override
            public void onExceptionThrown(Exception e) {
                e.printStackTrace();
                Toast.makeText(BookActivity.this, "下载失败，任务中断", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBookDownloadFinished(int bookIndex, int chapterOpenIndex) {
                bookFragmentContent.loadData(bookSeries, contentBlurredCover);
                popupWindow.dismiss();
                startViewer(bookIndex, chapterOpenIndex);
                BookShelfDataHolder.getInstance().saveBookShelfToFile(BookActivity.this);
            }
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setBookCover(final Bitmap bookCover){
        bookItemInfo.bookCover = bookCover;
        bookItemInfo.isBookCoverResolved = true;
        backgroundBlurredCover = BlurView.process(bookCover, 6f, 135, 180, getDrawable(R.drawable.ic_book_activity_mask));
        //backgroundBlurredCover = Blur.getBlurBitmap(bookCover, 6f, 135, 180, 1, this);
        contentBlurredCover = BlurView.process(bookCover, 4f, 270, 360, null);
        //contentBlurredCover = Blur.getBlurBitmap(bookCover, 5f,  270, 360, 1, this);
        background.setImageBitmap(backgroundBlurredCover);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.image_fadein);
        background.startAnimation(fadeInAnimation);
        bookFragmentInfo.setBookCover(bookCover);
        bookFragmentContent.setBookCover(contentBlurredCover);
    }

    @Override
    public void downloadBook(int bookIndex, int chapterOpenIndex){
        if(bookDownloader.isDownloading()){
            Toast.makeText(this, "已经有下载任务了哦", Toast.LENGTH_SHORT).show();
        }else{
            bookDownloader.downloadBook(bookIndex, chapterOpenIndex);
        }
    }

    private void updateReadingProgress(int bookIndex, int chapterIndex, float progress){
        bookSeries.bookReadingProgress = bookIndex;
        bookSeries.bookList.get(bookIndex).chapterReadingProgressNum = chapterIndex;
        bookSeries.bookList.get(bookIndex).chapterReadingProgress.set(chapterIndex, progress);
        bookFragmentInfo.loadData(bookSeries);
        bookFragmentContent.loadData(bookSeries, contentBlurredCover);
        BookShelfDataHolder.getInstance().saveBookShelfToFile(this);
    }

    @Override
    public boolean setFavor(int type){
        if(!bookItemInfo.isBookCoverResolved || bookSeries == null || bookSeries.bookSeriesName == null){
            return false;
        }
        if(bookSeries.bookSeriesCover == null){
            try{
                ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
                bookItemInfo.bookCover.compress(Bitmap.CompressFormat.PNG, 100, bAOS);
                bookSeries.bookSeriesCover = bAOS.toByteArray();
                bAOS.close();
            }
            catch (Exception ignored){
            }
        }
        Message msg = new Message();
        msg.what = type;
        msg.obj = bookSeries;
        mainBookShelfHandler.sendMessage(msg);
        return true;
    }

    @Override
    public void startViewer(int bookIndex, int chapterIndex){
        DataHolder.getInstance().putData("book", bookSeries.bookList.get(bookIndex));
        DataHolder.getInstance().putData("item", bookItemInfo);
        DataHolder.getInstance().putData("bookIndex", bookIndex);
        DataHolder.getInstance().putData("chapterIndex", chapterIndex);
        DataHolder.getInstance().putData("bookActivityHandler", bookActivityHandler);
        Intent intent = new Intent();
        intent.setClass(this, ViewerActivity.class);
        startActivity(intent);
        //保存阅读记录
        int index = BookShelfDataHolder.getInstance().getPositionOnBookShelf(bookSeries.bookSeriesName);
        if(index != -1){
            BookShelfDataHolder.getInstance().getBookShelf().readingProgress = index;
        }
        BookShelfDataHolder.getInstance().saveBookShelfToFile(this);
    }

    @Override
    public void switchFragment(int ID){
        fragmentShowingID = ID;
        viewPager.setCurrentItem(ID);
    }

    @Override
    public int getFragmentShowingID(){
        return fragmentShowingID;
    }

    @Override
    public BlurView getBlurView() {
        return blurView;
    }

    @Override
    public View getDivider() {
        return divider;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if(fragmentShowingID == 0){
                super.onBackPressed();
            }else{
                switchFragment(0);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(fragmentShowingID != 0){
            switchFragment(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy(){
        if(backgroundBlurredCover != null && !backgroundBlurredCover.isRecycled()){
            backgroundBlurredCover.recycle();
        }
        if(contentBlurredCover != null && !contentBlurredCover.isRecycled()){
            contentBlurredCover.recycle();
        }
        super.onDestroy();
    }

}
