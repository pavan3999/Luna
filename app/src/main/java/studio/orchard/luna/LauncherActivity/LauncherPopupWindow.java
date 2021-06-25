package studio.orchard.luna.LauncherActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.cardview.widget.CardView;

import studio.orchard.blurview.BlurView;
import studio.orchard.luna.Component.BaseActivity;
import studio.orchard.luna.R;

public class LauncherPopupWindow {
    private LauncherActivity context;
    private View popupWindowView;
    private PopupWindow popupWindow;

    private PopUpWindowListener listener;

    public interface PopUpWindowListener{
        Context getContext();
        void onConfirmButtonClick();
        void onDismiss();
    }

    public LauncherPopupWindow(PopUpWindowListener listener){
        this.listener = listener;
        this.context = (LauncherActivity)listener.getContext();
        init();
    }

    @SuppressLint("InflateParams")
    private void init(){
        popupWindowView = context.getLayoutInflater().inflate(R.layout.launcher_popup, null);
        popupWindow = new PopupWindow(popupWindowView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setClippingEnabled(false);
        popupWindow.setAnimationStyle(R.style.AnimationFade);
        popupWindow.setOnDismissListener(() -> listener.onDismiss());
        CardView button = popupWindowView.findViewById(R.id.launcher_popup_confirm);
        button.setOnClickListener(v -> listener.onConfirmButtonClick());
    }


    void close(){
        popupWindow.dismiss();
    }
    void show(){
        if(popupWindow != null && !popupWindow.isShowing()){
            final Bitmap screenShot = context.getActivityScreenShot();
            final Bitmap blurredScreenShot = BlurView.process(screenShot, 17f, 0.2f, BaseActivity.getColorDrawable("#4DFFFFFF"));
            ImageView backgroundCover = popupWindowView.findViewById(R.id.launcher_popup_background);
            backgroundCover.setImageBitmap(blurredScreenShot);


            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(context).inflate(R.layout.launcher_activity, null);
            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            screenShot.recycle();
        }
    }
}
