package com.blackzheng.me.piebald.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.blackzheng.me.piebald.util.LogHelper;

/**
 * Created by BlackZheng on 2016/4/22.
 */
public class AdjustableImageView extends ImageView {

    private static final String TAG = LogHelper.makeLogTag(AdjustableImageView.class);

    public AdjustableImageView(Context context) {
        super(context);
    }

    public AdjustableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){

        Drawable d = getDrawable();
        if(d!=null){// ceil not round - avoid thin vertical gaps along the left/right edges
            int width = MeasureSpec.getSize(widthMeasureSpec);
            //高度根据使得图片的宽度充满屏幕计算而得
            int height = (int) Math.ceil((float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
//            LogHelper.d(TAG, width + "x" + height);
            setMeasuredDimension(width, height);
        }else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

}