package com.blackzheng.me.piebald.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.util.LogHelper;

public class AboutActivity extends BaseActivity {
    private static final String TAG = LogHelper.makeLogTag(AboutActivity.class);
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        initActionBar(mToolbar);
    }
    public void developer(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://weibo.com/3557851780"));
        startActivity(intent);
    }
    public void email(View view){
//        Intent intent=new Intent(Intent.ACTION_SENDTO);
//        intent.setData(Uri.parse("mailto:blackzheng22@gmail.com"));
//        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
//        intent.putExtra(Intent.EXTRA_TEXT, "Text");
//        startActivity(intent);
    }
    public void github(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/BlackZheng"));
        startActivity(intent);
    }
    public void donate(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://qr.alipay.com/apx02931b9d8jlofd8euv47"));
        startActivity(intent);
    }
}
