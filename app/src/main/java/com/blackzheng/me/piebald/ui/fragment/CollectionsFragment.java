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
import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.blackzheng.me.piebald.dao.BaseDataHelper;
import com.blackzheng.me.piebald.dao.CollectionDataHelper;
import com.blackzheng.me.piebald.dao.ContentDataHelper;
import com.blackzheng.me.piebald.data.GsonRequest;
import com.blackzheng.me.piebald.model.Collection;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.ui.CollectionActivity;
import com.blackzheng.me.piebald.ui.MainActivity;
import com.blackzheng.me.piebald.ui.adapter.BaseAbstractRecycleCursorAdapter;
import com.blackzheng.me.piebald.ui.adapter.CollectionsListAdapter;
import com.blackzheng.me.piebald.util.TaskUtils;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Created by BlackZheng on 2016/8/27.
 */
public class CollectionsFragment extends ContentFragment implements CollectionsListAdapter.OnItemClickLitener {

    public static CollectionsFragment newInstance(String category) {
        CollectionsFragment categoryContentFragment = new CollectionsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CATEGORY, category);
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
    protected GsonRequest getRequest(String category, int page) {
        return new GsonRequest(String.format(UnsplashAPI.GET_CURATED, category.toLowerCase(), String.valueOf(page)), new TypeToken<List<Collection>>(){}.getType(),
                responseListener(), errorListener());
    }

    @Override
    protected Response.Listener<List<Collection>> responseListener() {
        return new Response.Listener<List<Collection>>() {
            @Override
            public void onResponse(final List<Collection> response) {
                if(response.size() < 1){    //已经拉到页尾了，隐藏进度圈
                    list.hideMoreProgress();
                    return;
                }
                final String newId = String.valueOf(response.get(0).id);
                TaskUtils.executeAsyncTask(new AsyncTask<Object, Object, Object>() {

                    @Override
                    protected Object doInBackground(Object... params) {
                        if (mOldId != null && !mOldId.equals(newId)) {  //avoid loading the same content repeatedly
                            if (isRefreshFromTop) {
                                ((CollectionDataHelper)mDataHelper).deleteAll();
                            }
                            ((CollectionDataHelper)mDataHelper).bulkInsert(response);
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
        return 1;
    }

    @Override
    public void onItemClick(View view, Collection collection, int position) {
        Intent intent = new Intent(getActivity(), CollectionActivity.class);
        intent.putExtra(CollectionActivity.ID, collection.id);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, Collection collection, int position) {

    }
}
