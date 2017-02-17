package com.blackzheng.me.piebald.util;

import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**主要用于获取缓存大小和清除缓存
 * Created by BlackZheng on 2016/8/18.
 */
public class CacheUtil {
    private static final String TAG = LogHelper.makeLogTag(CacheUtil.class);
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

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

}
