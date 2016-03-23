package com.way.captain.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.way.captain.data.GifInfos;
import com.way.captain.utils.glide.GlideHelper;

import java.util.ArrayList;

/**
 * Created by android on 16-2-2.
 */
public class GifPagerAdapter extends RecyclePagerAdapter<GifPagerAdapter.ViewHolder> {
    private final ViewPager mViewPager;
    private final ArrayList<GifInfos> mGifInfoses;

    public GifPagerAdapter(ViewPager pager) {
        mViewPager = pager;
        mGifInfoses = new ArrayList<>();
    }

    public static GestureImageView getImage(RecyclePagerAdapter.ViewHolder holder) {
        return ((ViewHolder) holder).image;
    }

    public void setDatas(ArrayList<GifInfos> productInfos) {
        mGifInfoses.clear();
        mGifInfoses.addAll(productInfos);
        notifyDataSetChanged();
    }

    public void clearData() {
        mGifInfoses.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container) {
        ViewHolder holder = new ViewHolder(container);
        holder.image.getController().enableScrollInViewPager(mViewPager);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GifInfos infos = mGifInfoses.get(position);
        GlideHelper.loadResource(infos.getPath(), holder.image);
    }

    @Override
    public int getCount() {
        return mGifInfoses.size();
    }

    static class ViewHolder extends RecyclePagerAdapter.ViewHolder {
        public final GestureImageView image;

        public ViewHolder(ViewGroup container) {
            super(new GestureImageView(container.getContext()));
            image = (GestureImageView) itemView;
        }
    }
}
