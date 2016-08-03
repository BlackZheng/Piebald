package com.blackzheng.me.piebald.ui;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.util.ShareBitmapHolder;
import com.blackzheng.me.piebald.util.ShareImgToWX;

public class ShareSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
        setContentView(R.layout.activity_share_select);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.BOTTOM);


    }
    public void shareToWeChat(View view){
        ShareImgToWX.getInstance().shareToWeChat(ShareBitmapHolder.getmBitmap());
        finish();
    }
    public void shareToTimeline(View view){
        ShareImgToWX.getInstance().shareToTimeline(ShareBitmapHolder.getmBitmap());
        finish();
    }
}
