package com.blackzheng.me.piebald.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.util.Decoder;
import com.blackzheng.me.piebald.util.LogHelper;
import com.blackzheng.me.piebald.util.ShareBitmapHolder;
import com.blackzheng.me.piebald.view.HideableToolbar;
import com.github.lzyzsd.circleprogress.CircleProgress;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.umeng.analytics.MobclickAgent;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by BlackZheng on 2016/4/15.
 */
public class PhotoZoomingActivity extends BaseActivity {

    private static final String TAG = LogHelper.makeLogTag(PhotoZoomingActivity.class);

    public static final String IMAGE_URL = "image_url";

    private HideableToolbar toolbar;

    private PhotoView photoView;

    private CircleProgress progress;

    private boolean isHide = false;

    private PhotoViewAttacher mAttacher;

    private boolean canShare = false;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_zooming_layout);
        toolbar = (HideableToolbar) findViewById(R.id.toolbar);
        initActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        photoView = (PhotoView) findViewById(R.id.photoView);
        progress = (CircleProgress) findViewById(R.id.progress);
        mAttacher = new PhotoViewAttacher(photoView);
        mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                if(isHide){
                    toolbar.show();
                    isHide = false;
                }
                else{
                    toolbar.hide();
                    isHide = true;
                }
            }

            @Override
            public void onOutsidePhotoTap() { finish(); }
        });

        String imageUrl = Decoder.decodeURL(getIntent().getStringExtra(IMAGE_URL));
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisk(true)
                .considerExifParams(false).build();

        ImageLoader.getInstance().displayImage(imageUrl, photoView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progress.setVisibility(View.GONE);
                mAttacher.update();
                canShare = true;
                invalidateOptionsMenu();
                ShareBitmapHolder.setBitmap(loadedImage);
            }
        }, new ImageLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                progress.setProgress(100 * current / total);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAttacher != null) {
            mAttacher.cleanup();
        }
        ShareBitmapHolder.recycleBitmap();
        ImageLoader.getInstance().cancelDisplayTask(photoView);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.share).setEnabled(canShare);
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share, menu);
        menu.findItem(R.id.share).setEnabled(canShare);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.share) {
            Intent intent = new Intent(this, ShareSelectActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
