package com.blackzheng.me.piebald.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.data.ImageCacheManager;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.ui.PhotoDetailActivity;
import com.blackzheng.me.piebald.ui.UserAlbumActivity;
import com.blackzheng.me.piebald.util.Constants;
import com.blackzheng.me.piebald.util.Decoder;
import com.blackzheng.me.piebald.util.LogHelper;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by BlackZheng on 2016/4/6.
 */
public class ContentAdapter extends BaseAbstractRecycleCursorAdapter<ContentAdapter.ViewHolder> {

    private static final String TAG = LogHelper.makeLogTag(ContentAdapter.class);
    private static final int[] COLORS = {R.color.holo_blue_light, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_purple_light, R.color.holo_red_light};
    private Resources mResource;
    private Drawable mDefaultImageDrawable;
    private Drawable mDefaultProfileDrawable;
    private OnItemClickLitener mOnItemClickLitener;
    private int mWidth;
    private int mResID;

    public ContentAdapter(Context context, Cursor c, int resID) {
        super(context, c);
        mResource = context.getResources();
        mWidth = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getWidth();
        if(mResource.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            mWidth /= 2;
        }
        mResID = resID;
    }

    @Override
    public ContentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mResID, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {
        ImageCacheManager.cancelDisplayingTask(holder.photo);
        ImageCacheManager.cancelDisplayingTask(holder.profile);
        final Photo photo = Photo.fromCursor(cursor);
        LogHelper.d(TAG, "onBindViewHolder() " + photo.id);

        //we need two default drawable because the size of drawable usted to be set when profile is loading will be set to the size of profile.
        if(photo.color != null){
            mDefaultImageDrawable = new ColorDrawable(Color.parseColor(photo.color));
            mDefaultProfileDrawable = new ColorDrawable(Color.parseColor(photo.color));
        }else{
            mDefaultImageDrawable = new ColorDrawable(mResource.getColor(COLORS[cursor.getPosition() % COLORS.length]));
            mDefaultProfileDrawable = new ColorDrawable(mResource.getColor(COLORS[cursor.getPosition() % COLORS.length]));
        }

        float scale = 1;
        //some photo's width may be 0, which will cause FC
        if(photo.width != 0){
            scale = (float)photo.height/photo.width;
        }

        ViewGroup.LayoutParams lp = holder.photo.getLayoutParams();
        lp.height = (int) (mWidth * scale);
        holder.photo.setLayoutParams(lp);

        holder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickLitener != null){
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.photo, photo, pos, Constants.TYPE_PHOTO);
                }

            }
        });
        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickLitener != null){
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.photo, photo, pos, Constants.TYPE_PROFILE);
                }
            }
        });
        ImageCacheManager.loadImage(Decoder.decodeURL(photo.urls.small), holder.photo, mDefaultImageDrawable);
        ImageCacheManager.loadImage(Decoder.decodeURL(photo.user.profile_image.small), holder.profile, mDefaultProfileDrawable);
        holder.username.setText(photo.user.name);
        holder.like_num.setText(String.valueOf(photo.likes));

    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView photo;
        public CircleImageView profile;
        public TextView username;
        public TextView like_num;


        public ViewHolder(View itemView) {
            super(itemView);
            photo = (ImageView) itemView.findViewById(R.id.photo);
            profile = (CircleImageView ) itemView.findViewById(R.id.profile);
            username = (TextView) itemView.findViewById(R.id.username);
            like_num = (TextView) itemView.findViewById(R.id.like_num);
        }
    }
    public interface OnItemClickLitener
    {
        void onItemClick(View view, Photo photo, int position, int type);
        void onItemLongClick(View view , Photo photo, int position, int type);
    }
}
