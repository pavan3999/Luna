package studio.orchard.luna.Component;

import android.app.Application;
import android.graphics.Typeface;

import java.lang.reflect.Field;

public class Font extends Application {
    public static Typeface typeface;

    @Override
    public void onCreate() {
        super.onCreate();
        setTypeFace();
    }

    public void setTypeFace() {
        typeface = Typeface.createFromAsset(getAssets(), "font/方正书宋_GBK.ttf");
        try {
            Field field = Typeface.class.getDeclaredField("SANS_SERIF");
            field.setAccessible(true);
            field.set(null, typeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
