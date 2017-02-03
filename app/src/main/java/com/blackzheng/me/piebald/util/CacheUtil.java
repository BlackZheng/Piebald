package com.blackzheng.me.piebald.util;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

/**主要用于获取缓存大小和清除缓存
 * Created by BlackZheng on 2016/8/18.
 */
public class CacheUtil {

    public static void clearDiskCache(){
        ImageLoader.getInstance().clearDiskCache();
    }
    public static long getDiskCacheSize(){
        return getUILDiskCacheSize();
    }

    private static long getUILDiskCacheSize() {
        long size;
        try {
            size = (int)(getFileSize(ImageLoader.getInstance().getDiskCache().getDirectory()));
        } catch (Exception e) {
            e.printStackTrace();
            size = 0;
        }
        return size;
    }

    private static long getFileSize(File f)throws Exception    //取得文件夹大小
    {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++)
        {
            if (flist[i].isDirectory())
            {
                size = size + getFileSize(flist[i]);
            } else
            {
                size = size + flist[i].length();
            }
        }
        return size;
    }

}
