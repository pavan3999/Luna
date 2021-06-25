package studio.orchard.luna.Component;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class Blur {

    private Blur(){ }

    public static Bitmap getBlurBitmap(Bitmap bitmap, float radius, float scalar, int times, Context context) {
        int width = Math.round(bitmap.getWidth() * scalar);
        int height = Math.round(bitmap.getHeight() * scalar);
        Bitmap bitmap1 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        bitmap1 = Bitmap.createScaledBitmap(bitmap1, width, height,false);
        for (int i = 0; i < times; i++){
            blurProcess(bitmap1, radius, context);
        }
        return bitmap1;
    }

    public static Bitmap getBlurBitmap(Bitmap bitmap, float radius, int width, int height, int times, Context context) {
        Bitmap bitmap1 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        bitmap1 = Bitmap.createScaledBitmap(bitmap1, width, height,false);
        for (int i = 0; i < times; i++){
            blurProcess(bitmap1, radius, context);
        }
        return bitmap1;
    }

    private static void blurProcess(Bitmap bitmap, float radius, Context context) {
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur gaussianBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createTyped(rs, allIn.getType());
        gaussianBlur.setRadius(radius);
        gaussianBlur.setInput(allIn);
        gaussianBlur.forEach(allOut);
        allOut.copyTo(bitmap);
        rs.destroy();
        allIn.destroy();
        allOut.destroy();
    }
}
