package com.blackzheng.me.piebald.ui.fragment;

import android.os.Bundle;
import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.blackzheng.me.piebald.data.GsonRequest;
import com.blackzheng.me.piebald.model.Photo;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Created by BlackZheng on 2016/4/7.
 */
public class NewContentFragment extends ContentFragment{

    public static NewContentFragment newInstance(String category) {
        NewContentFragment newContentFragment = new NewContentFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CATEGORY, category);
        newContentFragment.setArguments(bundle);
        return newContentFragment;
    }
    @Override
    protected GsonRequest getRequest(String category, String page) {
        return new GsonRequest(String.format(UnsplashAPI.LIST_PHOTOS, page), new TypeToken<List<Photo>>(){}.getType(),
                responseListener(), errorListener());
    }

}
