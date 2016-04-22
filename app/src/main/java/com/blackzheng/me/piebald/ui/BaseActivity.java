package com.blackzheng.me.piebald.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.data.RequestManager;
import com.blackzheng.me.piebald.util.ToastUtils;

/**
 * Created by BlackZheng on 2016/4/4.
 */
public abstract class BaseActivity extends ActionBarActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected void initActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
//            case R.id.action_settings:
//                startActivity(new Intent(this, PreferenceActivity.class));
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    protected void executeRequest(Request<?> request) {
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
