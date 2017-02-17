package com.blackzheng.me.piebald.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.dao.CollectionDataHelper;
import com.blackzheng.me.piebald.dao.PhotoCollectionDataHelper;
import com.blackzheng.me.piebald.dao.UserAlbumDataHelper;
import com.blackzheng.me.piebald.dao.UserCollectionsDataHelper;
import com.blackzheng.me.piebald.util.CacheUtil;
import com.blackzheng.me.piebald.util.Downloader;
import com.blackzheng.me.piebald.util.LogHelper;
import com.blackzheng.me.piebald.util.PathUtils;
import com.blackzheng.me.piebald.util.TaskUtils;
import com.blackzheng.me.piebald.util.ToastUtils;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

public class SettingActivity extends BaseActivity {

    private static final String TAG = LogHelper.makeLogTag(SettingActivity.class);
    private static final int REQUEST_DIRECTORY = 0;
    private Toolbar mToolbar;
    private String mOldPath;
    private String mNewPath;
    private double cacheSize;
    private TextView mDirectoryTextView, size;
    private SharedPreferences.Editor editor;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initData();
        initViews();
        setupViews();
        getCacheSize();

    }
    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        initActionBar(mToolbar);
        mDirectoryTextView = (TextView) findViewById(R.id.path);
        size = (TextView) findViewById(R.id.cache_size);
    }

    private void setupViews() {
        mDirectoryTextView.setText(mOldPath);
        // Set up click handler for "Choose Directory" button
        findViewById(R.id.choose_path)
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final Intent chooserIntent = new Intent(
                                SettingActivity.this,
                                DirectoryChooserActivity.class);

                        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                                .newDirectoryName("New Folder")
                                .allowReadOnlyDirectory(false)
                                .allowNewDirectoryNameModification(true)
                                .build();

                        chooserIntent.putExtra(
                                DirectoryChooserActivity.EXTRA_CONFIG,
                                config);

                        startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
                    }
                });
    }

    private void initData() {
        mOldPath = Downloader.getPath();
        editor = getSharedPreferences("Piebald", MODE_PRIVATE).edit();
    }

    private void getCacheSize() {

        TaskUtils.executeAsyncTask(new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                cacheSize = CacheUtil.getDiskCacheSize() * 1.0/ (1024 * 1024);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                size.setText(String.format("%.2f", cacheSize) + "MB");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_DIRECTORY) {

            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                mNewPath = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
                if (mNewPath.startsWith(PathUtils.path_Prefix)) {
                    mDirectoryTextView.setText(mNewPath);
                } else {
                    ToastUtils.showLong(R.string.wrong_path);
                }

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNewPath != null) {
            Downloader.setPath(mNewPath);
            editor.putString("path", mNewPath);
            editor.commit();
        }
    }

    public void about(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void feedback(View view){
        Intent intent = new Intent(this, FeedbackActivity.class);
        startActivity(intent);
    }
    public void clearCache(View view){
        TaskUtils.executeAsyncTask(new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = ProgressDialog.show(SettingActivity.this, "", "Clearing", true, false);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dialog.dismiss();
                getCacheSize();
            }


            @Override
            protected Void doInBackground(Void... params) {
                UserAlbumDataHelper.deleteAllRows();
                CollectionDataHelper.deleteAllRows();
                PhotoCollectionDataHelper.deleteAllRows();
                UserCollectionsDataHelper.deleteAllRows();
                CacheUtil.clearDiskCache();
                return null;
            }
        });
    }
}
