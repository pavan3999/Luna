package studio.orchard.luna.LauncherActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import studio.orchard.blurview.BlurView;
import studio.orchard.luna.Component.BaseActivity;
import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.DataHolder.BookShelfDataHolder;
import studio.orchard.luna.Component.DataHolder.DataHolder;
import studio.orchard.luna.Component.DataHolder.UserSettingDataHolder;
import studio.orchard.luna.Component.Resolver.Connector;
import studio.orchard.luna.Component.SerializedClass.v0.BookShelf;
import studio.orchard.luna.LoginActivity.LoginActivity;
import studio.orchard.luna.MainActivity.MainActivity;
import studio.orchard.luna.R;


public class LauncherActivity extends BaseActivity {
    private LauncherPopupWindow launcherPopupWindow;
    private boolean dataLoadFinished;
    private boolean countDownFinished;
    private boolean verifyFinished;
    private boolean verifyEnableFinished;
    private boolean verifyVersionFinished;
    private boolean verifyTimeFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarNavigationBar(
                Constants.ActivityMode.FULLSCREEN,
                Constants.ActivityMode.LIGHT_STATUS_BAR,
                Constants.ActivityMode.LIGHT_NAVIGATION_BAR);
        setContentView(R.layout.launcher_activity);
        initData();
        initView();
        countDown();
        disableVerify();
//        verify();
    }

    private void disableVerify() {
        verifyEnableFinished = true;
        verifyVersionFinished = true;
        verifyTimeFinished = true;
        DataHolder.getInstance().internetConnection = true;
        DataHolder.getInstance().verifyEnablePassed = true;
        DataHolder.getInstance().verifyVersionPassed = true;
        DataHolder.getInstance().verifyTimePassed = true;
        UserSettingDataHolder.getInstance().getUserSetting().enable = true;
        isVerifyFinished();
    }

    private void initData(){
        BookShelfDataHolder.getInstance().init(this);
        UserSettingDataHolder.getInstance().init(this);
        dataLoadFinished = true;
        start();
    }

    private void initView() {
        //random background cover
        BookShelf bookShelf = BookShelfDataHolder.getInstance().getBookShelf();
        if(bookShelf.bookSeriesList.size() > 0){
            ImageView background = findViewById(R.id.launcher_background);
            byte[] coverByte = bookShelf.bookSeriesList.get((int)(Math.random()  * bookShelf.bookSeriesList.size())).bookSeriesCover;
            Bitmap cover = BitmapFactory.decodeByteArray(coverByte, 0, coverByte.length);
            Bitmap blurredCover = BlurView.process(cover, 5f, 270, 360, null);
            background.setImageBitmap(blurredCover);
            cover.recycle();
        }

    }

    private void countDown(){
        new Handler().postDelayed(new Runnable(){
            @Override
            public synchronized void run() {
                countDownFinished = true;
                start();
            }
        }, 1500);
    }

    private void verify(){
        Connector.getInstance().getProfile(new Connector.ConnectorListener() {
            @Override
            public void onFinish(Object obj) {
                try{
                    JSONObject profile = (JSONObject)obj;
                    DataHolder.getInstance().putData("profile", obj);
                    DataHolder.getInstance().internetConnection = true;
                    DataHolder.getInstance().verifyEnablePassed = profile.getBoolean("enableBeta");
                    DataHolder.getInstance().verifyVersionPassed = profile.getDouble("minimumVersion") <= Constants.Application.VERSION;
                    UserSettingDataHolder.getInstance().getUserSetting().enable = DataHolder.getInstance().verifyEnablePassed;
                    UserSettingDataHolder.getInstance().saveUserSettingToFile(LauncherActivity.this);
                } catch (Exception ignored){}
                verifyEnableFinished = true;
                verifyVersionFinished = true;
                isVerifyFinished();
            }
            @Override
            public void onExceptionThrown(Exception e) {
                DataHolder.getInstance().internetConnection = false;
                verifyEnableFinished = true;
                verifyVersionFinished = true;
                isVerifyFinished();}
        });
        Connector.getInstance().getTime(new Connector.ConnectorListener() {
            @Override
            public void onFinish(Object obj) {
                try{
                    JSONObject time = (JSONObject)obj;
                    DataHolder.getInstance().verifyTimePassed = time.getLong("sysTime1") <= Constants.Application.EXPIRE_DATE;
                } catch (Exception ignored){ }
                verifyTimeFinished = true;
                isVerifyFinished();
            }
            @Override
            public void onExceptionThrown(Exception e) { verifyTimeFinished = true; isVerifyFinished(); }
        });
    }

    private void isVerifyFinished(){
        if(verifyEnableFinished && verifyVersionFinished && verifyTimeFinished){
            verifyFinished = true;
            start();
        }
    }

    private void start(){
        if(!dataLoadFinished || !countDownFinished || !verifyFinished){ return; }
        final boolean hasLogin = UserSettingDataHolder.getInstance().hasLogin();
        if(UserSettingDataHolder.getInstance().getUserSetting().showInfo){
            launcherPopupWindow = new LauncherPopupWindow(new LauncherPopupWindow.PopUpWindowListener() {
                @Override
                public Context getContext() {
                    return LauncherActivity.this;
                }
                @Override
                public void onConfirmButtonClick() {
                    if(!DataHolder.getInstance().internetConnection){
                        Toast.makeText(LauncherActivity.this, "无网络连接，请连接网络后重启软件", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!DataHolder.getInstance().verifyEnablePassed) {
                        Toast.makeText(LauncherActivity.this, "该版本已过期，请下载最新版本", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!DataHolder.getInstance().verifyVersionPassed) {
                        Toast.makeText(LauncherActivity.this, "该版本已过期，请下载最新版本", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!DataHolder.getInstance().verifyTimePassed) {
                        Toast.makeText(LauncherActivity.this, "该版本已过期，请下载最新版本", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    launcherPopupWindow.close();
                    if(hasLogin) {startMainActivity();} else {startLoginActivity();}
                }
                @Override
                public void onDismiss() {
                    LauncherActivity.this.onBackPressed();
                }
            });
            launcherPopupWindow.show();
        }else{
            if(hasLogin){startMainActivity();} else {startLoginActivity();}
        }
    }

    private void startMainActivity(){
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(0, R.anim.popup_fadeout);
        this.finish();
    }

    private void startLoginActivity(){
        //需要重新登录
        final Bitmap screenShot = getActivityScreenShot();
        final Bitmap blurredScreenShot = BlurView.process(screenShot, 17f, 0.2f, BaseActivity.getColorDrawable("#B3FFFFFF"));
        screenShot.recycle();
        DataHolder.getInstance().putData("loginActivityBackground", blurredScreenShot);
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(0, R.anim.popup_fadeout);
        this.finish();
    }
}
