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
import com.blackzheng.me.piebald.dao.ContentDataHelper;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.ui.MainActivity;
import com.blackzheng.me.piebald.ui.PhotoDetailActivity;
import com.blackzheng.me.piebald.ui.UserPageActivity;
import com.blackzheng.me.piebald.ui.adapter.BaseAbstractRecycleCursorAdapter;
import com.blackzheng.me.piebald.ui.adapter.ContentAdapter;
import com.blackzheng.me.piebald.util.Constants;
import com.blackzheng.me.piebald.util.LogHelper;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by BlackZheng on 2016/4/7.
 */
public class CategoryContentFragment extends ContentFragment implements ContentAdapter.OnItemClickLitener{

    private static final String TAG = LogHelper.makeLogTag(CategoryContentFragment.class);
    public static final int TYPE_PHOTO = 0x0101;
    public static final int TYPE_PROFILE = 0x0102;

    public static CategoryContentFragment newInstance(String category) {
        CategoryContentFragment categoryContentFragment = new CategoryContentFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CATEGORY, category);
        bundle.putBoolean(EXTRA_ALLOW_REFRESH, true);
        bundle.putBoolean(EXTRA_IS_LAZY_LOAD, false);
        categoryContentFragment .setArguments(bundle);
        return categoryContentFragment ;
    }

    @Override
    protected BaseAbstractRecycleCursorAdapter getAdapter() {
        ContentAdapter adapter = new ContentAdapter(getActivity(), null, R.layout.recycler_item);
        adapter.setOnItemClickLitener(this);
        return adapter;
    }

    @Override
    protected BaseDataHelper getDataHelper() {
        return  new ContentDataHelper(App.getContext(), mCategory);
    }

    @Override
    protected RecyclerView.LayoutManager reviewOnScreenChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            //竖屏
            return new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        }
    }


    @Override
    protected void getData(String category, String page) {
        Log.d(TAG, "getData:" + category);
        Subscription subscription = getObservable(category, page)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Func1<List<Photo>, List<Photo>>() {

                    @Override
                    public List<Photo> call(List<Photo> photos) {
                        Log.d(TAG, "call:" + photos);
                        String newId = "newId";
                        if(!photos.isEmpty())
                            newId = photos.get(0).id;
                        else
                            return photos;
                        if (mOldId != null && !mOldId.equals(newId)) {  //avoid loading the same content repeatedly
                            if (isRefreshFromTop) {
                                ((ContentDataHelper) mDataHelper).deleteAll();
                            }
                            ((ContentDataHelper) mDataHelper).bulkInsert(photos);
                        }
                        return photos;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Photo>>() {
                    @Override
                    public void call(List<Photo> photos) {
                        if(photos.isEmpty())
                            list.hideMoreProgress();
                        if (isRefreshFromTop) {
                            setRefreshFromTop(false);
                            setRefreshing(false);
                        }
                    }
                }, ERRORACTION);
        addSubscription(subscription);
    }

    protected Observable<List<Photo>> getObservable(String category, String page) {
        Log.d(TAG, "getObservable:" + category);
        if(category.equals(MainActivity.LATEST))
            return UnsplashAPI.getInstance().getUnsplashService().getLatestPhotos(page, UnsplashAPI.CLIENT_ID);
        else
            return UnsplashAPI.getInstance().getUnsplashService().getPhotosByCategory(String.valueOf(ContentDataHelper.CATEGORY_ID.get(mCategory)), page, UnsplashAPI.CLIENT_ID);
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
        return 0;
    }

    /*
    * Callback for Item Clicked
    */
    @Override
    public void onItemClick(View view, Photo photo, int position, int type) {
        Intent intent = null;
        LogHelper.d(TAG, "onItem Click: " + type);
        switch (type){
            case Constants.TYPE_PHOTO:
                intent = new Intent(getActivity(), PhotoDetailActivity.class);
                intent.putExtra(PhotoDetailActivity.PHOTO_ID, photo.id);
                intent.putExtra(PhotoDetailActivity.DOWNLOAD_URL, photo.links.download);
                startActivity(intent);
                break;
            case Constants.TYPE_PROFILE:
                intent = new Intent(getActivity(), UserPageActivity.class);
                intent.putExtra(UserPageActivity.USERNAME, photo.user.username);
                intent.putExtra(UserPageActivity.USER_ID, photo.user.id);
                intent.putExtra(UserPageActivity.NAME, photo.user.name);
                intent.putExtra(UserPageActivity.PROFILE_IMAGE_URL, photo.user.profile_image.large);
                startActivity(intent);
                break;
            default:
                break;
        }

    }

    @Override
    public void onItemLongClick(View view, Photo photo, int position, int type) {

    }
}
