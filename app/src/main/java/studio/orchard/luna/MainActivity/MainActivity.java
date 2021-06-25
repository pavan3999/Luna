package studio.orchard.luna.MainActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import studio.orchard.blurview.BlurView;
import studio.orchard.luna.BookActivity.BookActivity;
import studio.orchard.luna.Component.BaseActivity;
import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.DataHolder.BookShelfDataHolder;
import studio.orchard.luna.Component.DataHolder.DataHolder;
import studio.orchard.luna.Component.DataHolder.UserSettingDataHolder;
import studio.orchard.luna.Component.SerializedClass.BookItemInfo;
import studio.orchard.luna.Component.SerializedClass.v0.BookShelf;
import studio.orchard.luna.LoginActivity.LoginActivity;
import studio.orchard.luna.MainActivity.Adapter.MainViewPagerAdapter;
import studio.orchard.luna.MainActivity.Fragment.MainBookShelf;
import studio.orchard.luna.MainActivity.Fragment.MainCategory;
import studio.orchard.luna.MainActivity.Fragment.MainLatest;
import studio.orchard.luna.MainActivity.Fragment.MainRecommendation;
import studio.orchard.luna.R;
import studio.orchard.luna.SearchActivity.SearchActivity;

public class MainActivity extends BaseActivity {
    private boolean exiting;
    private boolean hasUpdate;
    private double latestVersion;
    private String latestNotes;
    private String latestLink;

    //private AppBarLayout appBar;
    private MainPopupWindow mainPopupWindow;
    private BlurView blurView;

    private ImageView navBackground;
    private TextView navUserName;
    private FrameLayout navContinueButton;
    private TextView navContinueTitle;


    public MainActivity(){
    }

    @Override
    public void onResume(){
        super.onResume();
        //nav header的背景设置
        BookShelf bookShelf = BookShelfDataHolder.getInstance().getBookShelf();
        if(bookShelf.readingProgress != -1 && bookShelf.readingProgress < bookShelf.bookSeriesList.size()){
            byte[] coverByte = bookShelf.bookSeriesList.get(bookShelf.readingProgress).bookSeriesCover;
            Bitmap cover = BitmapFactory.decodeByteArray(coverByte, 0, coverByte.length);
            Bitmap blurredCover =  BlurView.process(cover, 5f, 270, 360, null);
            navBackground.setImageBitmap(blurredCover);
            navContinueButton.setVisibility(View.VISIBLE);
            navContinueTitle.setText(bookShelf.bookSeriesList.get(bookShelf.readingProgress).bookSeriesName);
            cover.recycle();
        }else{
            navContinueButton.setVisibility(View.GONE);
            navBackground.setImageResource(R.drawable.ic_menu_cover);
        }
        //nav header的用户名设置
        if(UserSettingDataHolder.getInstance().hasLogin()){
            navUserName.setText(UserSettingDataHolder.getInstance().getUserSetting().userName);
        }
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
        setContentView(R.layout.main_activity);
//        initData();
        initView();
        initPopupWindow();
    }

    private void initData(){
        JSONObject profile = (JSONObject) DataHolder.getInstance().getData("profile");
        if(profile != null){
            try{
                latestVersion = profile.getDouble("latestVersion");
                hasUpdate = latestVersion > Constants.Application.VERSION;
                latestNotes = profile.getString("latestNotes");
                latestLink = profile.getString("latestLink");
            }catch (Exception ignored) { hasUpdate = false; }
        }else{ hasUpdate = false; }

        if(!DataHolder.getInstance().internetConnection){
            if(UserSettingDataHolder.getInstance().getUserSetting().enable){
                Toast.makeText(this, "无网络连接，无法使用在线功能", Toast.LENGTH_SHORT).show();
            }else{
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示");
                builder.setMessage("当前版本已锁定。");
                builder.setPositiveButton("确定", (dialog, which) -> { });
                builder.setOnDismissListener(dialog -> exit());
                builder.show();
            }
        }else if(!DataHolder.getInstance().verifyEnablePassed){
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("提示");
            builder.setMessage("当前版本已锁定。");
            builder.setPositiveButton("确定", (dialog, which) -> {
            });
            builder.setOnDismissListener(dialog -> exit());
            builder.show();
        }else if(!DataHolder.getInstance().verifyVersionPassed || !DataHolder.getInstance().verifyTimePassed){
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("提示");
            builder.setMessage("当前版本已过期，请更新到最新版本。");
            builder.setPositiveButton("更新", (dialog, which) -> {
                Uri uri = Uri.parse(latestLink);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            });
            builder.setNegativeButton("取消", (dialog, which) -> { });
            builder.show();
        }
    }


