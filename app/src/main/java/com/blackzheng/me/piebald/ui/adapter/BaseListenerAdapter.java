package com.blackzheng.me.piebald.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;

/**
 * Created by BlackZheng on 2016/8/27.
 */
public abstract  class BaseListenerAdapter<VH extends RecyclerView.ViewHolder> extends BaseAbstractRecycleCursorAdapter<VH>  {
    private Resources mResource;
    private Drawable mDefaultImageDrawable;
    private int mWidth;

    public BaseListenerAdapter(Context context, Cursor c) {
        super(context, c);
        mWidth = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getWidth();
        mResource = context.getResources();
    }
}
