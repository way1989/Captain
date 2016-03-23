package com.way.captain.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.views.GestureFrameLayout;
import com.way.captain.R;
import com.way.captain.data.VideoInfos;
import com.way.captain.utils.glide.GlideHelper;

import java.util.ArrayList;

/**
 * Created by android on 16-2-2.
 */
public class VideoPagerAdapter extends RecyclePagerAdapter<VideoPagerAdapter.ViewHolder> implements View.OnClickListener {
    private final ViewPager mViewPager;
    private final ArrayList<VideoInfos> mVideoInfoses;
    private LayoutInflater mInflater;
    private OnPagerItemClickListener mListener;

    public VideoPagerAdapter(Context context, ViewPager pager, OnPagerItemClickListener listener) {
        mInflater = LayoutInflater.from(context);
        mViewPager = pager;
        mVideoInfoses = new ArrayList<>();
        mListener = listener;
    }

    public static View getImage(RecyclePagerAdapter.ViewHolder holder) {
        return ((ViewHolder) holder).itemView;
    }

    public void setDatas(ArrayList<VideoInfos> productInfos) {
        mVideoInfoses.clear();
        mVideoInfoses.addAll(productInfos);
        notifyDataSetChanged();
    }

    public void clearData() {
        mVideoInfoses.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container) {
        final View v = mInflater.inflate(R.layout.item_pager_video, container, false);
        ViewHolder holder = new ViewHolder(v);
        ((GestureFrameLayout) holder.itemView).getController().enableScrollInViewPager(mViewPager);
        holder.playBtn.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoInfos infos = mVideoInfoses.get(position);
        holder.playBtn.setTag(R.id.tag_item, position);
        GlideHelper.loadResource(infos.getPath(), holder.imageView);
        //if(holder.videoView.getVideoHeight() == 0)
        //holder.videoView.setVideoPath(infos.getPath());
    }

    @Override
    public int getCount() {
        return mVideoInfoses.size();
    }

    @Override
    public void onClick(View v) {
        int pos = (Integer) v.getTag(R.id.tag_item);
        mListener.onPagerItemClick(mVideoInfoses.get(pos), pos, v);
    }

    public interface OnPagerItemClickListener {
        void onPagerItemClick(VideoInfos info, int position, View itemView);
    }

    static class ViewHolder extends RecyclePagerAdapter.ViewHolder {
        public final View playBtn;
        public final ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            playBtn = itemView.findViewById(R.id.play_video_btn);
            imageView = (ImageView) itemView.findViewById(R.id.gesture_image_view);
        }
    }
}
