package com.blackzheng.me.piebald.util;

/**
 * Created by BlackZheng on 2016/4/25.
 */
public class StringUtil {

    /**
     * 缩短EXIF中小数位过多的参数
     * @param str
     * @return
     */
    public static String shortenString(String str) {
        if (str.length() > 8) {
            return str.substring(0, 7);
        }
        return str;
    }

    /**
     * EXIF里的焦距参数有时会带单位mm,有时不会，在这里进行统一
     * @param str
     * @return
     */
    public static String checkFocalLength(String str) {
        if (str.endsWith("mm")) {
            return str.substring(0, str.length() - 2);
        }
        return str;
    }

    /**
     * 服务器返回的日期过长，去掉不需要的时间
     * @param date
     * @return
     */
    public static String dateFormat(String date){
        String format = " " + date.substring(0, date.indexOf('T'));
        return format;
    }
}