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
import android.widget.TextView;

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.data.ImageCacheManager;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.util.Decoder;
import com.blackzheng.me.piebald.util.DrawableUtil;
import com.blackzheng.me.piebald.util.LogHelper;
import com.blackzheng.me.piebald.view.AdjustableImageView;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by BlackZheng on 2016/4/6.
 */
public class ContentAdapter extends BaseAbstractRecycleCursorAdapter<ContentAdapter.ViewHolder> {

    private static final String TAG = LogHelper.makeLogTag(ContentAdapter.class);
    private static final int[] COLORS = {R.color.holo_blue_light, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_purple_light, R.color.holo_red_light};
    private Resources mResource;
    private Drawable mDefaultImageDrawable;
    private OnItemClickLitener mOnItemClickLitener;
    private int mWidth;
    private int mResID;

    public ContentAdapter(Context context, Cursor c, int resID) {
        super(context, c);
        mWidth = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getWidth();
        mResource = context.getResources();
        mResID = resID;
    }

    @Override
    public ContentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mResID, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {
        LogHelper.d(TAG, "onBindViewHolder()");
        ImageCacheManager.cancelDisplayingTask(holder.photo);
        ImageCacheManager.cancelDisplayingTask(holder.profile);
        final Photo photo = Photo.fromCursor(cursor);
        if(photo.color != null){
            mDefaultImageDrawable = new ColorDrawable(Color.parseColor(photo.color));
        }else{
            mDefaultImageDrawable = new ColorDrawable(mResource.getColor(COLORS[cursor.getPosition() % COLORS.length]));
        }

        ViewGroup.LayoutParams lp = holder.photo.getLayoutParams();
        lp.height = photo.height;
        holder.photo.setLayoutParams(lp);
        holder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickLitener != null){
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.photo, photo, pos);
                }

            }
        });
        LogHelper.d(TAG, "onBindViewHolder() " + photo.id);
        ImageCacheManager.loadImage(Decoder.decodeURL(photo.urls.small), holder.photo, DrawableUtil.getDrawable(photo.color, mWidth, mWidth*photo.height/photo.width));
//        ImageCacheManager.loadImage(Decoder.decodeURL(photo.urls.small), holder.photo, DrawableUtil.toSuitableDrawable(mDefaultImageDrawable, mWidth, mWidth*photo.height/photo.width));
        ImageCacheManager.loadImage(Decoder.decodeURL(photo.user.profile_image.small), holder.profile, mDefaultImageDrawable);
        holder.username.setText(photo.user.name);
        holder.like_num.setText(String.valueOf(photo.likes));

    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public AdjustableImageView photo;
        public CircleImageView profile;
        public TextView username;
        public TextView like_num;


        public ViewHolder(View itemView) {
            super(itemView);
            photo = (AdjustableImageView) itemView.findViewById(R.id.photo);
            profile = (CircleImageView ) itemView.findViewById(R.id.profile);
            username = (TextView) itemView.findViewById(R.id.username);
            like_num = (TextView) itemView.findViewById(R.id.like_num);
        }
    }
    public interface OnItemClickLitener
    {
        void onItemClick(View view, Photo photo, int position);
        void onItemLongClick(View view , Photo photo, int position);
    }
}
