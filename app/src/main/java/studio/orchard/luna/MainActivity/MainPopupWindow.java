package studio.orchard.luna.MainActivity;

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

public class MainPopupWindow {
    private MainActivity context;
    private View popupWindowView;
    private PopupWindow popupWindow;

    public interface PopUpWindowListener{
        Context getContext();
    }

    public MainPopupWindow(PopUpWindowListener listener){
        this.context = (MainActivity) listener.getContext();
        init();
    }

    @SuppressLint("InflateParams")
    private void init(){
        popupWindowView = context.getLayoutInflater().inflate(R.layout.main_popup, null);
        popupWindow = new PopupWindow(popupWindowView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setClippingEnabled(false);
        popupWindow.setAnimationStyle(R.style.AnimationFade);
        CardView button = popupWindowView.findViewById(R.id.main_popup_confirm);
        button.setOnClickListener(v -> popupWindow.dismiss());
    }

    void show(){
        if(popupWindow != null && !popupWindow.isShowing()){
            final Bitmap screenShot = context.getActivityScreenShot();
            final Bitmap blurredScreenShot = BlurView.process(screenShot, 17f, 0.2f, BaseActivity.getColorDrawable("#80FFFFFF"));
            ImageView backgroundCover = popupWindowView.findViewById(R.id.main_popup_background);
            backgroundCover.setImageBitmap(blurredScreenShot);
            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(context).inflate(R.layout.main_activity, null);
            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            screenShot.recycle();
        }
    }

}
