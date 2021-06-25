package studio.orchard.luna.ViewerActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import studio.orchard.luna.Component.Resolver.ContentResolver;
import studio.orchard.luna.Component.SerializedClass.BookItemInfo;
import studio.orchard.luna.Component.SerializedClass.v0.BookShelf;

public class Viewer {
    private Context context;
    private BookShelf.Book book;
    private ViewerListener viewerListener;
    private List<ContentResolver> resolverList;

    private TextView title;
    private TextView clock;
    private TextView content;
    private TextView pager;
    private ImageView illustration;
    private FrameLayout boundaryFrameLayout;
    private TextView boundaryBookTitle;

    private int chapterIndex;
    private int pageIndex;
    private int pageCount;
    private int illustrationPageCount;
    private float readingProgress;

    public interface ViewerListener {
        default void onChapterChanged(int newChapterIndex, int oldChapterIndex, float readingProgress) { }
        default void onGlobalLayout(TextView content){ }
        default boolean onTouch(View v, MotionEvent event) { return false; }
        default void onClick(View v) { }
    }


    @SuppressLint("ClickableViewAccessibility")
    public void setViewerListener(ViewerListener viewerListener){
        this.viewerListener = viewerListener;
        content.setOnTouchListener(viewerListener::onTouch);
        content.setOnClickListener(viewerListener::onClick);
        content.getViewTreeObserver().addOnGlobalLayoutListener(()->viewerListener.onGlobalLayout(content));
    }

    public Viewer(Context context,
                  TextView title, TextView clock, TextView content, TextView pager,
                  ImageView illustration,
                  FrameLayout boundaryFrameLayout, TextView boundaryBookTitle, ImageView boundaryBookCover,
                  BookShelf.Book book, BookItemInfo bookItem, List<ContentResolver> resolverList){
        this.context = context;
        this.title = title;
        this.clock = clock;
        this.content = content;
        this.pager = pager;
        this.illustration = illustration;
        this.boundaryFrameLayout = boundaryFrameLayout;
        this.boundaryBookTitle = boundaryBookTitle;
        this.book = book;
        this.resolverList = resolverList;
        this.readingProgress = 0.00f;
        boundaryBookCover.setImageBitmap(bookItem.bookCover);
    }

    private ContentResolver getResolver(){
        if(resolverList.get(chapterIndex) == null){
            resolverList.set(chapterIndex, new ContentResolver(context, book, chapterIndex));
        }
        return resolverList.get(chapterIndex);
    }

    public void resolve(int chapter){
        chapterIndex = chapter;
        getResolver().resolve(content);
        illustrationPageCount = getResolver().getIllustrationPageCount();
        pageCount = getResolver().getPageCount();
    }

    public void clearResolver(){
        for(ContentResolver resolver : resolverList){
            if(resolver != null){
                resolver.clear();
            }
        }
    }

    public void setPage(int page){
        pageIndex = page;
    }
    public void processPage(){
        if(pageIndex < 1){
            pageIndex = 1;
            lastPage();
        }else if(pageIndex > pageCount){
            pageIndex = pageCount;
            nextPage();
        }else{
            createPage();
        }
    }

    public void lastPage(){
        if(isFirstPage()){
            if(hasLastChapter()){
                lastChapter();
            }else{
                pageIndex--;
                showFirstBoundary();
            }
        }else{
            pageIndex--;
            createPage();
        }
    }
    public void nextPage(){
        if(isLastPage()){
            if(hasNextChapter()){
                nextChapter();
            }else{
                pageIndex++;
                showLastBoundary();
            }
        }else{
            pageIndex++;
            createPage();
        }
    }

    public void lastChapter(){
        if(viewerListener != null){
            viewerListener.onChapterChanged(chapterIndex - 1, chapterIndex, getReadingProgress());
        }
        resolve(chapterIndex - 1);
        pageIndex = pageCount;
        createPage();
    }
    public void nextChapter(){
        if(viewerListener != null){
            viewerListener.onChapterChanged(chapterIndex + 1, chapterIndex, getReadingProgress());
        }
        resolve(chapterIndex + 1);
        pageIndex = 1;
        createPage();
    }

    private void createPage(){
        restore();
        if(pageIndex <= illustrationPageCount){
            illustration.setImageBitmap(getResolver().getIllustration(pageIndex));
            title.setText((book.chapterTitle.get(chapterIndex) + " 「插画" + pageIndex + "」"));
            content.setText("");
            pager.setText((pageIndex + " / " + pageCount));
            illustration.setVisibility(View.VISIBLE);
        }else{
            title.setText(book.chapterTitle.get(chapterIndex));
            pager.setText((pageIndex + " / " + pageCount));
            content.setText(getResolver().getText(pageIndex));
            illustration.setVisibility(View.INVISIBLE);
        }
        updateReadingProgress();
    }

    private void showFirstBoundary(){
        title.setText("");
        content.setText("");
        pager.setText("");
        clock.setVisibility(View.INVISIBLE);
        illustration.setVisibility(View.INVISIBLE);
        boundaryBookTitle.setText(("《" + book.bookTitle + "》"));
        boundaryFrameLayout.setVisibility(View.VISIBLE);
    }
    private void showLastBoundary(){
        title.setText("");
        content.setText("");
        pager.setText("");
        clock.setVisibility(View.INVISIBLE);
        illustration.setVisibility(View.INVISIBLE);
        boundaryBookTitle.setText(("《" + book.bookTitle + "》\n完"));
        boundaryFrameLayout.setVisibility(View.VISIBLE);
    }

    public boolean isFirstPage(){ return pageIndex == 1; }
    public boolean isLastPage(){ return pageIndex == pageCount; }
    public boolean hasLastChapter(){ return chapterIndex > 0; }
    public boolean hasNextChapter(){ return chapterIndex < book.chapterTitle.size() - 1; }

    public int getPageCount(){ return pageCount; }
    public int getPageIndex(){ return pageIndex; }
    public int getBookIndex(){ return book.index; }
    public int getChapterIndex(){ return chapterIndex; }

    public float getReadingProgress(){ return readingProgress; }
    private void updateReadingProgress(){
        if(pageIndex == 0 || pageCount == 0){
            readingProgress = 0.00f;
        } else if(pageIndex == 1 && pageCount > 1){
            readingProgress = 0.01f;
        }else{
            readingProgress = (float)pageIndex / (float) pageCount;
        }
    }

    public void setContentPadding() {
        int leftPadding =  content.getWidth() - (int)getContentTextWidth();
        content.setPadding(leftPadding, 0, 0, 0);
    }
    public float getContentTextWidth() { return getResolver().getContentTextWidth(); }

    public void setContentTextSize(float fontSize){
        content.setTextSize(fontSize);
    }
    public void setTextColor(int color){
        title.setTextColor(color);
        clock.setTextColor(color);
        content.setTextColor(color);
        pager.setTextColor(color);
        boundaryBookTitle.setTextColor(color);
    }

    private void restore(){
        title.setVisibility(View.VISIBLE);
        content.setVisibility(View.VISIBLE);
        pager.setVisibility(View.VISIBLE);
        clock.setVisibility(View.VISIBLE);
        boundaryFrameLayout.setVisibility(View.INVISIBLE);
    }
}
