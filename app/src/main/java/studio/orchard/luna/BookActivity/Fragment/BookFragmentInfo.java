package studio.orchard.luna.BookActivity.Fragment;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import studio.orchard.blurview.BlurView;
import studio.orchard.luna.Component.BaseActivity;
import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.DataHolder.DataHolder;
import studio.orchard.luna.Component.SerializedClass.v0.BookShelf;
import studio.orchard.luna.R;
import studio.orchard.luna.SearchActivity.SearchActivity;

public class BookFragmentInfo extends Fragment {
    private View view;
    private BookFragmentInfoListener bookActivity;
    private TextView bookAuthor;
    private TextView bookTag ;
    private TextView bookStatus;
    private TextView bookIntroduction;
    private TextView bookCount;
    private TextView bookUpdatedTime;
    private CardView buttonReadingProgress;
    private ImageButton btnFavor;

    public interface BookFragmentInfoListener{
        void bookFragmentInfoCreated(BookFragmentInfo bookFragmentInfo);
        void switchFragment(int ID);
        boolean setFavor(int type);
        void downloadBook(int bookIndex, int chapterIndex);
        void startViewer(int bookIndex, int chapterIndex);
        void overridePendingTransition(int enterAnim, int exitAnim);
        Bitmap getActivityScreenShot();
    }

    public BookFragmentInfo() { }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        if(context instanceof BookFragmentInfoListener){
            bookActivity = (BookFragmentInfoListener)context;
        }
    }

    private int getMarginTop(){
        return getArguments() != null ? getArguments().getInt("marginTop") : 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.book_fragment_info, container, false);
        initView(getMarginTop());
        bookActivity.bookFragmentInfoCreated(this);
        return view;
    }

    private void initView(int marginTop){
        bookAuthor = view.findViewById(R.id.book_info_author);
        bookTag = view.findViewById(R.id.book_info_tag);
        bookStatus = view.findViewById(R.id.book_info_status);
        bookIntroduction = view.findViewById(R.id.book_info_introduction);
        bookCount = view.findViewById(R.id.book_info_count);
        bookUpdatedTime = view.findViewById(R.id.book_info_updated_time);
        buttonReadingProgress = view.findViewById(R.id.book_info_btn_start_reading);
        btnFavor = view.findViewById(R.id.book_info_btn_favor);
        CardView btnSwitchToContent = view.findViewById(R.id.book_info_btn_switcher);
        btnSwitchToContent.setOnClickListener(view -> bookActivity.switchFragment(1));
        CoordinatorLayout coordinatorLayout = view.findViewById(R.id.book_info_layout);
        coordinatorLayout.setPadding(10, marginTop, 10, 10);
    }

    public void loadData(final BookShelf.BookSeries bookSeries){
        final int readingProgress = bookSeries.bookReadingProgress;
        bookAuthor.setText(bookSeries.bookSeriesAuthor);
        bookAuthor.setOnClickListener(v -> {
            final Bitmap screenShot = bookActivity.getActivityScreenShot();
            final Bitmap blurredScreenShot = BlurView.process(screenShot, 17f, 0.2f, BaseActivity.getColorDrawable("#B3FFFFFF"));
            screenShot.recycle();
            DataHolder.getInstance().putData("searchActivityBackground", blurredScreenShot);
            DataHolder.getInstance().putData("searchActivityMode", Constants.SearchType.SHOW);
            DataHolder.getInstance().putData("searchActivityTitle", (bookSeries.bookSeriesAuthor + "作品集"));
            DataHolder.getInstance().putData("searchName", bookSeries.bookSeriesAuthor);
            DataHolder.getInstance().putData("searchType", Constants.SearchType.AUTHOR);
            Intent intent = new Intent();
            intent.setClass((Context)bookActivity, SearchActivity.class);
            startActivity(intent);
            bookActivity.overridePendingTransition(0, R.anim.popup_fadeout);
        });

        StringBuilder stringBuilder = new StringBuilder();
        for(String str : bookSeries.bookSeriesTag){
            stringBuilder.append(str).append(" "); }
        bookTag.setText(stringBuilder.toString());
        bookStatus.setText(bookSeries.bookSeriesStatus);
        bookIntroduction.setText(bookSeries.bookSeriesIntroduction);
        if(bookSeries.bookList.size() == 0){
            bookCount.setText("已下架");
        } else {
            bookCount.setText(readingProgress == -1 ? "开始阅读" : "继续阅读 第" + (readingProgress + 1) + "卷");
        }

        bookUpdatedTime.setText((bookSeries.bookSeriesUpdatedTime + "，共" + bookSeries.bookList.size() + "卷"));
        buttonReadingProgress.setOnClickListener(v -> {
            if(bookSeries.bookList.size() == 0) return;
            int readingProgress1 = bookSeries.bookReadingProgress == -1 ? 0 : bookSeries.bookReadingProgress;
            if(bookSeries.bookList.get(readingProgress1).isLocalized == 1){
                bookActivity.startViewer(readingProgress1,
                        bookSeries.bookList.get(readingProgress1).chapterReadingProgressNum);
            }else{
                bookActivity.downloadBook(readingProgress1,
                        bookSeries.bookList.get(readingProgress1).chapterReadingProgressNum);
            }
        });

        buttonReadingProgress.setOnLongClickListener(v -> true);
        btnFavor.setImageResource(bookSeries.index == -1 ? R.drawable.ic_book_favor : R.drawable.ic_book_favor_fill);
        btnFavor.setOnClickListener(v -> {
            if(bookSeries.index == -1){
                if(bookActivity.setFavor(Constants.MessageType.BOOK_ADD_FAVOR)){
                    btnFavor.setImageResource(R.drawable.ic_book_favor_fill);
                }
            }else{
                if(bookActivity.setFavor(Constants.MessageType.BOOK_REMOVE_FAVOR)) {
                    btnFavor.setImageResource(R.drawable.ic_book_favor);
                }
            }
        });
    }

    public void setBookCover(Bitmap bookCover){
        ImageView imageView = view.findViewById(R.id.book_info_img_cover);
        imageView.setImageBitmap(bookCover);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onDetach(){
        super.onDetach();
        bookActivity = null;
    }

}
