package com.blackzheng.me.piebald.data;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;
import com.blackzheng.me.piebald.App;

/**
 * Created by BlackZheng on 2016/4/7.
 */
public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {
    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) { put(url, bitmap);}
}
