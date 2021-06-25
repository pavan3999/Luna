package studio.orchard.luna.ViewerActivity;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import studio.orchard.luna.Component.BaseActivity;
import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.DataHolder.DataHolder;
import studio.orchard.luna.Component.DataHolder.UserSettingDataHolder;
import studio.orchard.luna.Component.Resolver.ContentResolver;
import studio.orchard.luna.Component.SerializedClass.BookItemInfo;
import studio.orchard.luna.Component.SerializedClass.v0.BookShelf;
import studio.orchard.luna.R;
import studio.orchard.luna.ViewerActivity.Adapter.ViewerActivityViewPagerAdapter;
import studio.orchard.luna.ViewerActivity.Listener.ViewerActivityOnPageChangeListener;

public class ViewerActivity extends BaseActivity {

    public ViewPager viewPager;
    public Viewer currentPageViewer, nextPageViewer, lastPageViewer;
    public enum BackgroundColor{WHITE, GREEN, BLUE, YELLOW, GREY, BLACK}

    private ViewerPopupWindow viewerPopupWindow;
    private ConstraintLayout background;
    private BackgroundColor backgroundColor;
    private Handler bookActivityHandler;

    private float fontSize;
    private float clickPosX, clickPosY;
    private int contentWidth, contentHeight;
    private float readingProgress;
    private int bookIndex, chapterIndex;
    public boolean isLoaded;

