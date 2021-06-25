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
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import studio.orchard.blurview.BlurView;
import studio.orchard.luna.Component.BaseActivity;
import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.DataHolder.DataHolder;
import studio.orchard.luna.Component.SerializedClass.BookItemInfo;
import studio.orchard.luna.MainActivity.Adapter.MainBookShelfRecyclerViewAdapter;
import studio.orchard.luna.MainActivity.Adapter.MainCategoryRecyclerViewAdapter;
import studio.orchard.luna.MainActivity.MainActivity;
import studio.orchard.luna.R;
import studio.orchard.luna.SearchActivity.SearchActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainCategory extends Fragment {
    private View view;
    private List<BookItemInfo> itemList;
    private MainActivity mainActivity;

    public MainCategory() {
        // Required empty public constructor
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
        view = inflater.inflate(R.layout.main_fragment_category, container, false);
        initData();
        initView(getMarginTop());
        return view;
    }

    void initData(){
        itemList = new ArrayList<>();
        itemList.add(new BookItemInfo(1));
        BookItemInfo item = new BookItemInfo();
        item.bookCover = BitmapFactory.decodeResource(getResources(), R.drawable.ic_category_lianai);
        item.index = Constants.Category.LIANAI;
        item.bookName = "恋爱";
        itemList.add(item);

        item = new BookItemInfo();
        item.bookCover = BitmapFactory.decodeResource(getResources(), R.drawable.ic_category_xiaoyuan);
        item.index = Constants.Category.XIAOYUAN;
        item.bookName = "校园";
        itemList.add(item);

        item = new BookItemInfo();
        item.bookCover = BitmapFactory.decodeResource(getResources(), R.drawable.ic_category_gaoxiao);
        item.index = Constants.Category.GAOXIAO;
        item.bookName = "搞笑";
        itemList.add(item);

        item = new BookItemInfo();
        item.bookCover = BitmapFactory.decodeResource(getResources(), R.drawable.ic_category_hougong);
        item.index = Constants.Category.HOUGONG;
        item.bookName = "后宫";
        itemList.add(item);

        item = new BookItemInfo();
        item.bookCover = BitmapFactory.decodeResource(getResources(), R.drawable.ic_category_maoxian);
        item.index = Constants.Category.MAOXIAN;
        item.bookName = "冒险";
        itemList.add(item);

        item = new BookItemInfo();
        item.bookCover = BitmapFactory.decodeResource(getResources(), R.drawable.ic_category_yishijie);
        item.index = Constants.Category.YISHIJIE;
        item.bookName = "异世界";
        itemList.add(item);

        item = new BookItemInfo();
        item.bookCover = BitmapFactory.decodeResource(getResources(), R.drawable.ic_category_mofa);
        item.index = Constants.Category.MOFA;
        item.bookName = "魔法";
        itemList.add(item);

        item = new BookItemInfo();
        item.bookCover = BitmapFactory.decodeResource(getResources(), R.drawable.ic_category_shengui);
        item.index = Constants.Category.SHENGUI;
        item.bookName = "神鬼";
        itemList.add(item);

        item = new BookItemInfo();
        item.bookCover = BitmapFactory.decodeResource(getResources(), R.drawable.ic_category_zhentan);
        item.index = Constants.Category.ZHENTAN;
        item.bookName = "侦探";
        itemList.add(item);

        item = new BookItemInfo();
        item.bookCover = BitmapFactory.decodeResource(getResources(), R.drawable.ic_category_kongbu);
        item.index = Constants.Category.KONGBU;
        item.bookName = "恐怖";
        itemList.add(item);
    }

    void initView(int marginTop){
        RecyclerView recyclerView = view.findViewById(R.id.main_category_recyclerview);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return itemList.get(position).type == 1 ? 2 : 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new MainCategoryRecyclerViewAdapter.ItemDecoration(mainActivity, 12));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        MainCategoryRecyclerViewAdapter recyclerViewAdapter = new MainCategoryRecyclerViewAdapter(itemList, marginTop);
        recyclerViewAdapter.setOnItemClickListener((view, position) -> {
            final Bitmap screenShot = mainActivity.getActivityScreenShot();
            final Bitmap blurredScreenShot = BlurView.process(screenShot, 17f, 0.2f, BaseActivity.getColorDrawable("#B3FFFFFF"));
            screenShot.recycle();
            DataHolder.getInstance().putData("searchActivityBackground", blurredScreenShot);
            DataHolder.getInstance().putData("searchActivityMode", Constants.SearchType.SHOW);
            DataHolder.getInstance().putData("searchActivityTitle", itemList.get(position).bookName);
            DataHolder.getInstance().putData("searchName", itemList.get(position).bookName);
            DataHolder.getInstance().putData("searchType", Constants.SearchType.TAG);
            Intent intent = new Intent();
            intent.setClass(mainActivity, SearchActivity.class);
            startActivity(intent);
            mainActivity.overridePendingTransition(0, R.anim.popup_fadeout);

        });
        recyclerViewAdapter.setOnItemLongClickListener((view, position) -> { });
        recyclerView.setAdapter(recyclerViewAdapter);
    }
}
