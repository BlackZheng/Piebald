package com.blackzheng.me.piebald.ui.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.blackzheng.me.piebald.dao.BaseDataHelper;
import com.blackzheng.me.piebald.dao.CollectionDataHelper;
import com.blackzheng.me.piebald.dao.UserCollectionsDataHelper;
import com.blackzheng.me.piebald.model.Collection;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.ui.CollectionActivity;
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
 * Created by BlackZheng on 2017/2/13.
 */

public class UserCollectionsFragment extends ContentFragment implements CollectionsListAdapter.OnItemClickLitener{

    private static final String TAG = LogHelper.makeLogTag(UserCollectionsFragment.class);

    public static UserCollectionsFragment newInstance(String category, String username, String userId) {
        UserCollectionsFragment usercollectionsfragment = new UserCollectionsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CATEGORY, category);
        bundle.putBoolean(EXTRA_ALLOW_REFRESH, false);
        bundle.putString(EXTRA_USERNAME, username);
        bundle.putString(EXTRA_USER_ID, userId);
        bundle.putBoolean(EXTRA_IS_LAZY_LOAD, true);
        usercollectionsfragment.setArguments(bundle);
        return usercollectionsfragment;
    }

    @Override
    protected void parseArgument() {
        Bundle bundle = getArguments();
        mCategory = bundle.getString(EXTRA_CATEGORY);
        allowRefresh = bundle.getBoolean(EXTRA_ALLOW_REFRESH, false);
        mUsername = bundle.getString(EXTRA_USERNAME);
        mUserId = bundle.getString(EXTRA_USER_ID);
        isLazyLoad = bundle.getBoolean(EXTRA_IS_LAZY_LOAD);
    }

    @Override
    protected int getLoaderID() {
        return 3;
    }

    @Override
    protected BaseAbstractRecycleCursorAdapter getAdapter() {
        CollectionsListAdapter adapter = new CollectionsListAdapter(getActivity(), null);
        adapter.setOnItemClickLitener(this);
        return adapter;
    }

    @Override
    protected BaseDataHelper getDataHelper() {
        return new UserCollectionsDataHelper(App.getContext(), mUserId);
    }

    @Override
    protected RecyclerView.LayoutManager reviewOnScreenChanged(Configuration newConfig) {
        return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    protected void getData(String category, final String page) {
        Log.d(TAG, "getData:" + category);
        Subscription subscription = UnsplashAPI.getInstance().getUnsplashService().getCollectionsByUser(mUsername, page, UnsplashAPI.CLIENT_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Func1<List<Collection>, List<Collection>>() {

                    @Override
                    public List<Collection> call(List<Collection> collections) {
                        LogHelper.d(TAG, "loadData: onNext count " + collections.size());
                        if(collections.size() > 0){
                            String newId = String.valueOf(collections.get(0).id);
                            if (mOldId != null && !mOldId.equals(newId)) {  //avoid loading the same content repeatedly
                                if(page.equals("1") || page.equals("0"))
                                    ((UserCollectionsDataHelper) mDataHelper).deleteAll();
                                ((UserCollectionsDataHelper) mDataHelper).bulkInsert(collections);
                            }
                        }
                        return collections;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Collection>>() {
                    @Override
                    public void call(List<Collection> collections) {
                        if(collections.size() < 10) {
                            View emptyView = list.getEmptyView();
                            emptyView.findViewById(R.id.empty_tip).setVisibility(View.VISIBLE);
                            emptyView.findViewById(R.id.progress).setVisibility(View.GONE);
                            list.removeMoreListener();
                            list.hideMoreProgress();
                        }
                    }
                }, ERRORACTION);
        addSubscription(subscription);
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
