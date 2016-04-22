package com.blackzheng.me.piebald.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.util.Decoder;
import com.github.lzyzsd.circleprogress.CircleProgress;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by BlackZheng on 2016/4/15.
 */
public class PhotoZoomingActivity extends BaseActivity {
    public static final String IMAGE_URL = "image_url";

    PhotoView photoView;

    CircleProgress progress;

    private PhotoViewAttacher mAttacher;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_zooming_layout);
        photoView = (PhotoView) findViewById(R.id.photoView);
        progress = (CircleProgress) findViewById(R.id.progress);
        mAttacher = new PhotoViewAttacher(photoView);
        mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                finish();
            }

            @Override
            public void onOutsidePhotoTap() { finish(); }
        });

        String imageUrl = Decoder.decodeURL(getIntent().getStringExtra(IMAGE_URL));
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc(true)
                .considerExifParams(true).build();
        ImageLoader.getInstance().displayImage(imageUrl, photoView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progress.setVisibility(View.GONE);
                mAttacher.update();
            }
        }, new ImageLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String imageUri, View view, int current, int total) {

                progress.setProgress(100 * current / total);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAttacher != null) {
            mAttacher.cleanup();
        }
    }
}
