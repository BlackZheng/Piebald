package com.blackzheng.me.piebald.ui.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.BaseAdapter;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.dao.BaseDataHelper;
import com.blackzheng.me.piebald.dao.ContentDataHelper;
import com.blackzheng.me.piebald.data.GsonRequest;
import com.blackzheng.me.piebald.model.BaseModel;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.ui.PhotoDetailActivity;
import com.blackzheng.me.piebald.ui.adapter.BaseAbstractRecycleCursorAdapter;
import com.blackzheng.me.piebald.ui.adapter.ContentAdapter;
import com.blackzheng.me.piebald.util.TaskUtils;
import com.blackzheng.me.piebald.util.ToastUtils;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import yalantis.com.sidemenu.interfaces.ScreenShotable;

/**
 * Created by BlackZheng on 2016/4/6.
 */
public abstract class ContentFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<Cursor>, OnMoreListener {

    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String NOT_OLD_ID = "not_old_id";

    protected String mOldId;//avoid loading the same content repeatedly
    protected int mCurrentPage = 1;
    protected String mCategory;
    protected boolean isRefreshFromTop;

    protected int res;
    protected SuperRecyclerView list;
    protected BaseAbstractRecycleCursorAdapter mAdapter;
    protected BaseDataHelper mDataHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOldId = NOT_OLD_ID;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.super_recyclerview, container, false);
        parseArgument();
        //初始化adapter
        mDataHelper = getDataHelper();
        mAdapter = getAdapter();
        SlideInBottomAnimationAdapter animAdapter = new SlideInBottomAnimationAdapter(mAdapter);
        animAdapter.setDuration(800);
        animAdapter.setInterpolator(new AccelerateInterpolator());

        //初始化recycleview
        list = (SuperRecyclerView) rootView.findViewById(R.id.list);
        list.setLayoutManager(reviewOnScreenChanged(getResources().getConfiguration()));
        list.setAdapter(animAdapter);
        list.setRefreshingColorResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light,
                android.R.color.holo_green_light, android.R.color.holo_red_light);
        list.setupMoreListener(this, 1);
        list.setRefreshListener(this);

        getLoaderManager().initLoader(getLoaderID(), null, this);
        loadFirst();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    private void parseArgument() {
        Bundle bundle = getArguments();
        mCategory = bundle.getString(EXTRA_CATEGORY);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mCategory);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mCategory);
    }

    /**
     * 由子类实现
     * @return
     */
    protected abstract Response.Listener responseListener() ;

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtils.showShort(R.string.loading_failed);
                setRefreshing(false);
            }
        };
    }

    /*
    *   Method for loading
    */
    private void loadData(int next) {
        if (!list.getSwipeToRefresh().isRefreshing() && next == 1 ) {
            setRefreshing(true);
        }
        executeRequest(getRequest(mCategory, next));
    }

    private void loadFirst() {
        setRefreshFromTop(true);
        loadData(1);
    }

    private void loadNext() {
        loadData(mCurrentPage);
    }
    public void loadFirstAndScrollToTop() {
        // TODO: gridView scroll to top
        loadFirst();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mDataHelper.getCursorLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCurrentPage = data.getCount() / 10 + 1;// Make the currentPage increase everytime loading is finished
        if (data.getCount() != 0 && data.moveToFirst()) {

            mOldId = data.getString(1);

        }
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }


    @Override
    public void onRefresh() {
        loadFirst();
    }

    @Override
    public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
        loadData(mCurrentPage);
    }

    /*
    *   Change refreshing state
    */
    protected void setRefreshing(boolean refreshing) {
        list.getSwipeToRefresh().setRefreshing(refreshing);
    }

    protected void setRefreshFromTop(boolean flag) {
        isRefreshFromTop = flag;
    }


    public RecyclerView getRecyclerView(){
        return list.getRecyclerView();
    }

    /**
     * 不同类别的fragment处理不同类型的Bean类，需要有不同的LoaderID，否则会崩溃
     * @return
     */
    protected abstract int getLoaderID();

    /**
     * 根据子类的具体实现返回制定类型的Adapter
     * @return
     */
    protected abstract BaseAbstractRecycleCursorAdapter getAdapter();

    /**
     * 根据子类的具体实现返回制定类型的DataHelper
     * @return
     */
    protected abstract BaseDataHelper getDataHelper();

    /**
     * 每次初始化Fragment时检查当前配置是竖屏还是横屏
     * @param newConfig
     * @return
     */
    protected abstract RecyclerView.LayoutManager reviewOnScreenChanged(Configuration newConfig);

    /**
     * 根据子类的具体类型返回具体请求类型
     * @param category
     * @param page
     * @return
     */
    protected abstract GsonRequest getRequest(String category, int page);
}