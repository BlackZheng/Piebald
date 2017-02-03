package com.blackzheng.me.piebald.ui;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.data.RequestManager;
import com.blackzheng.me.piebald.ui.adapter.DrawerAdapter;
import com.blackzheng.me.piebald.ui.fragment.CategoryContentFragment;
import com.blackzheng.me.piebald.ui.fragment.CollectionsFragment;
import com.blackzheng.me.piebald.ui.fragment.ContentFragment;
import com.blackzheng.me.piebald.ui.listener.OnDoubleClickListener;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends BaseActivity implements DrawerAdapter.OnItemClickLitener, OnDoubleClickListener{

    public static final String HEADER = "Header";
    public static final String DIVIDER = "Divider";
    public static final String NEW = "New";
    public static final String BUILDINGS = "Buildings";
    public static final String FOOD_AND_DRINK = "Food & Drink";
    public static final String NATURE = "Nature";
    public static final String PEOPLE = "People";
    public static final String TECHNOLOGY = "Technology";
    public static final String OBJECTS = "Objects";
    public static final String FEATURED = "Featured";
    public static final String CURATED = "Curated";

    //对应侧滑菜单的列表项
    private String[] mDrawerList = {
            HEADER,
            NEW,
            BUILDINGS,
            FOOD_AND_DRINK,
            NATURE,
            PEOPLE,
            TECHNOLOGY,
            OBJECTS,
            DIVIDER,
            FEATURED,
            CURATED
    };

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private RecyclerView drawerlist;
    private Toolbar mToolbar;
    private ContentFragment mContentFragment;
    private String mCategory = "New";//default category
    private FrameLayout splashView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSplashView();//初始化启动页面
        //请求权限
        MainActivityPermissionsDispatcher.requestPermissionWithCheck(this);
        mContentFragment = CategoryContentFragment.newInstance(mCategory);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, mContentFragment)
                .commit();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        registerDoubleClickListener(mToolbar, this);//给toolbar注册双击监听
        initActionBar(mToolbar);
        initDrawer();//初始化侧滑菜单

        getSupportActionBar().setTitle(mCategory);


    }

    private void initSplashView() {
        splashView = (FrameLayout) findViewById(R.id.splash_view);
        AlphaAnimation animation=new AlphaAnimation(1.0f,1.0f);
        animation.setDuration(2000);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                splashView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splashView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        splashView.setAnimation(animation);
    }

    @NeedsPermission(Manifest.permission.READ_PHONE_STATE)
    void requestPermission(){
        Log.d("Piebald", "Granted Photo Permission");
    }
    @OnShowRationale(Manifest.permission.READ_PHONE_STATE)
    void showRationaleForCamera(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.phone_permission_showRationale)
                .setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.button_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .show();
    }

    private void initDrawer(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_left);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        drawerlist = (RecyclerView) findViewById(R.id.drawer_list);
        drawerlist.setLayoutManager(new LinearLayoutManager(this));
        DrawerAdapter adapter = new DrawerAdapter(this, mDrawerList);
        adapter.setOnItemClickLitener(this);
        drawerlist.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RequestManager.cancelAll(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
        mContentFragment = CategoryContentFragment.newInstance(mCategory);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mContentFragment).commit();
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

    @Override
    public void OnSingleClick(View v) {

    }

    @Override
    public void OnDoubleClick(View v) {
        mContentFragment.getRecyclerView().smoothScrollToPosition(0);
    }
    //实现双击toolbar回到列表顶部
    public static void registerDoubleClickListener(View view, final OnDoubleClickListener listener){
        if(listener==null) return;
        view.setOnClickListener(new View.OnClickListener() {
            private static final int DOUBLE_CLICK_TIME = 350;        //双击间隔时间350毫秒
            private boolean waitDouble = true;

            private Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    listener.OnSingleClick((View)msg.obj);
                }

            };

            //等待双击
            public void onClick(final View v) {
                if(waitDouble){
                    waitDouble = false;        //与执行双击事件
                    new Thread(){

                        public void run() {
                            try {
                                Thread.sleep(DOUBLE_CLICK_TIME);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }    //等待双击时间，否则执行单击事件
                            if(!waitDouble){
                                //如果过了等待事件还是预执行双击状态，则视为单击
                                waitDouble = true;
                                Message msg = handler.obtainMessage();
                                msg.obj = v;
                                handler.sendMessage(msg);
                            }
                        }

                    }.start();
                }else{
                    waitDouble = true;
                    listener.OnDoubleClick(v);    //执行双击
                }
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        if(!mCategory.equals(mDrawerList[position])){ //如果是相同类别则不置换fragment
            mCategory = mDrawerList[position];
            if(position < 9){
                mContentFragment = CategoryContentFragment.newInstance(mCategory);
            }
            else
                mContentFragment = CollectionsFragment.newInstance(mCategory);
            getSupportActionBar().setTitle(mCategory);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, mContentFragment)
                    .commit();
        }
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
