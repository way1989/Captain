package com.way.captain.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.transition.SimpleViewsTracker;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.ViewsTransitionBuilder;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.way.captain.R;
import com.way.captain.adapter.GifAdapter;
import com.way.captain.adapter.GifPagerAdapter;
import com.way.captain.data.GifDataProvider;
import com.way.captain.data.GifInfos;
import com.way.captain.data.GifListLoader;
import com.way.captain.utils.GifPopupMenuHelper;
import com.way.captain.utils.PopupMenuHelper;
import com.way.captain.widget.IPopupMenuCallback;
import com.way.captain.widget.LoadingEmptyContainer;

/**
 * Created by android on 16-2-1.
 */
public class GifFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<GifListLoader.Result>,
        SwipeRefreshLayout.OnRefreshListener, GifAdapter.OnItemClickListener, ViewPositionAnimator.PositionUpdateListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private final SimpleViewsTracker mListTracker = new SimpleViewsTracker() {
        @Override
        public View getViewForPosition(int position) {
            RecyclerView.ViewHolder holder =
                    mRecyclerView.findViewHolderForLayoutPosition(position);
            return holder == null ? null : GifAdapter.getImage(holder);
        }
    };
    private GifAdapter mGifAdapter;
    private ViewPager mViewPager;
    private View mBackground;
    private GifPagerAdapter mPagerAdapter;
    private final SimpleViewsTracker mPagerTracker = new SimpleViewsTracker() {
        @Override
        public View getViewForPosition(int position) {
            RecyclePagerAdapter.ViewHolder holder = mPagerAdapter.getViewHolder(position);
            return holder == null ? null : GifPagerAdapter.getImage(holder);
        }
    };
    private ViewsTransitionAnimator<Integer> mAnimator;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private GifDataProvider dataProvider = new GifDataProvider();
    /**
     * Loading container and no results container
     */
    private LoadingEmptyContainer mLoadingEmptyContainer;
    /**
     * Pop up menu helper
     */
    private PopupMenuHelper mPopupMenuHelper;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLoadingEmptyContainer = (LoadingEmptyContainer)view.findViewById(R.id.loading_empty_container);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mViewPager = (ViewPager) view.findViewById(R.id.transition_pager);
        mBackground = view.findViewById(R.id.transition_full_background);

//        mRecyclerView.setLayoutManager(layoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mGifAdapter = new GifAdapter(getContext(), this);
        //mRecyclerView.setAdapter(mGifAdapter);
        mSwipeRefreshLayout.setRefreshing(true);
        mPagerAdapter = new GifPagerAdapter(mViewPager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mGifAdapter);      // wrap for swiping
        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,
                false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);


        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);

        mAnimator = new ViewsTransitionBuilder<Integer>()
                .fromRecyclerView(mRecyclerView, mListTracker)
                .intoViewPager(mViewPager, mPagerTracker)
                .build();
        mAnimator.addPositionUpdateListener(this);
        mPopupMenuHelper = new GifPopupMenuHelper(getActivity()) {
            @Override
            public GifInfos getGifInfos(int position) {
                return mGifAdapter.getItem(position);
            }
        };
        mGifAdapter.setPopupMenuClickedListener(new IPopupMenuCallback.IListener() {
            @Override
            public void onPopupMenuClicked(View v, int position) {
                mPopupMenuHelper.showPopupMenu(v, position);
            }
        });
        getLoaderManager().initLoader(1, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public Loader<GifListLoader.Result> onCreateLoader(int id, Bundle args) {
        mLoadingEmptyContainer.showLoading();
        return new GifListLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<GifListLoader.Result> loader, GifListLoader.Result data) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (data.gifInfosList != null && !data.gifInfosList.isEmpty()) {
            mLoadingEmptyContainer.setVisibility(View.INVISIBLE);
            dataProvider.setData(data.gifInfosList);
            mGifAdapter.setDatas(dataProvider);
            mPagerAdapter.setDatas(data.gifInfosList);
        }else{
            mLoadingEmptyContainer.showNoResults();
        }
    }

    @Override
    public void onLoaderReset(Loader<GifListLoader.Result> loader) {
        mSwipeRefreshLayout.setRefreshing(false);
        if(mGifAdapter != null)
        mGifAdapter.clearData();
        mPagerAdapter.clearData();
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        getLoaderManager().restartLoader(1, null, this);
    }

    @Override
    public void onItemClick(GifInfos gifInfos, int position, View image) {
        mAnimator.enter(position, true);
    }

    @Override
    public void onItemRemoved(int position) {
        Snackbar snackbar = Snackbar.make(
                mRecyclerView,
                R.string.snack_bar_text_item_removed,
                Snackbar.LENGTH_LONG);

        snackbar.setAction(R.string.snack_bar_action_undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onItemUndoActionClicked();
                int position = dataProvider.undoLastRemoval();
                if (position >= 0) {
                    mGifAdapter.notifyItemInserted(position);
                    mRecyclerView.scrollToPosition(position);
                    mLoadingEmptyContainer.setVisibility(View.INVISIBLE);
                }
            }
        });
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                dataProvider.deleteLastRemoval();
            }
        });
        snackbar.show();
        if (dataProvider.getCount() == 0) {
            mLoadingEmptyContainer.showNoResults();
        } else {
            mLoadingEmptyContainer.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onPositionUpdate(float state, boolean isLeaving) {
        mBackground.setVisibility(state == 0f ? View.INVISIBLE : View.VISIBLE);
        mBackground.getBackground().setAlpha((int) (255 * state));
    }

    @Override
    public boolean onBackPressed() {
        if (!mAnimator.isLeaving()) {
            mAnimator.exit(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean onFloatButtonClick() {
        return false;
    }
    @Override
    public void onDestroyView() {
        dataProvider.deleteLastRemoval();
        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mGifAdapter = null;
        mLayoutManager = null;

        super.onDestroyView();
    }
}
