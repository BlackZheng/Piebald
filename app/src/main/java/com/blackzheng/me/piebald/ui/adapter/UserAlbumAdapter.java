package com.blackzheng.me.piebald.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.data.ImageCacheManager;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.util.Decoder;
import com.blackzheng.me.piebald.util.DrawableUtil;
import com.blackzheng.me.piebald.util.LogHelper;
import com.blackzheng.me.piebald.view.AdjustableImageView;

import java.util.Random;

/**
 * Created by BlackZheng on 2016/8/17.
 */
public class UserAlbumAdapter extends BaseAbstractRecycleCursorAdapter<UserAlbumAdapter.PhotoViewHolder> {

    private static final String TAG = LogHelper.makeLogTag(UserAlbumAdapter.class);
    private Resources mResource;
    private Drawable mDefaultImageDrawable;
    private OnItemClickLitener mOnItemClickLitener;
    private int width;

    public UserAlbumAdapter(Context context, Cursor c) {
        super(context, c);
        width = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getWidth();
        mResource = context.getResources();
    }

    @Override
    public UserAlbumAdapter.PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_photo_item, parent, false);
        return new PhotoViewHolder(view);

    }


    @Override
    public void onBindViewHolder(final UserAlbumAdapter.PhotoViewHolder holder, Cursor cursor) {

        ImageCacheManager.cancelDisplayingTask(holder.photo);
        final Photo photo = Photo.fromCursor(cursor);
        if(photo.color != null){
            mDefaultImageDrawable = new ColorDrawable(Color.parseColor(photo.color));
        }else{
            mDefaultImageDrawable = new ColorDrawable(mResource.getColor(DrawableUtil.getDefaultColors()[new Random().nextInt(5)]));
        }
        holder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getLayoutPosition();
                mOnItemClickLitener.onItemClick(holder.photo, photo, pos);
            }
        });
        ImageCacheManager.loadImage(Decoder.decodeURL(photo.urls.thumb), holder.photo, DrawableUtil.toSuitableDrawable(mDefaultImageDrawable, width, width*photo.height/photo.width));
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        public AdjustableImageView photo;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            photo = (AdjustableImageView) itemView.findViewById(R.id.photo);
        }
    }

    public interface OnItemClickLitener
    {
        void onItemClick(View view, Photo photo, int position);
        void onItemLongClick(View view , Photo photo, int position);
    }
}
