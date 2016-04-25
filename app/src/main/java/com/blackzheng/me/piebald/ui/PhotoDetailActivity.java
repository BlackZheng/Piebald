package com.blackzheng.me.piebald.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.blackzheng.me.piebald.data.GsonRequest;
import com.blackzheng.me.piebald.data.ImageCacheManager;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.util.Decoder;
import com.blackzheng.me.piebald.util.Downloader;
import com.blackzheng.me.piebald.util.StringUtil;
import com.blackzheng.me.piebald.view.AdjustableImageView;
import com.google.gson.reflect.TypeToken;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class PhotoDetailActivity extends BaseActivity {
    private static final int[] COLORS = {R.color.holo_blue_light, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_purple_light, R.color.holo_red_light};
    private Resources mResource;
    private Drawable mDefaultImageDrawable;
    public static final String PHOTO_ID = "photo_id";
    public static final String DOWNLOAD_URL = "download_url";
    private String id;
    private String downloadUrl;
    private Photo detailed_photo;

    private Toolbar mToolbar;
    private AdjustableImageView photo;
    private CircleImageView profile;
    private TextView photo_by;
    private TextView location;
    private ImageButton download;
    private TextView make;
    private TextView model;
    private TextView aperture;
    private TextView exposure_time;
    private TextView focal_length;
    private TextView iso;
    private ImageLoader.ImageContainer photoRequest;
    private ImageLoader.ImageContainer profileRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_detail_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        initActionBar(mToolbar);
        mDefaultImageDrawable = new ColorDrawable(getResources().getColor(COLORS[new Random().nextInt(5)]));
        getIntentData();
        initView();
        detailed_photo = Photo.getFromCache(id);
        if (detailed_photo != null && detailed_photo.exif != null) {
            setView(detailed_photo);
        } else {

            executeRequest(new GsonRequest(String.format(UnsplashAPI.GET_SPECIFIC_PHOTO, id), new TypeToken<Photo>() {
            }.getType(),
                    responseListener(), errorListener()));
        }


    }

    private void initView() {
        photo = (AdjustableImageView) findViewById(R.id.photo);
        profile = (CircleImageView) findViewById(R.id.profile);
        photo_by = (TextView) findViewById(R.id.photo_by);
        location = (TextView) findViewById(R.id.location);
        make = (TextView) findViewById(R.id.make);
        model = (TextView) findViewById(R.id.model);
        aperture = (TextView) findViewById(R.id.aperture);
        exposure_time = (TextView) findViewById(R.id.exposure_time);
        focal_length = (TextView) findViewById(R.id.focal_length);
        iso = (TextView) findViewById(R.id.iso);
        download = (ImageButton) findViewById(R.id.download);
        if (downloadUrl != null)
            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Downloader.download(PhotoDetailActivity.this, downloadUrl, id);
                }
            });
    }

    private void getIntentData() {
        Intent intent = getIntent();
        id = intent.getStringExtra(PHOTO_ID);
        downloadUrl = intent.getStringExtra(DOWNLOAD_URL);
    }

    private void setView(final Photo detailed_photo) {
        if (detailed_photo.color != null)
            mDefaultImageDrawable = new ColorDrawable(Color.parseColor(detailed_photo.color));
        photoRequest = ImageCacheManager.loadImage(Decoder.decodeURL(detailed_photo.urls.small), ImageCacheManager
                .getImageListener(photo, mDefaultImageDrawable, mDefaultImageDrawable), 0, 0);
        profileRequest = ImageCacheManager.loadImage(Decoder.decodeURL(detailed_photo.user.profile_image.medium), ImageCacheManager
                .getProfileListener(profile, mDefaultImageDrawable, mDefaultImageDrawable), 0, 0);
        if (detailed_photo.user.name != null)
            photo_by.setText("By " + Decoder.decodeStr(detailed_photo.user.name));
        if (detailed_photo.location != null)
            location.setText("In " + Decoder.decodeStr(detailed_photo.location.city) + ", " + Decoder.decodeStr(detailed_photo.location.country));
        if (detailed_photo.exif.make != null)
            make.setText(detailed_photo.exif.make);
        if (detailed_photo.exif.model != null)
            model.setText(detailed_photo.exif.model);
        if (detailed_photo.exif.aperture != null)
            aperture.setText(StringUtil.shortenString(detailed_photo.exif.aperture));
        if (detailed_photo.exif.exposure_time != null)
            exposure_time.setText(StringUtil.shortenString(detailed_photo.exif.exposure_time) + "s");
        if (detailed_photo.exif.focal_length != null)
            focal_length.setText(StringUtil.checkFocalLength(detailed_photo.exif.focal_length) + "mm");
        iso.setText(detailed_photo.exif.iso + "");
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhotoDetailActivity.this, PhotoZoomingActivity.class);
                intent.putExtra(PhotoZoomingActivity.IMAGE_URL, detailed_photo.urls.regular);
                startActivity(intent);
            }
        });

    }

    protected Response.Listener<Photo> responseListener() {
//        final boolean isRefreshFromTop = ("1".equals(mPage));
        return new Response.Listener<Photo>() {
            @Override
            public void onResponse(final Photo response) {

                setView(response);
                Photo.addToCache(response);

            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (photoRequest != null) {
            photoRequest.cancelRequest();
        }
        if (profileRequest != null) {
            profileRequest.cancelRequest();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
