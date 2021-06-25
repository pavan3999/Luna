package studio.orchard.luna.MainActivity.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;
import java.util.Optional;

import studio.orchard.luna.BookActivity.BookActivity;
import studio.orchard.luna.Component.DataHolder.DataHolder;
import studio.orchard.luna.Component.Resolver.BookListResolver;
import studio.orchard.luna.Component.SerializedClass.BookItemInfo;
import studio.orchard.luna.MainActivity.Adapter.MainBookShelfRecyclerViewAdapter;
import studio.orchard.luna.MainActivity.Adapter.MainLatestRecyclerViewAdapter;
import studio.orchard.luna.MainActivity.MainActivity;
import studio.orchard.luna.R;

public class MainLatest extends Fragment {
    private View view;

    private MainLatestRecyclerViewAdapter recyclerViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BookListResolver bookListResolver;

    private boolean isCreated;
    private List<BookItemInfo> itemList;

    private MainActivity mainActivity;

    public MainLatest() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        mainActivity = (MainActivity)context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        DataHolder.getInstance().putData("mainLatestBookListResolver", bookListResolver);
        DataHolder.getInstance().putData("mainLatestIsCreated", isCreated);
    }

    private int getMarginTop(){
        return getArguments() != null ? getArguments().getInt("marginTop") : 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.main_fragment_latest, container, false);


        initData(savedInstanceState);
        initView(getMarginTop());
        return view;
    }

    @Override
    public void onResume(){
        //懒加载
        super.onResume();
        if(!isCreated){
            lazyLoad();
            isCreated = true;
        }
    }

    private void lazyLoad(){
        if(bookListResolver.isResolving()) return;
        swipeRefreshLayout.setRefreshing(true);
        recyclerViewAdapter.notifyDataSetChanged();
        bookListResolver.getLatestBookList();
    }

    private void initData(Bundle savedInstanceState){
        if(savedInstanceState == null){
            bookListResolver = new BookListResolver();
            isCreated = false;
            itemList = bookListResolver.getItemList();
            itemList.add(new BookItemInfo(1));
        }else{
            bookListResolver = (BookListResolver) DataHolder.getInstance().getData("mainLatestBookListResolver");
            isCreated = (boolean) DataHolder.getInstance().getData("mainLatestIsCreated");
            itemList = bookListResolver.getItemList();
        }
        bookListResolver.setBookListResolverListener(new BookListResolver.BookListResolverListener() {
            @Override
            public Context getContext() {
                return mainActivity;
            }

            @Override
            public void onBookListResolveFinished() {
                swipeRefreshLayout.setRefreshing(false);
                recyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onBookCoverResolveFinished(int index) {
                //recyclerViewAdapter.notifyItemChanged(index, itemList.get(index));
                recyclerViewAdapter.notifyItemChanged(index);
            }
        });
    }

    private void initView(int marginTop){
        swipeRefreshLayout = view.findViewById(R.id.main_latest_swipe_refresh_widget);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple);
        swipeRefreshLayout.setProgressViewOffset(false, marginTop, marginTop + 100);
        swipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            bookListResolver.clearItemList();
            itemList.add(new BookItemInfo(1));
            lazyLoad();
            recyclerViewAdapter.notifyDataSetChanged();
        },1000));

        RecyclerView recyclerView = view.findViewById(R.id.main_latest_recyclerview);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return itemList.get(position).type == 1 ? 3 : 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new MainLatestRecyclerViewAdapter.ItemDecoration(mainActivity, 10));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerViewAdapter = new MainLatestRecyclerViewAdapter(itemList, marginTop);
        recyclerViewAdapter.setOnItemClickListener((view, position) -> {
            itemList.get(position).entrance = "MainLatest";
            itemList.get(position).index = position;
            Intent intent = new Intent();
            DataHolder.getInstance().putData("item", itemList.get(position));
            intent.setClass(view.getContext(), BookActivity.class);
            startActivity(intent);
        });
        recyclerViewAdapter.setOnItemLongClickListener((view, position) -> { });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem =  layoutManager.findLastVisibleItemPosition();
                int totalItemCount = recyclerViewAdapter.getItemCount();
                if ((dy > 0) &&(lastVisibleItem >= totalItemCount - 16)) {
                    //dy > 0 表示向下滑动 totalItemCount - n 表示剩下n个item自动加载
                    bookListResolver.getLatestBookList();
                }

            }
        });
        recyclerView.setAdapter(recyclerViewAdapter);
    }
}