    private void initView(){
        AppBarLayout appBar = findViewById(R.id.main_content_appbar);
        initNavigationView();
        initViewPager(getAppBarHeight());
        /*
        appBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //observer.notifyListeners(appBar.getWidth(), appBar.getHeight());
                appBarHeight = appBar.getHeight();
                initToolbarViewPager();
                appBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        */
        blurView = findViewById(R.id.main_content_blurview);
        blurView.setTarget(findViewById(R.id.main_content_targetview))
                .setBinding(appBar)
                .setMask(getColorDrawable("#C0FFFFFF"))
                .enable();
    }

    private void initNavigationView(){
        NavigationView navigationView = findViewById(R.id.main_nav_view);
        navBackground = navigationView.getHeaderView(0).findViewById(R.id.main_nav_background);
        FrameLayout navLoginButton = navigationView.getHeaderView(0).findViewById(R.id.main_nav_login);
        navContinueButton = navigationView.getHeaderView(0).findViewById(R.id.main_nav_continue_read);
        navContinueTitle = navigationView.getHeaderView(0).findViewById(R.id.main_nav_continue_title);
        navUserName = navigationView.getHeaderView(0).findViewById(R.id.main_nav_login_username);
        navLoginButton.setOnClickListener(v -> {
            if(!UserSettingDataHolder.getInstance().hasLogin()){
                startLoginActivity();
                return;
            }
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("提示");
            builder.setMessage("当前已登录，是否更换账号？");
            builder.setPositiveButton("确定", (dialog, which) -> startLoginActivity());
            builder.setNegativeButton("取消", (dialog, which) -> {
            });
            builder.show();
        });
        navContinueButton.setOnClickListener(v -> {
            BookShelf bookShelf = BookShelfDataHolder.getInstance().getBookShelf();
            if(bookShelf.readingProgress != -1 && bookShelf.readingProgress < bookShelf.bookSeriesList.size()){
                BookShelf.BookSeries bookSeries = bookShelf.bookSeriesList.get(bookShelf.readingProgress);
                byte[] coverByte = bookSeries.bookSeriesCover;
                Bitmap cover = BitmapFactory.decodeByteArray(coverByte, 0, coverByte.length);
                BookItemInfo item = new BookItemInfo();
                item.bookName = bookSeries.bookSeriesName;
                item.bookAddress = bookSeries.bookSeriesAddress;
                item.bookCover = cover;
                item.bookCoverAddress = bookSeries.bookSeriesCoverAddress;
                item.isBookCoverResolved = true;
                DataHolder.getInstance().putData("item", item);
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, BookActivity.class);
                startActivity(intent);
            }
        });
        //自定义的navigation view菜单
        FrameLayout navSearch = navigationView.getHeaderView(0).findViewById(R.id.main_nav_search);
        FrameLayout navSetting = navigationView.getHeaderView(0).findViewById(R.id.main_nav_setting);
        FrameLayout navUpdate = navigationView.getHeaderView(0).findViewById(R.id.main_nav_update);
        CardView navUpdateHint = navigationView.getHeaderView(0).findViewById(R.id.main_nav_update_hint);
        FrameLayout navAbout = navigationView.getHeaderView(0).findViewById(R.id.main_nav_about);
        navSearch.setOnClickListener(v -> startSearchActivity());
        navSetting.setOnClickListener(v -> Toast.makeText(MainActivity.this, "设置页面正在开发", Toast.LENGTH_SHORT).show());
        navUpdate.setOnClickListener(v -> {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            if(hasUpdate){
                builder.setTitle("发现新版本 " + latestVersion);
                builder.setMessage(latestNotes);
                builder.setPositiveButton("更新", (dialog, which) -> {
                    Uri uri = Uri.parse(latestLink);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                });
                builder.setNegativeButton("取消", (dialog, which) -> { });
            }else{
                builder.setTitle("当前已是最新版本");
                builder.setMessage(latestNotes);
                builder.setPositiveButton("确定", (dialog, which) -> { });
            }
            builder.show();
        });
        navUpdateHint.setVisibility(hasUpdate ? View.VISIBLE : View.INVISIBLE);
        navAbout.setOnClickListener(v -> mainPopupWindow.show());
    }

    private void initViewPager(int marginTop){
        Toolbar toolbar = findViewById(R.id.main_content_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        //toolbar.setTitleTextColor(Color.WHITE);
        setTitle("");

        //设置DrawerLayout
        DrawerLayout drawer = findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //按钮跟随drawer变化
        //drawer.addDrawerListener(toggle);
        //toggle.syncState();
        //自定义menu图标并停用toggle
        toggle.setDrawerIndicatorEnabled(false);
        toolbar.setNavigationOnClickListener(view -> drawer.openDrawer(GravityCompat.START));

        //设置TabLayout
        TabLayout tabLayout = findViewById(R.id.main_content_tablayout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_main_tab_favor));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_menu_home));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_main_tab_latest));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_main_tab_category));
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);

        //设置ViewPager
        ViewPager viewPager = findViewById(R.id.main_content_viewpager);
        Bundle bundle = new Bundle();
        bundle.putInt("marginTop", marginTop);
        MainBookShelf mainBookShelf = new MainBookShelf();
        mainBookShelf.setArguments(bundle);
        MainRecommendation mainRecommendation = new MainRecommendation();
        mainRecommendation.setArguments(bundle);
        MainLatest mainLatest = new MainLatest();
        mainLatest.setArguments(bundle);
        MainCategory mainCategory = new MainCategory();
        mainCategory.setArguments(bundle);
        List<Fragment> fragmentList = Arrays.asList(mainBookShelf, mainRecommendation, mainLatest, mainCategory);
        MainViewPagerAdapter mainPagerAdapter = new MainViewPagerAdapter(
                getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, //懒加载
                fragmentList);

        viewPager.setAdapter(mainPagerAdapter);
        viewPager.setOffscreenPageLimit(fragmentList.size());
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                Objects.requireNonNull(tab.getIcon()).setColorFilter(new PorterDuffColorFilter(getColor(R.color.color_accent), PorterDuff.Mode.SRC_ATOP));
                viewPager.setCurrentItem(tab.getPosition());

            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Objects.requireNonNull(tab.getIcon()).clearColorFilter();
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.setCurrentItem(0);
        Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(0)).getIcon()).setColorFilter(new PorterDuffColorFilter(getColor(R.color.color_accent), PorterDuff.Mode.SRC_ATOP));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void startLoginActivity(){
        final Bitmap screenShot = getActivityScreenShot();
        final Bitmap blurredScreenShot = BlurView.process(screenShot, 17f, 0.2f, BaseActivity.getColorDrawable("#B3FFFFFF"));
        screenShot.recycle();
        DataHolder.getInstance().putData("loginActivityBackground", blurredScreenShot);
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(0, R.anim.popup_fadeout);
    }

    private void startSearchActivity(){
        final Bitmap screenShot = getActivityScreenShot();
        final Bitmap blurredScreenShot = BlurView.process(screenShot, 17f, 0.2f, BaseActivity.getColorDrawable("#B3FFFFFF"));
        screenShot.recycle();
        DataHolder.getInstance().putData("searchActivityBackground", blurredScreenShot);
        DataHolder.getInstance().putData("searchActivityMode", Constants.SearchType.SEARCH);
        DataHolder.getInstance().putData("searchActivityTitle", null);
        DataHolder.getInstance().putData("searchName", null);
        DataHolder.getInstance().putData("searchType", Constants.SearchType.BOOK_AUTHOR);
        Intent intent = new Intent();
        intent.setClass(this, SearchActivity.class);
        startActivity(intent);
        overridePendingTransition(0, R.anim.popup_fadeout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.main_menu_search).setIcon(R.drawable.ic_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_menu_search) {
            startSearchActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            if (intent.getIntExtra("flag", 0) == Constants.MessageType.EXIT) {
                this.finish();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DrawerLayout drawer = findViewById(R.id.main_drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
            if (exiting) {
                this.finish();
            }else{
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                exiting = true;
                new Handler().postDelayed(() -> exiting= false, 2000);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void initPopupWindow(){
        mainPopupWindow = new MainPopupWindow(() -> MainActivity.this);
    }

/*
    public static class MainActivityObserver{
        private CopyOnWriteArrayList<OnGlobalLayoutListener> listeners;

        public interface OnGlobalLayoutListener{
            void onGlobalLayout(int width, int height);
        }

        public MainActivityObserver(){
            this.listeners = new CopyOnWriteArrayList<>();
        }

        public void addOnGlobalLayoutListener(OnGlobalLayoutListener listener){
            listeners.add(listener);
        }

        public void removeOnGlobalLayoutListener(OnGlobalLayoutListener listener){
            listeners.remove(listener);
        }

        public void notifyListeners(int width, int height){
            for(OnGlobalLayoutListener listener : listeners){
                listener.onGlobalLayout(width, height);
            }
        }
    }*/

}
