package studio.orchard.luna.MainActivity.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import studio.orchard.luna.BookActivity.BookActivity;
import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.DataHolder.BookShelfDataHolder;
import studio.orchard.luna.Component.DataHolder.DataHolder;
import studio.orchard.luna.Component.Resolver.BookInfoResolver;
import studio.orchard.luna.Component.SerializedClass.BookItemInfo;
import studio.orchard.luna.Component.SerializedClass.v0.BookShelf;
import studio.orchard.luna.MainActivity.Adapter.MainBookShelfRecyclerViewAdapter;
import studio.orchard.luna.MainActivity.MainActivity;
import studio.orchard.luna.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainBookShelf extends Fragment {
    private View view;
    private MainBookShelfRecyclerViewAdapter recyclerViewAdapter;
    private MainBookShelfHandler mainBookShelfHandler;
    private MainActivity mainActivity;
    private BookShelf bookShelf;
    private List<BookItemInfo> itemList;
    private int itemOffset = 1;

    public MainBookShelf() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        mainActivity = (MainActivity)context;
    }

    private int getMarginTop(){
        return getArguments() != null ? getArguments().getInt("marginTop") : 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.main_fragment_bookshelf, container, false);
        initData();
        initView(getMarginTop());
        return view;
    }

    private void initData(){
        bookShelf = BookShelfDataHolder.getInstance().getBookShelf();
        mainBookShelfHandler = new MainBookShelfHandler(this);
        DataHolder.getInstance().putData("mainBookShelfHandler", mainBookShelfHandler);
        itemList = new ArrayList<>();
        itemList.add(new BookItemInfo(1));
        BookShelf.BookSeries bookSeries;
        for (int i = 0; i < bookShelf.bookSeriesList.size(); i++){
            bookSeries = bookShelf.bookSeriesList.get(i);
            if(bookSeries.bookSeriesName == null){
                BookShelfDataHolder.getInstance().removeBookSeriesFromBookShelf(bookSeries);
                BookShelfDataHolder.getInstance().saveBookShelfToFile(mainActivity);
                continue;
            }
            byte[] coverByte = bookSeries.bookSeriesCover;
            Bitmap cover = BitmapFactory.decodeByteArray(coverByte, 0, coverByte.length);
            BookItemInfo item = new BookItemInfo();
            item.index = i;
            item.bookName = bookSeries.bookSeriesName;
            item.bookAddress = bookSeries.bookSeriesAddress;
            item.bookCover = cover;
            item.bookCoverAddress = bookSeries.bookSeriesCoverAddress;
            item.isBookCoverResolved = true;
            item.updateNotification = bookSeries.updateNotification;
            itemList.add(item);
        }

    }

    private void initView(int marginTop){
        RecyclerView recyclerView = view.findViewById(R.id.main_bookshelf_recyclerview);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return itemList.get(position).type == 1 ? 3 : 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        //itemDecoration = new MainBookShelfRecyclerViewAdapter.ItemDecoration(mainActivity, itemList, 10);
        //recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerViewAdapter = new MainBookShelfRecyclerViewAdapter(itemList, marginTop);
        recyclerViewAdapter.setOnItemClickListener(new MainBookShelfRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                itemList.get(position).entrance = "MainBookShelf";
                itemList.get(position).index = position;
                itemList.get(position).isBookCoverResolved = true;
                DataHolder.getInstance().putData("item", itemList.get(position));
                Intent intent = new Intent();
                intent.setClass(view.getContext(), BookActivity.class);
                startActivity(intent);
                if(itemList.get(position).updateNotification){
                    removeUpdateNotification(position);
                }
            }
            @Override
            public void onCheckUpdate(View view) {
                checkBookUpdate(mainBookShelfHandler);
            }
        });
        recyclerViewAdapter.setOnItemLongClickListener((view, position) -> { });

        recyclerViewAdapter.setOnItemMoveListener((fromPosition, toPosition) -> {
                    fromPosition -= itemOffset;
                    toPosition -= itemOffset;
                    List<BookShelf.BookSeries> bookSeriesList = bookShelf.bookSeriesList;
                    if (fromPosition - toPosition == 1 || fromPosition - toPosition == -1){
                        Collections.swap(itemList, fromPosition + itemOffset, toPosition + itemOffset);
                        Collections.swap(bookSeriesList, fromPosition, toPosition);
                        int tmp = bookSeriesList.get(fromPosition).index;
                        itemList.get(fromPosition).index = bookSeriesList.get(toPosition).index;
                        bookSeriesList.get(fromPosition).index = bookSeriesList.get(toPosition).index;
                        itemList.get(toPosition).index = tmp;
                        bookSeriesList.get(toPosition).index = tmp;
                    } else {
                        if (fromPosition > toPosition) {
                            for (int i = fromPosition; i > toPosition; i--){
                                Collections.swap(itemList, i - 1 + itemOffset, i + itemOffset);
                                Collections.swap(bookSeriesList, i - 1, i);
                                int tmp = bookSeriesList.get(i - 1).index;
                                itemList.get(i - 1).index = bookSeriesList.get(i).index;
                                bookSeriesList.get(i - 1).index = bookSeriesList.get(i).index;
                                itemList.get(i).index = tmp;
                                bookSeriesList.get(i).index = tmp;
                            }
                        } else if(fromPosition < toPosition) {
                            for (int i = fromPosition; i < toPosition; i++){
                                Collections.swap(itemList, i + itemOffset, i + 1 + itemOffset);
                                Collections.swap(bookSeriesList, i, i + 1);
                                int tmp = bookSeriesList.get(i + 1).index;
                                itemList.get(i + 1).index = bookSeriesList.get(i).index;
                                bookSeriesList.get(i + 1).index = bookSeriesList.get(i).index;
                                itemList.get(i).index = tmp;
                                bookSeriesList.get(i).index = tmp;
                            }
                        }
                    }
                    //recyclerView.removeItemDecoration(itemDecoration);
                    //itemDecoration = new MainBookShelfRecyclerViewAdapter.ItemDecoration(mainActivity, itemList, 10);
                    //recyclerView.addItemDecoration(itemDecoration);
                    //recyclerViewAdapter.notifyDataSetChanged();
                });
        recyclerViewAdapter.setOnItemClear(() -> BookShelfDataHolder.getInstance().saveBookShelfToFile(mainActivity));
        recyclerView.setAdapter(recyclerViewAdapter);
        ItemTouchHelper.Callback callback =
                new MainBookShelfRecyclerViewAdapter.MainBookShelfRecyclerViewItemTouchHelper(recyclerViewAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void checkBookUpdate(final Handler handler){
        mainBookShelfHandler.resetUpdatedNum();
        Toast.makeText(mainActivity, "正在检查书籍更新", Toast.LENGTH_SHORT).show();
        new Thread(()->{
            BookInfoResolver bookInfoResolver = new BookInfoResolver();
            for(int i = 0; i < bookShelf.bookSeriesList.size(); i++){
                bookShelf.bookSeriesList.get(i).index = i;
                try{
                    BookShelf.BookSeries bookSeries = bookInfoResolver.getBookUpdate(bookShelf.bookSeriesList.get(i).bookSeriesID);
                    Message msg = new Message();
                    msg.what = Constants.MessageType.BOOK_UPDATE;
                    msg.arg1 = i + itemOffset;
                    msg.obj = bookSeries;
                    handler.sendMessage(msg);
                }catch (Exception e){ e.printStackTrace(); }
            }
            Message msg = new Message();
            msg.what = Constants.MessageType.BOOK_UPDATE_FINISH;
            handler.sendMessage(msg);
        }).start();
    }

    public static class MainBookShelfHandler extends Handler{
        private MainBookShelf mainBookShelf;
        private int updatedNum;

        private MainBookShelfHandler(MainBookShelf mainBookShelf){
            this.mainBookShelf = mainBookShelf;
            this.updatedNum = 0;
        }
        //public int getUpdatedNum() { return updatedNum; }
        public void resetUpdatedNum() { updatedNum = 0; }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            BookShelf.BookSeries bookSeries = (BookShelf.BookSeries)msg.obj;
            switch (msg.what){
                case Constants.MessageType.BOOK_REMOVE_FAVOR:
                    mainBookShelf.removeBookSeriesFromBookShelf(bookSeries);
                    break;
                case Constants.MessageType.BOOK_ADD_FAVOR:
                    mainBookShelf.addBookSeriesToBookShelf(bookSeries);
                    break;
                case Constants.MessageType.BOOK_UPDATE:
                    if (BookShelfDataHolder.getInstance().mergeBookSeries(bookSeries)){
                        updatedNum++;
                        mainBookShelf.addUpdateNotification(msg.arg1);
                    }
                    break;
                case Constants.MessageType.BOOK_UPDATE_FINISH:
                    Toast.makeText(mainBookShelf.mainActivity,
                            updatedNum == 0 ? "没有更新的书籍" : "共" + updatedNum + "本书有更新" ,
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void addUpdateNotification(int index){
        itemList.get(index).updateNotification = true;
        bookShelf.bookSeriesList.get(index - itemOffset).updateNotification = true;
        recyclerViewAdapter.notifyItemChanged(index);
        BookShelfDataHolder.getInstance().saveBookShelfToFile(mainActivity);
    }

    private void removeUpdateNotification(int index){
        itemList.get(index).updateNotification = false;
        bookShelf.bookSeriesList.get(index - itemOffset).updateNotification = false;
        recyclerViewAdapter.notifyItemChanged(index);
        BookShelfDataHolder.getInstance().saveBookShelfToFile(mainActivity);
    }

    private void addBookSeriesToBookShelf(BookShelf.BookSeries bookSeries){
        BookShelfDataHolder.getInstance().addBookSeriesToBookShelf(bookSeries);
        byte[] coverByte = bookSeries.bookSeriesCover;
        Bitmap cover = BitmapFactory.decodeByteArray(coverByte, 0, coverByte.length);
        BookItemInfo item = new BookItemInfo();
        item.bookName = bookSeries.bookSeriesName;
        item.bookAddress = bookSeries.bookSeriesAddress;
        item.bookCover = cover;
        itemList.add(item);
        recyclerViewAdapter.notifyItemInserted(itemList.size() - 1);
        BookShelfDataHolder.getInstance().saveBookShelfToFile(mainActivity);
    }

    private void removeBookSeriesFromBookShelf(BookShelf.BookSeries bookSeries){
        itemList.remove(bookSeries.index + itemOffset);
        recyclerViewAdapter.notifyDataSetChanged();
        BookShelfDataHolder.getInstance().removeBookSeriesFromBookShelf(bookSeries);
        BookShelfDataHolder.getInstance().saveBookShelfToFile(mainActivity);
    }
}
