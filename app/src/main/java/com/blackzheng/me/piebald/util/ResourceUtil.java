package com.blackzheng.me.piebald.util;

import android.content.Context;

/**
 * Created by BlackZheng on 2016/8/18.
 */
public class ResourceUtil {
    public static String getStringFromRes(Context context, int resId){
        return context.getResources().getString(resId);
    }
}
