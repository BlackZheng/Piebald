package com.blackzheng.me.piebald.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.blackzheng.me.piebald.data.ImageCacheManager;
import com.blackzheng.me.piebald.ui.MainActivity;
import com.blackzheng.me.piebald.util.DensityUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.os.Build;

/**
 * Created by BlackZheng on 2016/8/26.
 */
public class DrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_DIVIDER = 1;
    private static final int TYPE_ITEM = 2;

    private Drawable mDefaultHeaderDrawable;
    private String[] mDatas;
    private OnItemClickLitener mOnItemClickLitener;
    private Context mContext;

    public DrawerAdapter(Context context, String[] datas){
        mDatas = datas;
        mContext = context;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mDefaultHeaderDrawable = mContext.getDrawable(R.drawable.drawer_header_bg);
        else
            mDefaultHeaderDrawable = mContext.getResources().getDrawable(R.drawable.drawer_header_bg);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case TYPE_HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header, parent, false);
                return new HeaderViewHolder(view);
            case TYPE_DIVIDER :
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.divider_layout, parent, false);
                return new DividerViewHolder(view);
            case TYPE_ITEM :
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_item, parent, false);
                return new DrawerViewHolder(view);
            default:
                break;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof DrawerViewHolder){
            final DrawerViewHolder drawerHolder = (DrawerViewHolder) holder;
            drawerHolder.tv.setText(mDatas[position]);
            drawerHolder.tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickLitener.onItemClick(v, position);
                }
            });
        }
        else if(holder instanceof HeaderViewHolder){
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            ImageView imageView = headerViewHolder.img;
//            Log.d("size", imageView.getHeight() + ":" + imageView.getWidth());
//            ImageCacheManager.loadImage(String.format(UnsplashAPI.GET_RANDOM_PHOTOS, DensityUtils.dip2px(mContext, 320f), DensityUtils.dip2px(mContext, 200f)),
//                    ImageCacheManager.getImageListener(imageView, mDefaultHeaderDrawable, mDefaultHeaderDrawable),imageView.getWidth(), imageView.getHeight());
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.drawer_header_bg)
                    .showImageOnFail(R.drawable.drawer_header_bg)
                    .cacheInMemory(true)
                    .cacheOnDisk(false)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .considerExifParams(false)
                    .build();

            ImageLoader.getInstance().displayImage(String.format(UnsplashAPI.GET_RANDOM_PHOTOS, DensityUtils.dip2px(mContext, 320f), DensityUtils.dip2px(mContext, 200f))
                    , imageView, options);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (mDatas[position].equals(MainActivity.HEADER))
            return TYPE_HEADER;
        else if(mDatas[position].equals(MainActivity.DIVIDER))
            return TYPE_DIVIDER;
        return TYPE_ITEM ;
    }

    @Override
    public int getItemCount() {
        return mDatas.length;
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public class DrawerViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        public DrawerViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.text_view);
        }
    }
    public class DividerViewHolder extends RecyclerView.ViewHolder {
        public DividerViewHolder(View itemView) {
            super(itemView);
        }
    }
    public class HeaderViewHolder extends  RecyclerView.ViewHolder{
        ImageView img;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.drawer_header);
        }
    }

    public interface OnItemClickLitener
    {
        void onItemClick(View view, int position);
        void onItemLongClick(View view , int position);
    }
}
