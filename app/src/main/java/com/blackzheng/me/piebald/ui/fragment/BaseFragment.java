package com.blackzheng.me.piebald.ui.fragment;

import android.support.v4.app.Fragment;

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.util.LogHelper;
import com.blackzheng.me.piebald.util.ToastUtils;

import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by BlackZheng on 2016/4/6.
 */
public abstract class BaseFragment extends Fragment {

    private static final String TAG = LogHelper.makeLogTag(BaseFragment.class);
    public static final Action1<Throwable> ERRORACTION = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            ToastUtils.showLong(R.string.wrong_message);
        }
    };

    private CompositeSubscription mCompositeSubscription;

    protected void addSubscription(Subscription s) {
        if (this.mCompositeSubscription == null) {
            this.mCompositeSubscription = new CompositeSubscription();
        }
        this.mCompositeSubscription.add(s);
    }

    public void unsubcrible() {

        if (this.mCompositeSubscription != null) {
            this.mCompositeSubscription.unsubscribe();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unsubcrible();
    }
}
