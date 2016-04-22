package com.blackzheng.me.piebald.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.util.Downloader;
import com.blackzheng.me.piebald.util.PathUtils;
import com.blackzheng.me.piebald.util.ToastUtils;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

public class SettingActivity extends BaseActivity {
    private static final int REQUEST_DIRECTORY = 0;
    private static final String TAG = "DirChooserSample";
    private Toolbar mToolbar;
    private String mOldPath;
    private String mNewPath;
    private TextView mDirectoryTextView;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        initActionBar(mToolbar);
        mOldPath = PathUtils.rel2abs(Downloader.getPath());
        editor = getSharedPreferences("Piebald", MODE_PRIVATE).edit();
        mDirectoryTextView = (TextView) findViewById(R.id.path);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_DIRECTORY) {

            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                mNewPath = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
                if (mNewPath.startsWith(PathUtils.abs_prefix)) {
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
            Downloader.setPath(PathUtils.abs2rel(mNewPath));
            editor.putString("path", mNewPath);
            editor.commit();
        }

    }

    public void about(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
