package com.blackzheng.me.piebald.util;

/**
 * Created by BlackZheng on 2016/4/19.
 */
public class StringUtil {
    public static String shortenString(String str){
        if(str.length() > 8){
            return str.substring(0, 7);
        }
        return str;
    }
    public static String checkFocalLength(String str){
        if(str.endsWith("mm")){
            return str.substring(0, str.length() - 2);
        }
        return str;
    }
}
