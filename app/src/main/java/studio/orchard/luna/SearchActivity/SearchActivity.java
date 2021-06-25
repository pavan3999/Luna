package studio.orchard.luna.SearchActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;

import java.util.List;

import studio.orchard.blurview.BlurView;
import studio.orchard.luna.BookActivity.BookActivity;
import studio.orchard.luna.Component.BaseActivity;
import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.DataHolder.DataHolder;
import studio.orchard.luna.Component.Resolver.BookListResolver;
import studio.orchard.luna.Component.SerializedClass.BookItemInfo;
import studio.orchard.luna.R;
import studio.orchard.luna.SearchActivity.Adapter.SearchActivityRecyclerViewAdapter;


public class SearchActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private BlurView blurView;
    private EditText editText;
    private TextView resultTipsText;
    private InputMethodManager inputMethodManager;

    private int activityMode;
    private int searchType;
    private String searchAddress;
    private String activityTitle;
    private String searchName;

    private List<BookItemInfo> itemList;
    private SearchActivityRecyclerViewAdapter recyclerViewAdapter;
    private BookListResolver bookListResolver;

    public SearchActivity(){

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        DataHolder.getInstance().putData("searchBookListResolver", bookListResolver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarNavigationBar(
                Constants.ActivityMode.NORMAL,
                Constants.ActivityMode.LIGHT_STATUS_BAR,
                Constants.ActivityMode.LIGHT_NAVIGATION_BAR);
        setContentView(R.layout.search_activity);
        initData(savedInstanceState);
        initView();
    }


    private void initData(Bundle savedInstanceState){
        if( savedInstanceState == null){
            bookListResolver = new BookListResolver();
            itemList = bookListResolver.getItemList();
            itemList.add(new BookItemInfo(1));
        } else {
            bookListResolver = (BookListResolver) DataHolder.getInstance().getData("searchBookListResolver");
            itemList = bookListResolver.getItemList();
        }
        bookListResolver.setBookListResolverListener(new BookListResolver.BookListResolverListener() {
            @Override
            public Context getContext() {
                return SearchActivity.this;
            }

            @Override
            public void onBookListResolveFinished() {
                recyclerViewAdapter.notifyDataSetChanged();
            }
            @Override
            public void onBookListResolveReachedEnd() {
                recyclerViewAdapter.notifyDataSetChanged();
                if(searchType == Constants.SearchType.BOOK_AUTHOR){
                    if(searchAddress.equals(Constants.Connector.SEARCH_AUTHOR)){
                        searchAddress = Constants.Connector.SEARCH_BOOK;
                        bookListResolver.getBookList(searchAddress, searchName);
                    }else{
                        resultTipsText.setVisibility(bookListResolver.getItemList().size() == 1 ? View.VISIBLE : View.GONE);
                    }
                }else{
                    resultTipsText.setVisibility(bookListResolver.getItemList().size() == 1 ? View.VISIBLE : View.GONE);
                }
            }
            @Override
            public void onBookCoverResolveFinished(int index) {
                recyclerViewAdapter.notifyItemChanged(index);
            }
        });

        activityTitle = (String)DataHolder.getInstance().getData("searchActivityTitle");
        searchName = (String)DataHolder.getInstance().getData("searchName");
        Object obj = DataHolder.getInstance().getData("searchActivityMode");
        activityMode = obj != null ? (int)obj : Constants.SearchType.SHOW;
        obj = DataHolder.getInstance().getData("searchType");
        searchType = obj == null ? Constants.SearchType.SHOW : (int)obj;
        if(searchType == Constants.SearchType.BOOK_AUTHOR){
            searchAddress = Constants.Connector.SEARCH_AUTHOR;
        }else if(searchType == Constants.SearchType.BOOK){
            searchAddress = Constants.Connector.SEARCH_BOOK;
        }else if(searchType == Constants.SearchType.AUTHOR){
            searchAddress = Constants.Connector.SEARCH_AUTHOR;
        }else if(searchType == Constants.SearchType.TAG){
            searchAddress = Constants.Connector.SEARCH_TAG;
        }
    }

    private void initView(){
        AppBarLayout appBar = findViewById(R.id.search_appbar);
        blurView = findViewById(R.id.search_blurview);
        blurView.setTarget(findViewById(R.id.search_targetview))
                .setBinding(appBar)
                .setMask(getColorDrawable("#C0FFFFFF"))
                .setOpacity(0.00f)
                .enable();
        Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }
        setTitle(null);
        ImageView imgBackgroundCover = findViewById(R.id.search_background);
        Bitmap backgroundCover = (Bitmap) DataHolder.getInstance().getData("searchActivityBackground");
        imgBackgroundCover.setImageBitmap(backgroundCover);

        initRecyclerView(getAppBarHeight());

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        editText = findViewById(R.id.search_edit_text);
        if(activityMode == Constants.SearchType.SHOW){
            //展示模式，不可以编辑搜索，需要提供title
            editText.setText(activityTitle);
            editText.setHint("");
            editText.setEnabled(false);
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
            bookListResolver.getBookList(searchAddress, searchName);
        } else {
            //搜索模式
            editText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchBook(editText.getText().toString());
                }
                return false;
            });
            editText.setHint("搜索 轻小说/作者");
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            editText.requestFocus();
        }
        resultTipsText = findViewById(R.id.search_result_tips);
        resultTipsText.setVisibility(View.GONE);
    }

    private void initRecyclerView(int marginTop){
        recyclerView = findViewById(R.id.search_recyclerview);
        GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return itemList.get(position).type == 1 ? 3 : 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SearchActivityRecyclerViewAdapter.ItemDecoration(this, 10));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerViewAdapter = new SearchActivityRecyclerViewAdapter(itemList, marginTop);
        recyclerViewAdapter.setOnItemClickListener((view, position) -> {
            itemList.get(position).entrance = "Search";
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
                float appbarAlpha = Math.min((float)recyclerView.computeVerticalScrollOffset() / getAppBarHeight() * 10, 1.00f);
                blurView.setAlpha(appbarAlpha);
                int lastVisibleItem =  layoutManager.findLastVisibleItemPosition();
                int totalItemCount = recyclerViewAdapter.getItemCount();
                if ((dy > 0)
                        && (lastVisibleItem >= totalItemCount - 16)
                        && (!bookListResolver.isResolving())) {
                    bookListResolver.getBookList(searchAddress, searchName);
                }
            }
        });
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void searchBook(String str){
        if(!str.equals("") && !str.equals(searchName)){
            if(searchType == Constants.SearchType.BOOK_AUTHOR){
                searchAddress = Constants.Connector.SEARCH_AUTHOR;
            }else if(searchType == Constants.SearchType.BOOK){
                searchAddress = Constants.Connector.SEARCH_BOOK;
            }else if(searchType == Constants.SearchType.AUTHOR){
                searchAddress = Constants.Connector.SEARCH_AUTHOR;
            }else if(searchType == Constants.SearchType.TAG){
                searchAddress = Constants.Connector.SEARCH_TAG;
            }
            searchName = str;
            bookListResolver.clearItemList();
            itemList.add(new BookItemInfo(1));
            recyclerView.requestFocus();
            resultTipsText.setVisibility(View.GONE);
            recyclerViewAdapter.notifyDataSetChanged();
            bookListResolver.getBookList(searchAddress, searchName);
            if (inputMethodManager.isActive() && getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.search_menu_search:
                searchBook(editText.getText().toString());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.popup_fadeout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(activityMode == 0) return true; //展示模式，不需要searchMenu
        getMenuInflater().inflate(R.menu.serach_menu, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search_menu_search);
        searchMenuItem.setIcon(R.drawable.ic_search);
        return true;
    }
}
