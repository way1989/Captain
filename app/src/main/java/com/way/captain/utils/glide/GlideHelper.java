package com.way.captain.utils.glide;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;

public class GlideHelper {

    private static final ViewPropertyAnimation.Animator ANIMATOR =
            new ViewPropertyAnimation.Animator() {
                @Override
                public void animate(View view) {
                    view.setAlpha(0f);
                    view.animate().alpha(1f);
                }
            };


    public static void loadResource(@DrawableRes int drawableId, @NonNull ImageView image) {
        DisplayMetrics metrics = image.getResources().getDisplayMetrics();
        final int w = metrics.widthPixels, h = metrics.heightPixels;

        Glide.with(image.getContext())
                .load(drawableId)
                .animate(ANIMATOR)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new GlideDrawableImageViewTarget(image) {
                    @Override
                    public void getSize(final SizeReadyCallback cb) {
                        // We don't want to load very big images on devices with small screens.
                        // This will help Glide correctly choose images scale when reading them.
                        super.getSize(new SizeReadyCallback() {
                            @Override
                            public void onSizeReady(int width, int height) {
                                cb.onSizeReady(w / 2, h / 2);
                            }
                        });
                    }
                });
    }

    public static void loadResource(String path, @NonNull ImageView image) {
        DisplayMetrics metrics = image.getResources().getDisplayMetrics();
        final int w = metrics.widthPixels, h = metrics.heightPixels;

        Glide.with(image.getContext())
                .load(path)
                .animate(ANIMATOR)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new GlideDrawableImageViewTarget(image) {
                    @Override
                    public void getSize(final SizeReadyCallback cb) {
                        // We don't want to load very big images on devices with small screens.
                        // This will help Glide correctly choose images scale when reading them.
                        super.getSize(new SizeReadyCallback() {
                            @Override
                            public void onSizeReady(int width, int height) {
                                cb.onSizeReady(w / 2, h / 2);
                            }
                        });
                    }
                });
    }

    public static void loadResourceBitmap(String path, @NonNull ImageView image) {
        Glide.with(image.getContext())
                .load(path)
                .asBitmap()
                .animate(ANIMATOR)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(image);
    }
}
