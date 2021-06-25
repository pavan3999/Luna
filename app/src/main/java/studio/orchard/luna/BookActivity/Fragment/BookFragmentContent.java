package studio.orchard.luna.BookActivity.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import studio.orchard.blurview.BlurView;
import studio.orchard.luna.BookActivity.Adapter.BookContentRecyclerViewAdapter;
import studio.orchard.luna.Component.SerializedClass.v0.BookShelf;
import studio.orchard.luna.R;

public class BookFragmentContent extends Fragment {
    private View view;
    private List<ItemAdapter> itemList;
    private BookContentRecyclerViewAdapter recyclerViewAdapter;
    private BookShelf.BookSeries bookSeries;
    private BookFragmentContentListener bookActivity;
    private Bitmap blurredCover;
    private float appbarAlpha;

    public static class ItemAdapter{
        public int itemType;
        public int isLocalized;
        private int bookIndex; //List中 第j个标题属于第几卷书
        private int chapterIndex; //List中 第j个标题属于这本书的第几章
        public int chapterReadingProgress;
        public String itemTitle;
        public Bitmap bookCover;

        private ItemAdapter(int itemType, int isLocalized, int bookIndex, int chapterIndex,
                            int chapterReadingProgress, String itemTitle, Bitmap bookCover){
            this.itemType = itemType;
            this.isLocalized = isLocalized;
            this.bookIndex = bookIndex;
            this.chapterIndex = chapterIndex;
            this.chapterReadingProgress = chapterReadingProgress;
            this.itemTitle = itemTitle;
            this.bookCover = bookCover;
        }

        private ItemAdapter(int itemType, int isLocalized, int bookIndex, int chapterIndex,
                            int chapterReadingProgress, String itemTitle){
            this.itemType = itemType;
            this.isLocalized = isLocalized;
            this.bookIndex = bookIndex;
            this.chapterIndex = chapterIndex;
            this.chapterReadingProgress = chapterReadingProgress;
            this.itemTitle = itemTitle;
        }
    }

    public BookFragmentContent() { }

    public interface BookFragmentContentListener{
        void bookFragmentContentCreated(BookFragmentContent bookFragmentContent);
        //void popupWindow(BookShelf.BookSeries bookSeries, int index)
        void downloadBook(int bookIndex, int chapterIndex);
        void startViewer(int bookIndex, int chapterIndex);
        int getFragmentShowingID();
        BlurView getBlurView();
        View getDivider();
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        if(context instanceof BookFragmentContentListener){
            bookActivity = (BookFragmentContentListener)context;
        }
    }

    private int getMarginTop(){
        return getArguments() != null ? getArguments().getInt("marginTop") : 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.book_fragment_content, container, false);
        itemList = new ArrayList<>();
        initView(getMarginTop());
        bookActivity.bookFragmentContentCreated(this);
        return view;
    }

    private void initView(int marginTop){
        RecyclerView recyclerView = view.findViewById(R.id.book_content_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerViewAdapter = new BookContentRecyclerViewAdapter(itemList, marginTop);
        recyclerViewAdapter.setOnItemClickListener(new BookContentRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onDownloadAllClick(View view){
                Toast.makeText(getContext(), "全部缓存", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartReadingClick(View view){
                Toast.makeText(getContext(), "开始阅读", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(View view, int position) {
                ItemAdapter item = itemList.get(position);
                if (item.isLocalized == 1) {
                    //开始阅读
                    if (item.itemType == 1) {
                        bookActivity.startViewer(item.bookIndex, item.chapterIndex); }
                    if (item.itemType == 2) {
                        bookActivity.startViewer(item.bookIndex, item.chapterIndex); }
                } else {
                    //开始下载
                    if (item.itemType == 1) {
                        bookActivity.downloadBook(item.bookIndex , item.chapterIndex);}
                    if (item.itemType == 2) {
                        bookActivity.downloadBook(item.bookIndex , item.chapterIndex);
                    }
                }
            }
        });
        recyclerViewAdapter.setOnItemLongClickListener((view, position) -> {
            //ItemAdapter item = itemList.get(position);
            //bookActivity.popupWindow(bookSeries, item.bookIndex);
        });
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                appbarAlpha = Math.min((float) recyclerView.computeVerticalScrollOffset() / marginTop * 2, 1.00f);
                if(bookActivity.getFragmentShowingID() == 1){
                    bookActivity.getBlurView().setAlpha(appbarAlpha);
                    bookActivity.getDivider().setAlpha(appbarAlpha);
                }
            }
        });
    }

    public void loadData(BookShelf.BookSeries bookSeries, Bitmap blurredCover){
        this.bookSeries = bookSeries;
        this.blurredCover = blurredCover;
        initItemList();
    }

    private void initItemList(){
        itemList.clear();
        ItemAdapter item;
        itemList.add(new ItemAdapter(0, 0, 0, 0,
                0, null, null));
        for (int i = 0; i < bookSeries.bookList.size(); i++) {
            BookShelf.Book book = bookSeries.bookList.get(i);
            item = new ItemAdapter(1, book.isLocalized, i, book.chapterReadingProgressNum,
                    0, book.bookTitle, blurredCover);
            itemList.add(item);
            for (int j = 0; j < book.chapterTitle.size(); j++) {
                item = new ItemAdapter(2, book.isLocalized, i, j,
                        Float.valueOf(book.chapterReadingProgress.get(j) * 100.0f).intValue(),
                        book.chapterTitle.get(j));
                itemList.add(item);
            }
        }
        recyclerViewAdapter.notifyDataSetChanged();
    }

    public void setBookCover(Bitmap blurredCover){
        for(int i = 0; i < itemList.size(); i++){
            if(itemList.get(i).itemType == 1){
                itemList.get(i).bookCover = blurredCover;
            }
        }
        recyclerViewAdapter.notifyDataSetChanged();
    }

    public float getAppbarAlpha(){
        return appbarAlpha;
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

