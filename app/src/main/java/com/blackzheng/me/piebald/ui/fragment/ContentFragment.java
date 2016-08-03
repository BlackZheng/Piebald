package com.blackzheng.me.piebald.ui.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
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

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.dao.ContentDataHelper;
import com.blackzheng.me.piebald.data.GsonRequest;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.ui.PhotoDetailActivity;
import com.blackzheng.me.piebald.ui.adapter.ContentAdapter;
import com.blackzheng.me.piebald.util.TaskUtils;
import com.blackzheng.me.piebald.util.ToastUtils;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import java.util.List;

import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import yalantis.com.sidemenu.interfaces.ScreenShotable;

/**
 * Created by BlackZheng on 2016/4/6.
 */
public abstract class ContentFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, ScreenShotable,
        LoaderManager.LoaderCallbacks<Cursor>, OnMoreListener, ContentAdapter.OnItemClickLitener {

    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String NOT_OLD_ID = "not_old_id";
    public static final String CLOSE = "Close";
    public static final String NEW = "New";
    public static final String BUILDINGS = "Buildings";
    public static final String FOOD_AND_DRINK = "Food & Drink";
    public static final String NATURE = "Nature";
    public static final String PEOPLE = "People";
    public static final String TECHNOLOGY = "Technology";
    public static final String OBJECTS = "Objects";


    private String mOldId;//avoid loading the same content repeatedly
    private int mCurrentPage = 1;
    protected String mCategory;
    private boolean isRefreshFromTop;

    protected int res;
    private View containerView;
    protected SuperRecyclerView list;
    private Bitmap bitmap;
    private ContentAdapter mAdapter;
    private ContentDataHelper mDataHelper;




    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.containerView = view.findViewById(R.id.container);
    }

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

        //initialize adapter
        mDataHelper = new ContentDataHelper(App.getContext(), mCategory);
        mAdapter = new ContentAdapter(getActivity(), null);
        mAdapter.setOnItemClickLitener(this);
        SlideInBottomAnimationAdapter animAdapter = new SlideInBottomAnimationAdapter(mAdapter);
        animAdapter.setDuration(800);
        animAdapter.setInterpolator(new AccelerateInterpolator());

        //initialize recycleview
        list = (SuperRecyclerView) rootView.findViewById(R.id.list);
        reviewOnScreenChanged(getResources().getConfiguration());
        list.setAdapter(animAdapter);
        list.setRefreshingColorResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light,
                android.R.color.holo_green_light, android.R.color.holo_red_light);
        list.setupMoreListener(this, 1);
        list.setRefreshListener(this);

        getLoaderManager().initLoader(0, null, this);
        loadFirst();
        return rootView;
    }

    private void parseArgument() {
        Bundle bundle = getArguments();
        mCategory = bundle.getString(EXTRA_CATEGORY);
    }


    /*
    *  Listeners for Volley requests
    */
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
                                mDataHelper.deleteAll();
                            }
                            mDataHelper.bulkInsert(response);
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


    /*
    * Callback for Loaders
    */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mDataHelper.getCursorLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCurrentPage = data.getCount() / 10 + 1;// Make the currentPage increase everytime loading is finished
//        mPage = String.valueOf(data.getCount() / 10 + 1);
        if (data.getCount() != 0 && data.moveToFirst()) {

            mOldId = data.getString(1);

        }
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
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


    /*
    * Callback for Loading Picture
    */
    @Override
    public void onRefresh() {
        loadFirst();
    }

    @Override
    public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
//        loadNext();
        loadData(mCurrentPage);
    }


    /*
    *   Change refreshing state
    */
    private void setRefreshing(boolean refreshing) {
        list.getSwipeToRefresh().setRefreshing(refreshing);
    }

    private void setRefreshFromTop(boolean flag) {
        isRefreshFromTop = flag;
    }


    /*
    * Method about views
    */
    public RecyclerView getRecyclerView(){
        return list.getRecyclerView();
    }

    @Override
    public void takeScreenShot() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
                        containerView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                containerView.draw(canvas);
                ContentFragment.this.bitmap = bitmap;
            }
        });
/*
       The code below will throw Exception:"Only the original thread that created a view hierarchy can touch its view."
*/
//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//                Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
//                        containerView.getHeight(), Bitmap.Config.ARGB_8888);
//                Canvas canvas = new Canvas(bitmap);
//                containerView.draw(canvas);
//                ContentFragment.this.bitmap = bitmap;
//            }
//        };
//
//        thread.start();
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }


    /*
    *   Check orientation to determine RecycleView Layout
    */
    private void reviewOnScreenChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            list.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        } else {
            //竖屏
            list.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        }
    }

    //Overrided by Child
    protected abstract GsonRequest getRequest(String category, int page);
}