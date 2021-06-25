package studio.orchard.luna.MainActivity.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import studio.orchard.luna.BookActivity.BookActivity;
import studio.orchard.luna.Component.DataHolder.DataHolder;
import studio.orchard.luna.Component.Resolver.BookListResolver;
import studio.orchard.luna.Component.SerializedClass.BookItemInfo;
import studio.orchard.luna.MainActivity.Adapter.MainRecommendationRecyclerViewAdapter;
import studio.orchard.luna.MainActivity.MainActivity;
import studio.orchard.luna.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainRecommendation extends Fragment {
    private View view;
    private MainRecommendationRecyclerViewAdapter recyclerViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BookListResolver bookListResolver;
    private List<BookItemInfo> itemList;
    private boolean isCreated;
    private MainActivity mainActivity;

    public MainRecommendation() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        DataHolder.getInstance().putData("mainRecommendationBookListResolver", bookListResolver);
        DataHolder.getInstance().putData("mainRecommendationIsCreated", isCreated);
    }

    private int getMarginTop(){
        return getArguments() != null ? getArguments().getInt("marginTop") : 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.main_fragment_recommendation, container, false);
        initData(savedInstanceState);
        initView(getMarginTop());
        return view;
    }

    @Override
    public void onResume(){
        //懒加载
        super.onResume();
        if(!isCreated){
            isCreated = true;
            lazyLoad();
        }
    }

    private void lazyLoad(){
        if(bookListResolver.isResolving()) return;
        swipeRefreshLayout.setRefreshing(true);
        recyclerViewAdapter.notifyDataSetChanged();
        bookListResolver.getRecommendationBookList();
    }

    private void initData(Bundle savedInstanceState){
        if(savedInstanceState == null){
            bookListResolver = new BookListResolver();
            isCreated = false;
            itemList = bookListResolver.getItemList();
            itemList.add(new BookItemInfo(1));
        }else{
            bookListResolver = (BookListResolver) DataHolder.getInstance().getData("mainRecommendationBookListResolver");
            isCreated = (boolean) DataHolder.getInstance().getData("mainRecommendationIsCreated");
            itemList = bookListResolver.getItemList();
        }
        bookListResolver.setBookListResolverListener(new BookListResolver.BookListResolverListener() {
            @Override
            public Context getContext() { return mainActivity; }

            @Override
            public void onBookListResolveFinished() {
                swipeRefreshLayout.setRefreshing(false);
                recyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onBookCoverResolveFinished(int index) {
                recyclerViewAdapter.notifyItemChanged(index);
            }
        });
    }

    private void initView(int marginTop){
        swipeRefreshLayout = view.findViewById(R.id.main_recommendation_swipe_refresh_widget);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple);
        swipeRefreshLayout.setProgressViewOffset(false, marginTop, marginTop + 100);
        swipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            bookListResolver.clearItemList();
            itemList.add(new BookItemInfo(1));
            lazyLoad();
            recyclerViewAdapter.notifyDataSetChanged();
        },1000));
        RecyclerView recyclerView = view.findViewById(R.id.main_recommendation_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerViewAdapter = new MainRecommendationRecyclerViewAdapter(itemList, marginTop);
        recyclerViewAdapter.setOnItemClickListener((view, position) -> {
            itemList.get(position).entrance = "MainRecommendation";
            itemList.get(position).index = position;
            Intent intent = new Intent();
            DataHolder.getInstance().putData("item", itemList.get(position));
            intent.setClass(view.getContext(), BookActivity.class);
            startActivity(intent);
        });
        recyclerViewAdapter.setOnItemLongClickListener((view, position) -> { });
        recyclerView.setAdapter(recyclerViewAdapter);
    }
}
