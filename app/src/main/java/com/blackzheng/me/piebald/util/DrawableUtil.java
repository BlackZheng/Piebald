package com.blackzheng.me.piebald.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
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

import org.apache.velocity.runtime.resource.Resource;

/**
 * Created by BlackZheng on 2016/4/29.
 */
public class DrawableUtil {

    private static final int[] COLORS = {R.color.holo_blue_light, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_purple_light, R.color.holo_red_light};

    public static int[] getDefaultColors(){
        return COLORS;
    }
}
