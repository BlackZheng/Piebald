package com.blackzheng.me.piebald.util;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.LruCache;

import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.R;

/**
 * Created by BlackZheng on 2016/4/29.
 */
public class DrawableUtil {

    private static final int[] COLORS = {R.color.holo_blue_light, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_purple_light, R.color.holo_red_light};
    private static final int MEM_CACHE_SIZE = 1024 * 1024 * ((ActivityManager) App.getContext()
            .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() / 16;
    private static final LruCache<String, BitmapDrawable> DRAWABLE_LRU_CACHE = new LruCache<String, BitmapDrawable>(MEM_CACHE_SIZE){
        @Override
        protected int sizeOf(String key, BitmapDrawable value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                return value.getBitmap().getByteCount();
            }
            // Pre HC-MR1
            return value.getBitmap().getRowBytes() * value.getBitmap().getHeight();
        }
    };
    /**
     * 根据宽高得到适当尺寸的Drawable
     * @param drawable
     * @param width
     * @param height
     * @return
     */
    public static Drawable toSuitableDrawable(Drawable drawable, int width, int height) // drawable 转换成bitmap
    {
//        BitmapDrawable bd =
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ?Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565;// 取drawable的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);// 建立对应bitmap
        Canvas canvas = new Canvas(bitmap);// 建立对应bitmap的画布
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);// 把drawable内容画到画布中
        return new BitmapDrawable(bitmap);
    }
    public static Drawable getDrawable(String color, int width, int height){
        BitmapDrawable bd = DRAWABLE_LRU_CACHE.get(color);
        if(bd == null){
            Bitmap.Config config = Bitmap.Config.RGB_565;
            Bitmap bitmap = Bitmap.createBitmap(width, height, config);
            bitmap.eraseColor(Color.parseColor(color));
            bd = new BitmapDrawable(bitmap);
            DRAWABLE_LRU_CACHE.put(color, bd);
        }
        return bd;
    }
    public static int[] getDefaultColors(){
        return COLORS;
    }
}
