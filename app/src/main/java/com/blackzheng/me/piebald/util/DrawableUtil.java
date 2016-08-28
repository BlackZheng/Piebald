package com.blackzheng.me.piebald.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.blackzheng.me.piebald.R;

/**
 * Created by BlackZheng on 2016/4/29.
 */
public class DrawableUtil {

    private static final int[] COLORS = {R.color.holo_blue_light, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_purple_light, R.color.holo_red_light};

    /**
     * 根据宽高得到适当尺寸的Drawable
     * @param drawable
     * @param width
     * @param height
     * @return
     */
    public static Drawable toSuitableDrawable(Drawable drawable, int width, int height) // drawable 转换成bitmap
    {
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ?Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565;// 取drawable的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);// 建立对应bitmap
        Canvas canvas = new Canvas(bitmap);// 建立对应bitmap的画布
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);// 把drawable内容画到画布中
        return new BitmapDrawable(bitmap);
    }
    public static int[] getDefaultColors(){
        return COLORS;
    }
}
