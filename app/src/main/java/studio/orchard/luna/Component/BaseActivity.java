package studio.orchard.luna.Component;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import studio.orchard.luna.MainActivity.MainActivity;
import studio.orchard.luna.R;

public class BaseActivity extends AppCompatActivity {

    public void exit() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("flag", Constants.MessageType.EXIT);
        this.startActivity(intent);
    }

    public DisplayMetrics getRealDisplayMetrics(){
        WindowManager windowManager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(outMetrics);
        return outMetrics;
    }

    public DisplayMetrics getUserDisplayMetrics(){
        WindowManager windowManager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics;
    }

    public int getNavigationBarHeight(){
        int resourceId = getResources().getIdentifier("navigation_bar_height","dimen", "android");
        return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
    }

    public int getStatusBarHeight(){
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
    }


    public Bitmap getActivityScreenShot() {
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, 0,
                getRealDisplayMetrics().widthPixels,
                getUserDisplayMetrics().heightPixels + getStatusBarHeight());
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public int getAppBarHeight(){
        int height = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        height += resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
        TypedValue typedValue = new TypedValue();
        height += getTheme().resolveAttribute(android.R.attr.windowTitleSize, typedValue, true) ?
                TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics()) : 0;
        return height;
    }

    public static Drawable getColorDrawable(String color){
        return new ColorDrawable(Color.parseColor(color));
    }

    public Drawable drawableWithColorFilter(int ID, int colorID){
        Drawable drawable = getDrawable(ID);
        assert drawable != null;
        drawable.setColorFilter(new PorterDuffColorFilter(getColor(colorID), PorterDuff.Mode.SRC_ATOP));
        return drawable;
    }

    protected void setStatusBarNavigationBar(int activityMode, int statusBar, int navigationBar){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS  | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //status bar transparent
            window.setStatusBarColor(Color.TRANSPARENT);
            //navigation bar white
            window.setNavigationBarColor(getResources().getColor(R.color.color_primary, getTheme()));

            View decorView = getWindow().getDecorView();
            int vis = decorView.getSystemUiVisibility();
            if(statusBar == Constants.ActivityMode.LIGHT_STATUS_BAR){
                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            if(navigationBar == Constants.ActivityMode.LIGHT_NAVIGATION_BAR
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                vis |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            decorView.setSystemUiVisibility(vis);
        }

        //刘海屏适配
        if(activityMode == Constants.ActivityMode.FULLSCREEN
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            WindowManager.LayoutParams lp =getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode =WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
    }
}
