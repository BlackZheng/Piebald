package com.blackzheng.me.piebald.ui.fragment;

import android.os.Bundle;

import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.blackzheng.me.piebald.dao.ContentDataHelper;
import com.blackzheng.me.piebald.data.GsonRequest;
import com.blackzheng.me.piebald.model.Photo;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Created by BlackZheng on 2016/4/7.
 */
public class CategoryContentFragment extends ContentFragment {

    public static CategoryContentFragment newInstance(String category) {
        CategoryContentFragment categoryContentFragment = new CategoryContentFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CATEGORY, category);
        categoryContentFragment .setArguments(bundle);
        return categoryContentFragment ;
    }
    @Override
    protected GsonRequest getRequest(String category, String page) {
        return new GsonRequest(String.format(UnsplashAPI.GET_PHOTOS_BY_CATEGORY, ContentDataHelper.CATEGORY_ID.get(mCategory), page), new TypeToken<List<Photo>>(){}.getType(),
                responseListener(), errorListener());
    }
}
