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

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.data.ImageCacheManager;
import com.blackzheng.me.piebald.model.Collection;
import com.blackzheng.me.piebald.util.Decoder;
import com.blackzheng.me.piebald.util.DrawableUtil;
import com.blackzheng.me.piebald.util.LogHelper;

/**
 * Created by BlackZheng on 2016/8/27.
 */
public class CollectionsListAdapter extends BaseAbstractRecycleCursorAdapter<CollectionsListAdapter.ViewHolder> {

    private static final String TAG = LogHelper.makeLogTag(CollectionsListAdapter.class);
    private static final int[] COLORS = {R.color.holo_blue_light, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_purple_light, R.color.holo_red_light};
    private Resources mResource;
    private Drawable mDefaultImageDrawable;
    private Drawable mDefaultProfileDrawable;
    private OnItemClickLitener mOnItemClickLitener;
    private int mWidth;

    public CollectionsListAdapter(Context context, Cursor c) {
        super(context, c);
        mWidth = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getWidth() / 2;
        mResource = context.getResources();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collections_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {
        ImageCacheManager.cancelDisplayingTask(holder.cover_photo);
        ImageCacheManager.cancelDisplayingTask(holder.profile);
        final Collection collection = Collection.fromCursor(cursor);
        holder.title.setText(collection.title);
        holder.curator.setText(collection.user.name);
        LogHelper.d(TAG, "onBindViewHolder() " + collection.id);
        float scale = 1;
        if(collection.total_photos > 0 && collection.cover_photo.color != null){// when the total photo of collections is 0, there is no cover photo
            mDefaultImageDrawable = new ColorDrawable(Color.parseColor(collection.cover_photo.color));
            mDefaultProfileDrawable = new ColorDrawable(Color.parseColor(collection.cover_photo.color));
            //some photo's width may be 0, which will cause FC
            if(collection.cover_photo.width != 0){
                scale = (float)collection.cover_photo.height / collection.cover_photo.width;
            }
            ViewGroup.LayoutParams lp = holder.cover_photo.getLayoutParams();
            lp.height = (int) (mWidth * scale);
            holder.cover_photo.setLayoutParams(lp);
            ImageCacheManager.loadImage(Decoder.decodeURL(collection.cover_photo.urls.thumb), holder.cover_photo, mDefaultImageDrawable);

        }else{
            ViewGroup.LayoutParams lp = holder.cover_photo.getLayoutParams();
            lp.height = (int) (mWidth * scale);
            holder.cover_photo.setLayoutParams(lp);
            mDefaultImageDrawable = new ColorDrawable(mResource.getColor(COLORS[cursor.getPosition() % COLORS.length]));
            mDefaultProfileDrawable  = new ColorDrawable(mResource.getColor(COLORS[cursor.getPosition() % COLORS.length]));
            holder.cover_photo.setImageDrawable(mDefaultImageDrawable);
        }

        ImageCacheManager.loadImage(Decoder.decodeURL(collection.user.profile_image.medium), holder.profile, mDefaultProfileDrawable);

        holder.cover_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickLitener != null){
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(v, collection, pos);
                }
            }
        });
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView cover_photo, profile;
        public TextView title;
        public TextView curator;

        public ViewHolder(View itemView) {
            super(itemView);
            cover_photo = (ImageView) itemView.findViewById(R.id.cover_photo);
            profile = (ImageView) itemView.findViewById(R.id.profile);
            title = (TextView) itemView.findViewById(R.id.title);
            curator = (TextView) itemView.findViewById(R.id.curator);
        }
    }

    public interface OnItemClickLitener
    {
        void onItemClick(View view, Collection collection, int position);
        void onItemLongClick(View view , Collection collection, int position);
    }
}
