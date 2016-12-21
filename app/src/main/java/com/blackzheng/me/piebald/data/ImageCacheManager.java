package com.blackzheng.me.piebald.data;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.widget.ImageView;


import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.qiujuer.genius.blur.StackBlur;

/**
 * Created by BlackZheng on 2016/4/7.
 */
public class ImageCacheManager {
    // 取运行内存阈值的1/8作为图片缓存
    private static final int MEM_CACHE_SIZE = 1024 * 1024 * ((ActivityManager) App.getContext()
            .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() / 8;

    private ImageCacheManager() {

    }

    public static void loadImage(String url, ImageView imageView, Drawable drawableOnLoading){
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(drawableOnLoading)
                .showImageOnFail(R.drawable.error_drawable)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(false)
                .build();
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(url, imageView, options);
    }

    public static void loadImageWithBlur(String url, ImageView imageView, final int defaultColor, final View blurView){
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultColor)
                .showImageOnFail(defaultColor)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(false)
                .build();
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(url, imageView, options, new SimpleImageLoadingListener(){
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
                blurView.setBackgroundResource(defaultColor);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                Drawable[] drawables=new Drawable[2];
                drawables[0] = new BitmapDrawable(null, StackBlur.blurNatively(loadedImage, 20, false));
                drawables[1] = new ColorDrawable(0x55000000);
                blurView.setBackground(new LayerDrawable(drawables));
            }
        });
    }

    public static void cancelDisplayingTask(ImageView imageView){
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().cancelDisplayTask(imageView);
    }

}

