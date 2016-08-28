package com.blackzheng.me.piebald.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.R;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.notification.BaseNotificationItem;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationHelper;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationListener;
import com.liulishuo.filedownloader.util.FileDownloadHelper;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.List;

/**
 * Created by BlackZheng on 2016/4/14.
 */
public class Downloader {
    private static String defaultPath = "/storage/emulated/0/Piebald/Download";
    private static String mPath;
    static{
        SharedPreferences preferences = App.getContext().getSharedPreferences("Piebald", App.getContext().MODE_PRIVATE);
        String path = preferences.getString("path", defaultPath);
        setPath(path);
    }

    public static void download(Context context, String downloadUrl, String fileName){
        switch(NetworkUtils.getNetworkType(context)){
            case NetworkUtils.TYPE_WIFI:
                downloadFile2(downloadUrl, fileName);
                break;
            case NetworkUtils.TYPE_MOBILE:
                confirmDownloading(context, downloadUrl, fileName);
                break;
            default:
                ToastUtils.showLong(R.string.wrong_network);
        }
        MobclickAgent.onEvent(context,"Download");
    }

    /**
     * 该下载方式部分ROM不支持，已废弃
     * @param downloadUrl
     * @param fileName
     */
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

    /**
     * 采用第三方狂街FileDownloader进行下载
     * @param downloadUrl
     * @param fileName
     */
    private static void downloadFile2(String downloadUrl, String fileName){
        String savePath = getPath() + "/" + fileName;
        File file = new File(savePath);
        if (file.exists()) {
            ToastUtils.showShort(R.string.file_exists);
        }
        int downloadId = 0;
        FileDownloadNotificationHelper<NotificationItem> notificationHelper = new FileDownloadNotificationHelper<>();
        NotificationListener listener = new NotificationListener(notificationHelper, savePath, fileName);
        downloadId = FileDownloader.getImpl().replaceListener(downloadUrl, savePath, listener);
        if (downloadId != 0) {
            // Avoid the task has passed 'pending' status, so we must create notification manually.
            listener.addNotificationItem(downloadId);
        }

        FileDownloader.getImpl().create(downloadUrl)
                .setPath(savePath)
                .setListener(listener)
                .start();

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

    /**
     * 当用户处于非WIFI环境下，让用户进行确认是否进行下载
     * @param context
     * @param downloadUrl
     * @param fileName
     */
    private static void confirmDownloading(Context context, final String downloadUrl, final String fileName){
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Confirm")
                .setMessage("Are you sure to download this file?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadFile2(downloadUrl, fileName);
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

    //下载路径的获取与设置
    public static void setPath(String path){
        mPath = path;
    }
    public static String getPath(){
        if(mPath != null){
            return mPath;
        }
        return defaultPath;
    }

    private static class NotificationListener extends FileDownloadNotificationListener {
        private String savePath;
        private String fileName;

        public NotificationListener(FileDownloadNotificationHelper notificationHelper, String savePath, String fileName) {
            super(notificationHelper);
            this.savePath = savePath;
            this.fileName = fileName;
        }

        @Override
        protected BaseNotificationItem create(BaseDownloadTask task) {
            return new NotificationItem(task.getDownloadId(), fileName, "downloading", savePath);
        }

        @Override
        public void addNotificationItem(BaseDownloadTask task) {
            super.addNotificationItem(task);
        }

        @Override
        public void destroyNotification(BaseDownloadTask task) {
            super.destroyNotification(task);

        }

        @Override
        protected boolean interceptCancel(BaseDownloadTask task,
                                          BaseNotificationItem n) {
            // in this demo, I don't want to cancel the notification, just show for the test
            // so return true
            return true;
        }

        @Override
        protected boolean disableNotification(BaseDownloadTask task) {
            return super.disableNotification(task);
        }

        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.pending(task, soFarBytes, totalBytes);


        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.progress(task, soFarBytes, totalBytes);

        }

        @Override
        protected void completed(BaseDownloadTask task) {
            super.completed(task);
//            FileDownloader.getImpl().stopForeground(false);
        }
    }
    public static class NotificationItem extends BaseNotificationItem {

        private String savePath;
        private String title;
        PendingIntent pendingIntent;
        private NotificationItem(int id, String title, String desc, String savePath) {
            super(id, title, desc);
            this.title = title;
            this.savePath = savePath;
        }

        @Override
        public void show(boolean statusChanged, int status, boolean isShowProgress) {
            NotificationCompat.Builder builder = new NotificationCompat.
                    Builder(FileDownloadHelper.getAppContext());

            String desc = getDesc();
            builder.setProgress(getTotal(), getSofar(), !isShowProgress);
            switch (status) {
                case FileDownloadStatus.pending:
                    desc += " pending";
                    break;
                case FileDownloadStatus.started:
                    desc += " started";
                    builder.setOngoing(true)
                            .setTicker(title + " " + desc); //No ticker above API23
                    ToastUtils.showShort(title + " " + desc);
                    break;
                case FileDownloadStatus.progress:
                    desc += " progress";
                    builder.setOngoing(true);
                    break;
                case FileDownloadStatus.retry:
                    desc += " retry";
                    break;
                case FileDownloadStatus.error:
                    desc += " error";
                    builder.setOngoing(false)
                            .setTicker(title + " " + desc);
                    ToastUtils.showShort(title + " " + desc);
                    break;
                case FileDownloadStatus.paused:
                    desc += " paused";
                    builder.setOngoing(false);
                    break;
                case FileDownloadStatus.completed:
                    desc += " completed";
                    ToastUtils.showShort(title + " " + desc);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(savePath)), "image/jpg");
                    this.pendingIntent = PendingIntent.getActivity(App.getContext(), 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(pendingIntent)
                            .setOngoing(false)
                            .setAutoCancel(true)
                            .setTicker(title + " " + desc)
                            .setProgress(getTotal(), getSofar(), false);
                    break;
                case FileDownloadStatus.warn:
                    desc += " warn";
                    builder.setOngoing(false)
                            .setTicker(title + " " + desc);
                    break;
            }
            builder.setDefaults(Notification.DEFAULT_LIGHTS)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setContentTitle(getTitle())
                    .setContentText(desc)
                    .setSmallIcon(R.mipmap.ic_launcher);

            getManager().notify(getId(), builder.build());
//            FileDownloader.getImpl().startForeground(getId(), builder.build());

        }

        @Override
        public void cancel() {
//            super.cancel();
            getManager().cancel(getId());
//            FileDownloader.getImpl().stopForeground(true);
        }
    }
}

