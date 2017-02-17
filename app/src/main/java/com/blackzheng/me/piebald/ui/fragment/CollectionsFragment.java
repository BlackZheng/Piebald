package com.blackzheng.me.piebald.ui.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.blackzheng.me.piebald.dao.BaseDataHelper;
import com.blackzheng.me.piebald.dao.CollectionDataHelper;
import com.blackzheng.me.piebald.model.Collection;
import com.blackzheng.me.piebald.ui.CollectionActivity;
import com.blackzheng.me.piebald.ui.MainActivity;
import com.blackzheng.me.piebald.ui.adapter.BaseAbstractRecycleCursorAdapter;
import com.blackzheng.me.piebald.ui.adapter.CollectionsListAdapter;
import com.blackzheng.me.piebald.util.LogHelper;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by BlackZheng on 2016/8/27.
 */
public class CollectionsFragment extends ContentFragment implements CollectionsListAdapter.OnItemClickLitener {

    private static final String TAG = LogHelper.makeLogTag(CollectionsFragment.class);

    public static CollectionsFragment newInstance(String category) {
        CollectionsFragment categoryContentFragment = new CollectionsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CATEGORY, category);
        bundle.putBoolean(EXTRA_ALLOW_REFRESH, true);
        bundle.putBoolean(EXTRA_IS_LAZY_LOAD, false);
        categoryContentFragment .setArguments(bundle);
        return categoryContentFragment ;
    }

    @Override
    protected BaseAbstractRecycleCursorAdapter getAdapter() {
        CollectionsListAdapter adapter = new CollectionsListAdapter(getActivity(), null);
        adapter.setOnItemClickLitener(this);
        return adapter;
    }

    @Override
    protected BaseDataHelper getDataHelper() {
        boolean isCurated = false;
        if(mCategory.equals(MainActivity.CURATED))
            isCurated = !isCurated;
        return new CollectionDataHelper(App.getContext(), isCurated);
    }

    @Override
    protected RecyclerView.LayoutManager reviewOnScreenChanged(Configuration newConfig) {
        //不管横竖屏都是瀑布流
        return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    }


    @Override
    protected void getData(String category, String page) {
        Log.d(TAG, "getData:" + category);
        Subscription subscription = UnsplashAPI.getInstance().getUnsplashService().getCollections(CollectionDataHelper.COLLECTION_TYPE.get(category), page, UnsplashAPI.CLIENT_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Func1<List<Collection>, List<Collection>>() {

            @Override
            public List<Collection> call(List<Collection> collections) {
                String newId = "newId";
                if(!collections.isEmpty())
                    newId = String.valueOf(collections.get(0).id);
                else
                    return collections;
                if (mOldId != null && !mOldId.equals(newId)) {  //avoid loading the same content repeatedly
                    if (isRefreshFromTop) {
                        ((CollectionDataHelper) mDataHelper).deleteAll();
                    }
                    ((CollectionDataHelper) mDataHelper).bulkInsert(collections);
                }
                return collections;
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Collection>>() {
                    @Override
                    public void call(List<Collection> collections) {
                        if(collections.isEmpty())
                            list.hideMoreProgress();
                        if (isRefreshFromTop) {
                            setRefreshFromTop(false);
                            setRefreshing(false);
                        }
                    }
                }, ERRORACTION);
        addSubscription(subscription);
    }

    @Override
    protected void parseArgument() {
        Bundle bundle = getArguments();
        mCategory = bundle.getString(EXTRA_CATEGORY);
        allowRefresh = bundle.getBoolean(EXTRA_ALLOW_REFRESH, false);
        isLazyLoad = bundle.getBoolean(EXTRA_IS_LAZY_LOAD);
    }

    @Override
    protected int getLoaderID() {
        return 1;
    }

    @Override
    public void onItemClick(View view, Collection collection, int position) {
        Intent intent = new Intent(getActivity(), CollectionActivity.class);
        intent.putExtra(CollectionActivity.ID, String.valueOf(collection.id));
        intent.putExtra(CollectionActivity.IS_CURATED, String.valueOf(collection.curated));
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, Collection collection, int position) {

    }
}
