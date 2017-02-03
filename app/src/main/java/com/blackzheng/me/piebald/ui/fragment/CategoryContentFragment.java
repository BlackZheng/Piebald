package com.blackzheng.me.piebald.ui.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.blackzheng.me.piebald.dao.BaseDataHelper;
import com.blackzheng.me.piebald.dao.ContentDataHelper;
import com.blackzheng.me.piebald.data.GsonRequest;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.ui.PhotoDetailActivity;
import com.blackzheng.me.piebald.ui.adapter.BaseAbstractRecycleCursorAdapter;
import com.blackzheng.me.piebald.ui.adapter.ContentAdapter;
import com.blackzheng.me.piebald.util.TaskUtils;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Created by BlackZheng on 2016/4/7.
 */
public class CategoryContentFragment extends ContentFragment implements ContentAdapter.OnItemClickLitener{

    public static CategoryContentFragment newInstance(String category) {
        CategoryContentFragment categoryContentFragment = new CategoryContentFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CATEGORY, category);
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
    protected GsonRequest getRequest(String category, int page) {

        if(category.equals("New"))
            return new GsonRequest(String.format(UnsplashAPI.LIST_PHOTOS, String.valueOf(page)), new TypeToken<List<Photo>>(){}.getType(),
                    responseListener(), errorListener());
        else
            return new GsonRequest(String.format(UnsplashAPI.GET_PHOTOS_BY_CATEGORY, ContentDataHelper.CATEGORY_ID.get(mCategory), String.valueOf(page)), new TypeToken<List<Photo>>(){}.getType(),
                    responseListener(), errorListener());
    }

    @Override
    protected Response.Listener<List<Photo>> responseListener() {

        return new Response.Listener<List<Photo>>() {
            @Override
            public void onResponse(final List<Photo> response) {
                final String newId = response.get(0).id;
                TaskUtils.executeAsyncTask(new AsyncTask<Object, Object, Object>() {

                    @Override
                    protected Object doInBackground(Object... params) {
                        if (mOldId != null && !mOldId.equals(newId)) {  //avoid loading the same content repeatedly
                            if (isRefreshFromTop) {
                                ((ContentDataHelper)mDataHelper).deleteAll();
                            }
                            ((ContentDataHelper)mDataHelper).bulkInsert(response);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        if (isRefreshFromTop) {
                            setRefreshFromTop(false);
                            setRefreshing(false);
                        }
                    }
                });

            }
        };
    }

    @Override
    protected int getLoaderID() {
        return 0;
    }

    /*
    * Callback for Item Clicked
    */
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
