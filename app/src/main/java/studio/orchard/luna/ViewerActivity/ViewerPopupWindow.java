package studio.orchard.luna.ViewerActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import studio.orchard.luna.R;

class ViewerPopupWindow {
    private ViewerActivity context;
    private enum ExtendType {NONE, PROGRESS, SUN, FONT}
    private ExtendType currentExtendType;
    private PopupWindow popupWindow;
    private CardView extendView;
    private ViewerPopUpWindowListener listener;

    private FrameLayout progressFrame;
    private FrameLayout sunFrame;
    private FrameLayout fontFrame;

    private SeekBar progressSeekBar;
    private TextView progressStart;
    private TextView progressEnd;

    private SeekBar fontSizeSeekBar;

    interface ViewerPopUpWindowListener{
        Context getContext();
        void onCreate();
        void onDismiss();
        void setProgressSeekBar(SeekBar progressSeekBar, TextView start, TextView end);
        void setFontSizeSeekBar(SeekBar fontSeekBar);
        void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);
        void onFontSizeChanged(SeekBar seekBar, int progress, boolean fromUser);
        void onBackgroundColorButtonClicked(ViewerActivity.BackgroundColor newBackGroundColor);
    }

    ViewerPopupWindow(ViewerPopUpWindowListener listener){
        this.listener = listener;
        this.context = (ViewerActivity) listener.getContext();
        init();
    }

    @SuppressLint("InflateParams")
    private void init(){
        View popupWindowView = context.getLayoutInflater().inflate(R.layout.viewer_popup, null);
        popupWindow = new PopupWindow(popupWindowView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setClippingEnabled(false);
        popupWindow.setAnimationStyle(R.style.AnimationFade);

        final FrameLayout extendClick = popupWindowView.findViewById(R.id.viewer_popup_extend_click);
        extendClick.setOnClickListener(v -> {
            if(extendView.getVisibility() == View.GONE){
                popupWindow.dismiss();
            }
        });

        final ImageButton buttonProgress = popupWindowView.findViewById(R.id.viewer_popup_btn_progress);
        final ImageButton buttonFont = popupWindowView.findViewById(R.id.viewer_popup_btn_font);
        final ImageButton buttonSun = popupWindowView.findViewById(R.id.viewer_popup_btn_sun);
        final TranslateAnimation animShow = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        animShow.setDuration(300);
        final TranslateAnimation animHide = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f);
        animHide.setDuration(300);
        currentExtendType = ExtendType.NONE;
        extendView = popupWindowView.findViewById(R.id.viewer_popup_extend);
        extendView.setVisibility(View.GONE);

        popupWindow.setOnDismissListener(() -> {
            listener.onDismiss();
            buttonProgress.setImageResource(R.drawable.ic_viewer_progress);
            buttonSun.setImageResource(R.drawable.ic_viewer_sun);
            buttonFont.setImageResource(R.drawable.ic_viewer_font);
            currentExtendType = ExtendType.NONE;
            progressFrame.setVisibility(View.GONE);
            sunFrame.setVisibility(View.GONE);
            fontFrame.setVisibility(View.GONE);
            extendView.setVisibility(View.GONE);
        });

        buttonProgress.setOnClickListener(v -> {
            buttonSun.setImageResource(R.drawable.ic_viewer_sun);
            buttonFont.setImageResource(R.drawable.ic_viewer_font);
            if(extendView.getVisibility() == View.GONE){
                buttonProgress.setImageDrawable(context.drawableWithColorFilter(R.drawable.ic_viewer_progress, R.color.color_accent));
                extendView.startAnimation(animShow);
                extendView.setVisibility(View.VISIBLE);
            }else{
                if(currentExtendType == ExtendType.PROGRESS){
                    buttonProgress.setImageResource(R.drawable.ic_viewer_progress);
                    extendView.startAnimation(animHide);
                    extendView.setVisibility(View.GONE);
                }else{
                    buttonProgress.setImageDrawable(context.drawableWithColorFilter(R.drawable.ic_viewer_progress, R.color.color_accent));
                }
            }
            switchExtendView(ExtendType.PROGRESS);
        });
        buttonSun.setOnClickListener(v -> {
            buttonProgress.setImageResource(R.drawable.ic_viewer_progress);
            buttonFont.setImageResource(R.drawable.ic_viewer_font);
            if(extendView.getVisibility() == View.GONE){
                buttonSun.setImageDrawable(context.drawableWithColorFilter(R.drawable.ic_viewer_sun, R.color.color_accent));
                extendView.startAnimation(animShow);
                extendView.setVisibility(View.VISIBLE);
            }else{
                if(currentExtendType == ExtendType.SUN){
                    buttonSun.setImageResource(R.drawable.ic_viewer_sun);
                    extendView.startAnimation(animHide);
                    extendView.setVisibility(View.GONE);
                }else{
                    buttonSun.setImageDrawable(context.drawableWithColorFilter(R.drawable.ic_viewer_sun, R.color.color_accent));
                }
            }
            switchExtendView(ExtendType.SUN);
        });
        buttonFont.setOnClickListener(v -> {
            buttonProgress.setImageResource(R.drawable.ic_viewer_progress);
            buttonSun.setImageResource(R.drawable.ic_viewer_sun);
            if(extendView.getVisibility() == View.GONE){
                buttonFont.setImageDrawable(context.drawableWithColorFilter(R.drawable.ic_viewer_font, R.color.color_accent));
                extendView.startAnimation(animShow);
                extendView.setVisibility(View.VISIBLE);
            }else{
                if(currentExtendType == ExtendType.FONT){
                    buttonFont.setImageResource(R.drawable.ic_viewer_font);
                    extendView.startAnimation(animHide);
                    extendView.setVisibility(View.GONE);
                }else{
                    buttonFont.setImageDrawable(context.drawableWithColorFilter(R.drawable.ic_viewer_font, R.color.color_accent));
                }
            }
            switchExtendView(ExtendType.FONT);
        });

        progressFrame = popupWindowView.findViewById(R.id.viewer_popup_extend_progress);
        sunFrame = popupWindowView.findViewById(R.id.viewer_popup_extend_sun);
        fontFrame = popupWindowView.findViewById(R.id.viewer_popup_extend_font);
        progressFrame.setVisibility(View.GONE);
        sunFrame.setVisibility(View.GONE);
        fontFrame.setVisibility(View.GONE);

        progressSeekBar = popupWindowView.findViewById(R.id.viewer_popup_extend_progress_seekbar);
        progressStart = popupWindowView.findViewById(R.id.viewer_popup_extend_progress_start);
        progressEnd = popupWindowView.findViewById(R.id.viewer_popup_extend_progress_end);

        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                listener.onProgressChanged(seekBar, progress, fromUser);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        fontSizeSeekBar = popupWindowView.findViewById(R.id.viewer_popup_extend_font_size_seekbar);
        //TextView fontSizeStart = popupWindowView.findViewById(R.id.viewer_popup_extend_font_start);
        //TextView fontSizeEnd = popupWindowView.findViewById(R.id.viewer_popup_extend_font_end);
        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                listener.onFontSizeChanged(seekBar, progress, fromUser);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        CardView sunWhite = popupWindowView.findViewById(R.id.viewer_popup_sun_white);
        CardView sunGreen = popupWindowView.findViewById(R.id.viewer_popup_sun_green);
        CardView sunBlue = popupWindowView.findViewById(R.id.viewer_popup_sun_blue);
        CardView sunYellow = popupWindowView.findViewById(R.id.viewer_popup_sun_yellow);
        CardView sunGrey = popupWindowView.findViewById(R.id.viewer_popup_sun_grey);
        CardView sunBlack = popupWindowView.findViewById(R.id.viewer_popup_sun_black);

        sunWhite.setOnClickListener(v -> listener.onBackgroundColorButtonClicked(ViewerActivity.BackgroundColor.WHITE));
        sunGreen.setOnClickListener(v -> listener.onBackgroundColorButtonClicked(ViewerActivity.BackgroundColor.GREEN));
        sunBlue.setOnClickListener(v -> listener.onBackgroundColorButtonClicked(ViewerActivity.BackgroundColor.BLUE));
        sunYellow.setOnClickListener(v -> listener.onBackgroundColorButtonClicked(ViewerActivity.BackgroundColor.YELLOW));
        sunGrey.setOnClickListener(v -> listener.onBackgroundColorButtonClicked(ViewerActivity.BackgroundColor.GREY));
        sunBlack.setOnClickListener(v -> listener.onBackgroundColorButtonClicked(ViewerActivity.BackgroundColor.BLACK));
    }

    void show(){
        if(popupWindow != null && !popupWindow.isShowing()){
            listener.setProgressSeekBar(progressSeekBar, progressStart, progressEnd);
            listener.setFontSizeSeekBar(fontSizeSeekBar);
            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(context).inflate(R.layout.viewer_activity, null);
            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            listener.onCreate();
        }
    }

    private void switchExtendView(ExtendType extendType){
        switch (extendType){
            case PROGRESS:
                progressFrame.setVisibility(View.VISIBLE);
                sunFrame.setVisibility(View.GONE);
                fontFrame.setVisibility(View.GONE);
                break;
            case SUN:
                progressFrame.setVisibility(View.GONE);
                sunFrame.setVisibility(View.VISIBLE);
                fontFrame.setVisibility(View.GONE);
                break;
            case FONT:
                progressFrame.setVisibility(View.GONE);
                sunFrame.setVisibility(View.GONE);
                fontFrame.setVisibility(View.VISIBLE);
                break;
        }
        currentExtendType = extendType;
    }

}
