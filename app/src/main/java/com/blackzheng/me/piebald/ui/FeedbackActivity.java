package com.blackzheng.me.piebald.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.model.Feedback;
import com.blackzheng.me.piebald.util.LogHelper;
import com.blackzheng.me.piebald.util.ToastUtils;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class FeedbackActivity extends BaseActivity {

    private static final String TAG = LogHelper.makeLogTag(FeedbackActivity.class);
    private EditText mSubject, mContent;
    private Toolbar mToolbar;
    private Feedback mFeedback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mSubject = (EditText) findViewById(R.id.subject);
        mContent = (EditText) findViewById(R.id.content);
        initActionBar(mToolbar);
    }

    public void submit(View view){
        String subject = mSubject.getText().toString();
        String content = mContent.getText().toString();
        if(content != null && content.length() > 0){
            mFeedback = new Feedback();
            mFeedback.setModel(Build.MODEL);
            mFeedback.setSdk_int(Build.VERSION.SDK_INT);
            if(subject != null && content.length() > 0)
                mFeedback.setSubject(subject);
            mFeedback.setContent(content);
            mFeedback.save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {
                    if(e==null){
                        ToastUtils.showShort(R.string.feedback_success);
                        finish();
                    }else{
                        ToastUtils.showShort(R.string.feedback_fail);
                    }
                }
            });
        }
        else{
            ToastUtils.showShort(R.string.feedback_null_toast);
        }

    }
}
