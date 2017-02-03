package com.blackzheng.me.piebald.util;

import android.os.AsyncTask;
import android.os.Build;

import java.util.concurrent.Executors;

/**
 * Created by BlackZheng on 2016/4/7.
 */
public class TaskUtils {
    public static <Params, Progress, Result> void executeAsyncTask(
            AsyncTask<Params, Progress, Result> task, Params... params) {
        if (Build.VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            task.execute(params);
        }
    }
}