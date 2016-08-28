package com.blackzheng.me.piebald.data;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.util.DrawableUtil;

import net.qiujuer.genius.blur.StackBlur;

import java.util.Random;

/**
 * Created by BlackZheng on 2016/4/7.
 */
public class ImageCacheManager {
    // 取运行内存阈值的1/8作为图片缓存
    private static final int MEM_CACHE_SIZE = 1024 * 1024 * ((ActivityManager) App.getContext()
            .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() / 8;

    private static ImageLoader mImageLoader = new ImageLoader(RequestManager.mRequestQueue, new BitmapLruCache(
            MEM_CACHE_SIZE));

    private ImageCacheManager() {

    }

    public static ImageLoader.ImageContainer loadImage(String requestUrl,
                                                       ImageLoader.ImageListener imageListener) {
        return loadImage(requestUrl, imageListener, 0, 0);
    }

    public static ImageLoader.ImageContainer loadImage(String requestUrl,
                                                       ImageLoader.ImageListener imageListener, int maxWidth, int maxHeight) {
        return mImageLoader.get(requestUrl, imageListener, maxWidth, maxHeight);
    }

    public static ImageLoader.ImageListener getImageListener(final ImageView view,
                                                             final Drawable defaultImageDrawable, final Drawable errorImageDrawable) {
        return new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorImageDrawable != null) {
                    view.setImageDrawable(errorImageDrawable);
                }
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    if (!isImmediate && defaultImageDrawable != null) {
                        TransitionDrawable transitionDrawable = new TransitionDrawable(
                                new Drawable[]{
                                        defaultImageDrawable,
                                        new BitmapDrawable(App.getContext().getResources(),
                                                response.getBitmap())
                                }
                        );
                        transitionDrawable.setCrossFadeEnabled(true);
                        view.setImageDrawable(transitionDrawable);
                        transitionDrawable.startTransition(100);
                    } else {
                        view.setImageBitmap(response.getBitmap());
                    }
                } else if (defaultImageDrawable != null) {
                    view.setImageDrawable(defaultImageDrawable);
                }
            }
        };
    }

//    Using a TransitionDrawable with CircleImageView doesn't work properly and leads to messed up images.
    public static ImageLoader.ImageListener getProfileListener(final ImageView view,
                                                             final Drawable defaultImageDrawable, final Drawable errorImageDrawable) {
        return new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorImageDrawable != null) {
                    view.setImageDrawable(errorImageDrawable);
                }
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                        view.setImageBitmap(response.getBitmap());
                } else if (defaultImageDrawable != null) {
                    view.setImageDrawable(defaultImageDrawable);
                }
            }
        };
    }

    public static ImageLoader.ImageListener getProfileListenerWithBlur(final ImageView view,
                                                               final int defaultColor, final int errorColor, final View blur) {
        return new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorColor != 0) {
                    view.setImageResource(errorColor);
                    blur.setBackgroundResource(errorColor);
                }
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    Drawable[] drawables=new Drawable[2];
                    drawables[0] = new BitmapDrawable(StackBlur.blurNatively(response.getBitmap(), 20, false));
                    drawables[1] = new ColorDrawable(0x55000000);
                    view.setImageBitmap(response.getBitmap());
                    blur.setBackground(new LayerDrawable(drawables));

                } else if (defaultColor != 0) {
                    view.setImageResource(defaultColor);
//                    blur.setBackground(defaultImageDrawable);
                    blur.setBackgroundResource(defaultColor);
//                    Log.d("color", DrawableUtil.getDefaultColors()[new Random().nextInt(5)] + "");
//                    blur.setBackgroundColor(DrawableUtil.getDefaultColors()[new Random().nextInt(5)]);
                }
            }
        };
    }
}

