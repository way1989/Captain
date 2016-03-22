package com.way.captain.adapter;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.way.captain.R;
import com.way.captain.data.GifDataProvider;
import com.way.captain.data.GifInfos;
import com.way.captain.utils.glide.GlideHelper;
import com.way.captain.widget.IPopupMenuCallback;
import com.way.captain.widget.PopupMenuButton;
import com.way.captain.widget.SimpleTagImageView;

/**
 * Created by android on 16-2-1.
 */
public class GifAdapter extends RecyclerView.Adapter<GifAdapter.ViewHolder> implements SwipeableItemAdapter<GifAdapter.ViewHolder>, View.OnClickListener, IPopupMenuCallback {
    private Context mContext;
    private LayoutInflater mInflater;
    //private ArrayList<GifInfos> mGifsInfos;
    private GifDataProvider mDataProvider = new GifDataProvider();
    private OnItemClickListener mListener;

    public GifAdapter(Context context, OnItemClickListener listener) {
        mContext = context;
        mListener = listener;
        mInflater = LayoutInflater.from(context);
        //mGifsInfos = new ArrayList<>();
        setHasStableIds(true);
    }

    public static ImageView getImage(RecyclerView.ViewHolder holder) {
        if (holder instanceof ViewHolder) {
            return ((ViewHolder) holder).image;
        } else {
            return null;
        }
    }

    public void setDatas(GifDataProvider dataProvider) {
//        mGifsInfos.clear();
//        mGifsInfos.addAll(productInfos);
        mDataProvider = dataProvider;
        notifyDataSetChanged();
    }

    public void clearData() {
        mDataProvider.clear();
        notifyDataSetChanged();
    }
    public GifInfos getItem(int pos) {
        return mDataProvider.getItem(pos);
    }

    @Override
    public GifAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = mInflater.inflate(R.layout.item_image, parent, false);
        ViewHolder holder = new ViewHolder(v);
        holder.itemView.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(GifAdapter.ViewHolder holder, int position) {
        holder.itemView.setTag(R.id.tag_item, position);
        GifInfos info = mDataProvider.getItem(position);
        GlideHelper.loadResourceBitmap(info.getPath(), holder.image);
        holder.title.setText(info.getName());
        holder.time.setText(info.getLastModifyTime());
        holder.size.setText(info.getFileSize());
        holder.image.setTagText(GifInfos.TYPE_GIF);
        holder.popupMenuButton.setPosition(position);
        holder.popupMenuButton.setPopupMenuClickedListener(mPopupMenuListener);
        // set swiping properties
        holder.setSwipeItemHorizontalSlideAmount(0);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        ViewCompat.setAlpha(holder.mContainer, 1.0f);
    }

    @Override
    public int getItemCount() {
        return mDataProvider.getCount();
    }

    @Override
    public long getItemId(int position) {
        //return super.getItemId(position);
        return mDataProvider.getItem(position).getPath().hashCode();
    }

    @Override
    public void onClick(View v) {
        int pos = (Integer) v.getTag(R.id.tag_item);
        mListener.onItemClick(mDataProvider.getItem(pos), pos,  v);
    }

    @Override
    public SwipeResultAction onSwipeItem(ViewHolder holder, int position, int result) {
        switch (result) {
            // swipe left -- pin
            case SwipeableItemConstants.RESULT_SWIPED_LEFT:
            case SwipeableItemConstants.RESULT_SWIPED_RIGHT:
                return new SwipeLeftResultAction(this, position);
            // other --- do nothing
            case SwipeableItemConstants.RESULT_CANCELED:
            default:
                return null;
        }
    }

    @Override
    public int onGetSwipeReactionType(ViewHolder holder, int position, int x, int y) {
        return SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H;
    }

    @Override
    public void onSetSwipeBackground(ViewHolder holder, int position, int type) {
        int bgRes = 0;
        switch (type) {
            case SwipeableItemConstants.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_neutral;
                break;
            case SwipeableItemConstants.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_left;
                break;
            case SwipeableItemConstants.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_right;
                break;
            default:
                break;
        }

        holder.itemView.setBackgroundResource(bgRes);
    }

    public interface OnItemClickListener {
        void onItemClick(GifInfos gifInfos, int position, View image);

        void onItemRemoved(int position);
    }

    public static class ViewHolder extends AbstractSwipeableItemViewHolder {
        public final LinearLayout mContainer;
        public final SimpleTagImageView image;
        public final TextView title;
        public final TextView time;
        public final TextView size;
        public final PopupMenuButton popupMenuButton;

        public ViewHolder(View itemView) {
            super(itemView);
            mContainer = (LinearLayout) itemView.findViewById(R.id.container);
            image = (SimpleTagImageView) itemView.findViewById(R.id.ic_gif);
            title = (TextView) itemView.findViewById(R.id.title_gif);
            time = (TextView) itemView.findViewById(R.id.time_gif);
            size = (TextView) itemView.findViewById(R.id.size_gif);
            popupMenuButton = (PopupMenuButton) itemView.findViewById(R.id.popup_menu_button);
        }

        @Override
        public View getSwipeableContainerView() {
            return mContainer;
        }

        @Override
        public void onSlideAmountUpdated(float horizontalAmount, float verticalAmount, boolean isSwiping) {
            float alpha = 1.0f - Math.min(Math.max(Math.abs(horizontalAmount), 0.0f), 1.0f);
            ViewCompat.setAlpha(mContainer, alpha);
        }
    }
    private static class SwipeLeftResultAction extends SwipeResultActionMoveToSwipedDirection {
        private final int mPosition;
        private GifAdapter mAdapter;
        private boolean mSetPinned;

        SwipeLeftResultAction(GifAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            mAdapter.mDataProvider.removeItem(mPosition);
            mAdapter.notifyItemRemoved(mPosition);
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();

            if (mAdapter.mListener != null) {
                mAdapter.mListener.onItemRemoved(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }
    /**
     * Used to listen to the pop up menu callbacks
     */
    private IPopupMenuCallback.IListener mPopupMenuListener;
    @Override
    public void setPopupMenuClickedListener(IPopupMenuCallback.IListener listener) {
        mPopupMenuListener = listener;
    }
}
