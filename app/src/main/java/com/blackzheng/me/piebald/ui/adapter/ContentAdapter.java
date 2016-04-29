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
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.data.ImageCacheManager;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.util.Decoder;
import com.blackzheng.me.piebald.util.DrawableUtil;
import com.blackzheng.me.piebald.view.AdjustableImageView;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by BlackZheng on 2016/4/6.
 */
public class ContentAdapter extends BaseAbstractRecycleCursorAdapter<ContentAdapter.ViewHolder> {

    private static final int[] COLORS = {R.color.holo_blue_light, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_purple_light, R.color.holo_red_light};
    private Resources mResource;
    private Drawable mDefaultImageDrawable;
    private OnItemClickLitener mOnItemClickLitener;
    private int width;

    public ContentAdapter(Context context, Cursor c) {
        super(context, c);
        width = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getWidth();
        mResource = context.getResources();
    }

    @Override
    public ContentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {

        if (holder.photoRequest != null) {
            holder.photoRequest.cancelRequest();
        }
        if (holder.profileRequest != null) {
            holder.profileRequest.cancelRequest();
        }
        final Photo photo = Photo.fromCursor(cursor);
        if(photo.color != null){
            mDefaultImageDrawable = new ColorDrawable(Color.parseColor(photo.color));
        }else{
            mDefaultImageDrawable = new ColorDrawable(mResource.getColor(COLORS[cursor.getPosition() % COLORS.length]));
        }

        holder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getLayoutPosition();
                mOnItemClickLitener.onItemClick(holder.photo, photo, pos);
            }
        });

        holder.photoRequest = ImageCacheManager.loadImage(Decoder.decodeURL(photo.urls.small), ImageCacheManager
                .getImageListener(holder.photo,
                        DrawableUtil.toSuitableDrawable(mDefaultImageDrawable, width, width * photo.height / photo.height),
                        mDefaultImageDrawable), 0, 0);
        holder.profileRequest = ImageCacheManager.loadImage(Decoder.decodeURL(photo.user.profile_image.small), ImageCacheManager
                .getProfileListener(holder.profile, mDefaultImageDrawable, mDefaultImageDrawable), 0, 0);
        holder.username.setText(Decoder.decodeStr(photo.user.name));
        holder.like_num.setText(String.valueOf(photo.likes));




    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public AdjustableImageView photo;
        public CircleImageView profile;
        public TextView username;
        public TextView like_num;
        public ImageLoader.ImageContainer photoRequest;
        public ImageLoader.ImageContainer profileRequest;

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