    public ViewerActivity(){ }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //DataHolder.getInstance().putData("chapterIndex", chapterIndex);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarNavigationBar(
                Constants.ActivityMode.FULLSCREEN,
                Constants.ActivityMode.LIGHT_STATUS_BAR,
                Constants.ActivityMode.LIGHT_NAVIGATION_BAR);
        setContentView(R.layout.viewer_activity);

        initView();
        initPopupWindow();
    }



    @SuppressLint("InflateParams")
    private void initView(){
        BookItemInfo bookItem = (BookItemInfo) DataHolder.getInstance().getData("item");
        bookIndex = (int)DataHolder.getInstance().getData("bookIndex");
        chapterIndex = (int)DataHolder.getInstance().getData("chapterIndex");
        bookActivityHandler = (Handler)DataHolder.getInstance().getData("bookActivityHandler");
        BookShelf.Book book = (BookShelf.Book) DataHolder.getInstance().getData("book");
        readingProgress = book.chapterReadingProgress.get(chapterIndex);

        background = findViewById(R.id.viewer_background);
        viewPager = findViewById(R.id.viewer_viewpager);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(viewPager.getLayoutParams());
        layoutParams.setMargins(0, getStatusBarHeight(),0,0);
        viewPager.setLayoutParams(layoutParams);
        View lastView = getLayoutInflater().inflate(R.layout.viewer_activity_view, null);
        View currentView = getLayoutInflater().inflate(R.layout.viewer_activity_view, null);
        View nextView = getLayoutInflater().inflate(R.layout.viewer_activity_view, null);
        List<View> viewList = Arrays.asList(lastView, currentView, nextView);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(new ViewerActivityViewPagerAdapter(viewList));
        viewPager.addOnPageChangeListener(new ViewerActivityOnPageChangeListener(this));
        viewPager.setCurrentItem(1, false);
        List<ContentResolver> resolverList = new ArrayList<>();
        for(int i = 0; i < book.chapterTitle.size(); i++){ resolverList.add(null); }
        lastPageViewer = new Viewer(this,
                lastView.findViewById(R.id.viewer_viewpager_title),
                lastView.findViewById(R.id.viewer_viewpager_clock),
                lastView.findViewById(R.id.viewer_viewpager_content),
                lastView.findViewById(R.id.viewer_viewpager_pager),
                lastView.findViewById(R.id.viewer_viewpager_illustration_img),
                lastView.findViewById(R.id.viewer_viewpager_boundary_framelayout),
                lastView.findViewById(R.id.viewer_viewpager_boundary_title),
                lastView.findViewById(R.id.viewer_viewpager_boundary_cover),
                book, bookItem, resolverList);
        currentPageViewer = new Viewer(this,
                currentView.findViewById(R.id.viewer_viewpager_title),
                currentView.findViewById(R.id.viewer_viewpager_clock),
                currentView.findViewById(R.id.viewer_viewpager_content),
                currentView.findViewById(R.id.viewer_viewpager_pager),
                currentView.findViewById(R.id.viewer_viewpager_illustration_img),
                currentView.findViewById(R.id.viewer_viewpager_boundary_framelayout),
                currentView.findViewById(R.id.viewer_viewpager_boundary_title),
                currentView.findViewById(R.id.viewer_viewpager_boundary_cover),
                book, bookItem, resolverList);
        nextPageViewer = new Viewer(this,
                nextView.findViewById(R.id.viewer_viewpager_title),
                nextView.findViewById(R.id.viewer_viewpager_clock),
                nextView.findViewById(R.id.viewer_viewpager_content),
                nextView.findViewById(R.id.viewer_viewpager_pager),
                nextView.findViewById(R.id.viewer_viewpager_illustration_img),
                nextView.findViewById(R.id.viewer_viewpager_boundary_framelayout),
                nextView.findViewById(R.id.viewer_viewpager_boundary_title),
                nextView.findViewById(R.id.viewer_viewpager_boundary_cover),
                book, bookItem, resolverList);

        currentPageViewer.setViewerListener(new Viewer.ViewerListener() {
            @Override
            public void onGlobalLayout(TextView content) {
                if(isContentSizeChanged(content)) {
                    onContentSizeChanged();
                }
            }
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    clickPosX = event.getX();
                    clickPosY = event.getY();
                }
                return false;
            }
            @Override
            public void onClick(View v) {
                int height = v.getHeight();
                int width = v.getWidth();
                if(clickPosY >= 300 && clickPosY <= height - 300){
                    if(clickPosX >= 30 && clickPosX < (float)width / 8 * 3){
                        viewPager.setCurrentItem(0, true);
                    } else if(clickPosX >= (float)width / 8 * 3 && clickPosX < (float)width / 8 * 5){
                        viewerPopupWindow.show();
                    } else if(clickPosX >= (float)width / 8 * 5 && clickPosX <= width - 30){
                        viewPager.setCurrentItem(2, true);
                    }
                }else{
                    if(clickPosX >= 30 && clickPosX < (float)width / 2){
                        viewPager.setCurrentItem(0, true);
                    } else if(clickPosX >= (float)width / 2 && clickPosX <= width - 30){
                        viewPager.setCurrentItem(2, true);
                    }
                }
            }
            @Override
            public void onChapterChanged(int newChapterIndex, int oldChapterIndex, float readingProgress) {
                chapterIndex = newChapterIndex;
                saveReadingProgress(bookIndex, oldChapterIndex, readingProgress);
            }
        });
        setFontSize(UserSettingDataHolder.getInstance().getUserSetting().fontSize);
        setBackgroundColor(BackgroundColor.valueOf(UserSettingDataHolder.getInstance().getUserSetting().background));
        setNavigationBarColor(backgroundColor);
    }


    private boolean isContentSizeChanged(TextView content){
        if(content.getHeight() != contentHeight || content.getWidth() != contentWidth){
            contentHeight = content.getHeight();
            contentWidth = content.getWidth();
            isLoaded = false;
            return true;
        }
        return false;
    }

    private void onContentSizeChanged(){
        lastPageViewer.clearResolver();
        currentPageViewer.clearResolver();
        nextPageViewer.clearResolver();
        readingProgress = currentPageViewer.getReadingProgress() == 0.00f ? readingProgress : currentPageViewer.getReadingProgress();
        createPage(readingProgress);
        isLoaded = true;
    }


    private void createPage(float readingProgress){
        lastPageViewer.resolve(chapterIndex);
        currentPageViewer.resolve(chapterIndex);
        nextPageViewer.resolve(chapterIndex);
        int pageNum = Math.round(readingProgress * currentPageViewer.getPageCount());
        if(pageNum < 1) pageNum = 1;
        lastPageViewer.setPage(pageNum - 1);
        currentPageViewer.setPage(pageNum);
        nextPageViewer.setPage(pageNum + 1);
        lastPageViewer.processPage();
        currentPageViewer.processPage();
        nextPageViewer.processPage();
    }

    private void initPopupWindow(){
        viewerPopupWindow = new ViewerPopupWindow(new ViewerPopupWindow.ViewerPopUpWindowListener() {
            private SeekBar progressSeekBar;
            private TextView progressStart;
            private TextView progressEnd;
            @Override
            public Context getContext() { return ViewerActivity.this; }

            @Override
            public void onCreate(){ setNavigationBarColor(Color.rgb(80, 80, 80)); }

            @Override
            public void onDismiss(){ setNavigationBarColor(backgroundColor); }

            @SuppressLint("SetTextI18n")
            @Override
            public void setProgressSeekBar(SeekBar progressSeekBar, TextView start, TextView end) {
                this.progressSeekBar = progressSeekBar;
                progressStart = start;
                progressEnd = end;
                progressSeekBar.setMax(currentPageViewer.getPageCount() - 1);
                progressSeekBar.setProgress(currentPageViewer.getPageIndex() - 1);
                start.setText(Float.valueOf(currentPageViewer.getReadingProgress() * 100f).intValue() + "%");
                end.setText(String.valueOf(currentPageViewer.getPageCount()));
            }

            @Override
            public void setFontSizeSeekBar(SeekBar fontSizeSeekBar){
                fontSizeSeekBar.setMax(20);
                fontSizeSeekBar.setProgress((int)ViewerActivity.this.fontSize - 10);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(!fromUser) return;
                lastPageViewer.resolve(chapterIndex);
                currentPageViewer.resolve(chapterIndex);
                nextPageViewer.resolve(chapterIndex);
                lastPageViewer.setPage(progress);
                currentPageViewer.setPage(progress + 1);
                nextPageViewer.setPage(progress + 2);
                lastPageViewer.processPage();
                currentPageViewer.processPage();
                nextPageViewer.processPage();
                progressStart.setText(Float.valueOf(currentPageViewer.getReadingProgress() * 100f).intValue() + "%");
            }

            @Override
            public void onFontSizeChanged(SeekBar seekBar, int progress, boolean fromUser){
                if(!fromUser) return;
                //save reading progress
                setFontSize((float)progress + 10);
                onContentSizeChanged();
                if (progressSeekBar != null){
                    setProgressSeekBar(progressSeekBar, progressStart, progressEnd);
                }
                saveUserSetting();
            }

            @Override
            public void onBackgroundColorButtonClicked(BackgroundColor color) {
                setBackgroundColor(color);
                saveUserSetting();
            }
        });
    }


    private void setFontSize(float fontSize){
        this.fontSize = fontSize;
        lastPageViewer.setContentTextSize(fontSize);
        currentPageViewer.setContentTextSize(fontSize);
        nextPageViewer.setContentTextSize(fontSize);
    }

    private void setNavigationBarColor(int color){ getWindow().setNavigationBarColor(color); }

    private void setNavigationBarColor(BackgroundColor color){
        switch (color){
            case WHITE:
                getWindow().setNavigationBarColor(getResources().getColor(R.color.color_viewer_background_white, getTheme()));
                break;
            case GREEN:
                getWindow().setNavigationBarColor(getResources().getColor(R.color.color_viewer_background_green, getTheme()));
                break;
            case BLUE:
                getWindow().setNavigationBarColor(getResources().getColor(R.color.color_viewer_background_blue, getTheme()));
                break;
            case YELLOW:
                getWindow().setNavigationBarColor(getResources().getColor(R.color.color_viewer_background_yellow, getTheme()));
                break;
            case GREY:
                getWindow().setNavigationBarColor(getResources().getColor(R.color.color_viewer_background_grey, getTheme()));
                break;
            case BLACK:
                getWindow().setNavigationBarColor(getResources().getColor(R.color.color_viewer_background_black, getTheme()));
                break;
        }
    }

    private void setBackgroundColor(BackgroundColor color){
        backgroundColor = color;
        switch (color){
            case WHITE:
                background.setBackgroundResource(R.color.color_viewer_background_white);
                lastPageViewer.setTextColor(getResources().getColor(R.color.color_text_black, null));
                currentPageViewer.setTextColor(getResources().getColor(R.color.color_text_black, null));
                nextPageViewer.setTextColor(getResources().getColor(R.color.color_text_black, null));
                break;
            case GREEN:
                background.setBackgroundResource(R.color.color_viewer_background_green);
                lastPageViewer.setTextColor(getResources().getColor(R.color.color_text_black, null));
                currentPageViewer.setTextColor(getResources().getColor(R.color.color_text_black, null));
                nextPageViewer.setTextColor(getResources().getColor(R.color.color_text_black, null));
                break;
            case BLUE:
                background.setBackgroundResource(R.color.color_viewer_background_blue);
                lastPageViewer.setTextColor(getResources().getColor(R.color.color_text_black, null));
                currentPageViewer.setTextColor(getResources().getColor(R.color.color_text_black, null));
                nextPageViewer.setTextColor(getResources().getColor(R.color.color_text_black, null));
                break;
            case YELLOW:
                background.setBackgroundResource(R.color.color_viewer_background_yellow);
                lastPageViewer.setTextColor(getResources().getColor(R.color.color_text_black, null));
                currentPageViewer.setTextColor(getResources().getColor(R.color.color_text_black, null));
                nextPageViewer.setTextColor(getResources().getColor(R.color.color_text_black, null));
                break;
            case GREY:
                background.setBackgroundResource(R.color.color_viewer_background_grey);
                lastPageViewer.setTextColor(getResources().getColor(R.color.color_text_white, null));
                currentPageViewer.setTextColor(getResources().getColor(R.color.color_text_white, null));
                nextPageViewer.setTextColor(getResources().getColor(R.color.color_text_white, null));
                break;
            case BLACK:
                background.setBackgroundResource(R.color.color_viewer_background_black);
                lastPageViewer.setTextColor(getResources().getColor(R.color.color_text_grey, null));
                currentPageViewer.setTextColor(getResources().getColor(R.color.color_text_grey, null));
                nextPageViewer.setTextColor(getResources().getColor(R.color.color_text_grey, null));
                break;
        }
    }

    public void saveReadingProgress(int bookIndex, int chapterIndex, float readingProgress){
        Bundle bundle = new Bundle();
        bundle.putInt("bookIndex", bookIndex);
        bundle.putInt("chapterIndex", chapterIndex);
        bundle.putFloat("progress", readingProgress);
        Message msg = new Message();
        msg.what = Constants.MessageType.BOOK_PROGRESS;
        msg.obj = bundle;
        bookActivityHandler.sendMessage(msg);
    }

    private void saveUserSetting(){
        UserSettingDataHolder.getInstance().getUserSetting().background = backgroundColor.name();
        UserSettingDataHolder.getInstance().getUserSetting().fontSize = fontSize;
        UserSettingDataHolder.getInstance().saveUserSettingToFile(this);
    }

    @Override
    public void onBackPressed() {
        saveReadingProgress(currentPageViewer.getBookIndex(), currentPageViewer.getChapterIndex(), currentPageViewer.getReadingProgress());
        super.onBackPressed();
    }

    @Override
    public void onPause(){
        saveReadingProgress(currentPageViewer.getBookIndex(), currentPageViewer.getChapterIndex(), currentPageViewer.getReadingProgress());
        super.onPause();
    }

}
