package com.blackzheng.me.piebald.ui.fragment;

import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.dao.BaseDataHelper;
import com.blackzheng.me.piebald.ui.adapter.BaseAbstractRecycleCursorAdapter;
import com.blackzheng.me.piebald.util.LogHelper;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.umeng.analytics.MobclickAgent;

import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

/**
 * Created by BlackZheng on 2016/4/6.
 */
public abstract class ContentFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<Cursor>, OnMoreListener {

    private static final String TAG = LogHelper.makeLogTag(ContentFragment.class);
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_ALLOW_REFRESH = "extra_allow_refresh";
    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_IS_LAZY_LOAD = "extra_is_lazy_load";
    public static final String NOT_OLD_ID = "not_old_id";

    protected String mOldId;//avoid loading the same content repeatedly
    protected int mCurrentPage = 1;
    protected String mCategory;
    protected boolean isRefreshFromTop;
    protected boolean allowRefresh;
    protected String mUserId;
    protected String mUsername;
    protected boolean isLazyLoad;
    protected boolean isPrepared = true;

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
        LogHelper.d(TAG, "onCreateView:" + getClass().getName());
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
        list.setupMoreListener(this, 1);

        if(allowRefresh) {
            list.setRefreshingColorResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light,
                    android.R.color.holo_green_light, android.R.color.holo_red_light);
            list.setRefreshListener(this);
        }
        isPrepared = false;
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        LogHelper.d(TAG, "isVisibleToUser: " + isVisibleToUser);
        if(isVisibleToUser && !isPrepared){
            startLoading();
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void startLoading(){
        LogHelper.d(TAG, "startLoading");
        getLoaderManager().initLoader(getLoaderID(), null, this);
        loadFirst();
        isPrepared = true;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(!isLazyLoad || getUserVisibleHint())
            startLoading();
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

    /*
    *   Method for loading
    */
    private void loadData(int next) {
        if (allowRefresh && !list.getSwipeToRefresh().isRefreshing() && next == 1 ) {
            setRefreshing(true);
        }
        getData(mCategory, String.valueOf(next));
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
        Log.d("ContentFragment", mCurrentPage + "");
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
     * 获取参数
     */
    protected  abstract void parseArgument();

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

    protected  abstract void getData(String category, String page);
}