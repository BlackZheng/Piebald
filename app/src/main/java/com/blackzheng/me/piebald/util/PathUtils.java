package com.blackzheng.me.piebald.util;

/**
 * Created by BlackZheng on 2016/4/16.
 */
public class PathUtils {
    public static String abs_prefix = "/storage/emulated/0";
    public static String abs2rel(String abs){
        return abs.substring(abs_prefix.length());
    }

    public static String rel2abs(String rel){
        return abs_prefix + rel;
    }
}
