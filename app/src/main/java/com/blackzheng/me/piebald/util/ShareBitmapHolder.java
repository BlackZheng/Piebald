package com.blackzheng.me.piebald.util;

import android.graphics.Bitmap;

/**用于分享图片到微信的过程中bitmap对象的暂存
 * Created by BlackZheng on 2016/4/27.
 */
//It holder the bitmap loaded in PhotoZooming Activity for sharing to Wechat
public class ShareBitmapHolder {
    private static Bitmap mBitmap;

    public static void setBitmap(Bitmap bmp){
        mBitmap = bmp;
    }
    public static Bitmap getmBitmap(){
        return mBitmap;
    }
    public static void recycleBitmap(){
        if(mBitmap != null && !mBitmap.isRecycled()){
            mBitmap.recycle();
        }
        mBitmap = null;
    }
}
