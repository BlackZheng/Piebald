package com.blackzheng.me.piebald.util;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by BlackZheng on 2016/4/26.
 */
public class BitmapUtil {
    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        int count = 1;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( output.toByteArray().length / 1024>32) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            count++;
            output.reset();//重置baos即清空baos
            bmp.compress(Bitmap.CompressFormat.JPEG, options, output);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        if (needRecycle) {
            bmp.recycle();
        }


        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
