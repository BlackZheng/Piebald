package com.blackzheng.me.piebald.util;

import android.graphics.Bitmap;


import com.blackzheng.me.piebald.App;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPI;

/**
 * Created by BlackZheng on 2016/4/26.
 */
public class ShareImgToWX {

    private static final int THUMB_SIZE = 150;
    private IWXAPI api;
    private static ShareImgToWX shareImgToWX;

    private ShareImgToWX(){
        api = App.getWXAPI();
    }
    public static ShareImgToWX getInstance(){
        if(shareImgToWX == null){
            shareImgToWX = new ShareImgToWX();
        }
        return shareImgToWX;
    }
    private void share(Bitmap targetbmp, boolean isTimeline){
        Bitmap bmp = targetbmp.copy(targetbmp.getConfig(), false);

        WXImageObject imgObj = new WXImageObject(bmp);

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE * bmp.getHeight()/bmp.getWidth(), true);
        bmp.recycle();
        msg.thumbData = BitmapUtil.bmpToByteArray(thumbBmp, true);  // ��������ͼ
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = isTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;

        api.sendReq(req);
    }
    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public void shareToWeChat(Bitmap bmp){
        share(bmp, false);
    }

    public void shareToTimeline(Bitmap bmp){
        share(bmp, true);
    }
}
