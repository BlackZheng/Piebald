package com.blackzheng.me.piebald.ui.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.blackzheng.me.piebald.dao.BaseDataHelper;
import com.blackzheng.me.piebald.dao.UserAlbumDataHelper;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.ui.PhotoDetailActivity;
import com.blackzheng.me.piebald.ui.adapter.BaseAbstractRecycleCursorAdapter;
import com.blackzheng.me.piebald.ui.adapter.UserPhotosAdapter;
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

public class UserPhotosFragment extends ContentFragment implements UserPhotosAdapter.OnItemClickLitener{

    private static final String TAG = LogHelper.makeLogTag(UserPhotosFragment.class);

    public static UserPhotosFragment newInstance(String category, String username) {
        UserPhotosFragment userPhotosFragment = new UserPhotosFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CATEGORY, category);
        bundle.putBoolean(EXTRA_ALLOW_REFRESH, false);
        bundle.putString(EXTRA_USERNAME, username);
        bundle.putBoolean(EXTRA_IS_LAZY_LOAD, true);
        userPhotosFragment.setArguments(bundle);
        return userPhotosFragment;
    }

    @Override
    protected void parseArgument() {
        Bundle bundle = getArguments();
        mCategory = bundle.getString(EXTRA_CATEGORY);
        allowRefresh = bundle.getBoolean(EXTRA_ALLOW_REFRESH, false);
        mUsername = bundle.getString(EXTRA_USERNAME);
        isLazyLoad = bundle.getBoolean(EXTRA_IS_LAZY_LOAD);
    }

    @Override
    protected int getLoaderID() {
        return 2;
    }

    @Override
    protected BaseAbstractRecycleCursorAdapter getAdapter() {
        UserPhotosAdapter adapter = new UserPhotosAdapter(getActivity(), null);
        adapter.setOnItemClickLitener(this);
        return adapter;
    }

    @Override
    protected BaseDataHelper getDataHelper() {
        return new UserAlbumDataHelper(App.getContext(), mUsername);
    }

    @Override
    protected RecyclerView.LayoutManager reviewOnScreenChanged(Configuration newConfig) {
        return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    protected void getData(String category, final String page) {
        Subscription subscription = UnsplashAPI.getInstance().getUnsplashService().getPhotosByUser(mUsername, Integer.valueOf(page), UnsplashAPI.CLIENT_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Func1<List<Photo>, List<Photo>>() {
                    @Override
                    public List<Photo> call(List<Photo> photos) {
                        LogHelper.d(TAG, "loadData: onNext count " + photos.size());
                        if(photos.size() > 0){
                            String newId = photos.get(0).id;
                            if (mOldId != null && !mOldId.equals(newId)) {  //avoid loading the same content repeatedly
                                if(page.equals("1") || page.equals("0"))
                                    ((UserAlbumDataHelper) mDataHelper).deleteAll();
                                ((UserAlbumDataHelper) mDataHelper).bulkInsert(photos);
                            }
                        }
                        return photos;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Photo>>() {
                    @Override
                    public void call(List<Photo> photos) {
                        if(photos.size() < 10) {
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
    public void onItemClick(View view, Photo photo, int position) {
        Intent intent = new Intent(getActivity(), PhotoDetailActivity.class);
        intent.putExtra(PhotoDetailActivity.PHOTO_ID, photo.id);
        intent.putExtra(PhotoDetailActivity.DOWNLOAD_URL, photo.links.download);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, Photo photo, int position) {

    }
}
