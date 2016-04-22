package com.blackzheng.me.piebald.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.R;

import java.util.List;

/**
 * Created by BlackZheng on 2016/4/14.
 */
public class Downloader {
    public static String mPath;
    static{
        SharedPreferences preferences = App.getContext().getSharedPreferences("Piebald", App.getContext().MODE_PRIVATE);
        String path = preferences.getString("path", "/Piebald/Download");
        setPath(path);
    }

    public static void download(Context context, String downloadUrl, String fileName){
        switch(NetworkUtils.getNetworkType(context)){
            case NetworkUtils.TYPE_WIFI:
                downloadFile(downloadUrl, fileName);
                break;
            case NetworkUtils.TYPE_MOBILE:
                confirmDownloading(context, downloadUrl, fileName);
                break;
            default:
                ToastUtils.showLong(R.string.wrong_network);
        }
    }

    @SuppressLint("NewApi")
    private static void downloadFile(String downloadUrl, String fileName){
        Boolean result=isDownloadManagerAvailable(App.getContext());
        if (result){
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            request.setDescription("Image from Piebald");   //appears the same in Notification bar while downloading
            request.setTitle("Downloading " + fileName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            }
            request.setDestinationInExternalPublicDir(getPath(), fileName + ".jpg");
//            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            // get download service and enqueue file
            DownloadManager manager = (DownloadManager) App.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);
        }
        else{
            ToastUtils.showShort(R.string.download_failure);
        }

    }

    private static boolean isDownloadManagerAvailable(Context context) {
        try {
            Log.d("version", Build.VERSION.SDK_INT + ":" + Build.VERSION_CODES.GINGERBREAD);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            Log.d("version", list.size() + "");
            return list.size() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private static void confirmDownloading(Context context, final String downloadUrl, final String fileName){
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Confirm")
                .setMessage("Are you sure to download this file?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadFile(downloadUrl, fileName);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        builder.create()
                .show();
    }
    public static void setPath(String path){
        mPath = path;
    }
    public static String getPath(){
        if(mPath != null){
            return mPath;
        }
        return "Piebald/Download";
    }
}

