package com.blackzheng.me.piebald.ui.fragment;

import android.support.v4.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.data.RequestManager;
import com.blackzheng.me.piebald.util.ToastUtils;

/**
 * Created by BlackZheng on 2016/4/6.
 */
public abstract class BaseFragment extends Fragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        RequestManager.cancelAll(this);
    }

    protected void executeRequest(Request request) {
        RequestManager.addRequest(request, this);
    }

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtils.showLong(R.string.wrong_message);
            }
        };
    }
}
