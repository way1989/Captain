package com.way.captain.fragment;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.transition.SimpleViewsTracker;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.ViewsTransitionBuilder;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.way.captain.R;
import com.way.captain.adapter.VideoAdapter;
import com.way.captain.adapter.VideoPagerAdapter;
import com.way.captain.data.VideoDataProvider;
import com.way.captain.data.VideoInfos;
import com.way.captain.data.VideoListLoader;
import com.way.captain.utils.GifUtils;
import com.way.captain.utils.PopupMenuHelper;
import com.way.captain.widget.FastVideoView;
import com.way.captain.widget.IPopupMenuCallback;
import com.way.captain.widget.LoadingEmptyContainer;
import com.way.captain.widget.VideoMenuHelper;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_MOVIES;

/**
 * Created by android on 16-2-1.
 */
public class VideoFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<VideoListLoader.Result>,
        SwipeRefreshLayout.OnRefreshListener, VideoAdapter.OnItemClickListener, VideoPagerAdapter.OnPagerItemClickListener,
        ViewPositionAnimator.PositionUpdateListener, PopupMenu.OnMenuItemClickListener {
    private static final String TAG = VideoFragment.class.getSimpleName();
    private final static int PROGRESS_CHANGED = 0;
    private final DateFormat fileFormat = new SimpleDateFormat("'Gif_'yyyy-MM-dd-HH-mm-ss'.gif'", Locale.US);
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private final SimpleViewsTracker mListTracker = new SimpleViewsTracker() {
        @Override
        public View getViewForPosition(int position) {
            RecyclerView.ViewHolder holder =
                    mRecyclerView.findViewHolderForLayoutPosition(position);
            return holder == null ? null : VideoAdapter.getImage(holder);
        }
    };
    private VideoAdapter mVideoAdapter;
    private ViewPager mViewPager;
    private View mBackground;
    private VideoPagerAdapter mPagerAdapter;
    private final SimpleViewsTracker mPagerTracker = new SimpleViewsTracker() {
        @Override
        public View getViewForPosition(int position) {
            RecyclePagerAdapter.ViewHolder holder = mPagerAdapter.getViewHolder(position);
            return holder == null ? null : VideoPagerAdapter.getImage(holder);
        }
    };
    private ViewsTransitionAnimator<Integer> mAnimator;
    private FastVideoView mVideoView;
    private ProgressDialog progressDialog;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private VideoDataProvider dataProvider = new VideoDataProvider();
    /**
     * Pop up menu helper
     */
    private PopupMenuHelper mPopupMenuHelper;
    /**
     * Loading container and no results container
     */
    private LoadingEmptyContainer mLoadingEmptyContainer;
    private View mPlayControlerView;
    private ImageButton mPlayPauseBtn;
    private SeekBar mSeekBar;
    private TextView mDurationTextView;
    private TextView mPlayedTextView = null;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS_CHANGED:

                    int position = mVideoView.getCurrentPosition();
                    mSeekBar.setProgress(position);

                    mPlayedTextView.setText(stringForTime(position));
                    msg = obtainMessage(PROGRESS_CHANGED);
                    sendMessageDelayed(msg, 1000 - (position % 1000));
                    break;
            }

            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadFFMpegBinary();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FFmpeg.getInstance(getContext().getApplicationContext()).killRunningProcesses();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPlayControlerView(view);
        initView(view);
        initSwipeItem();
        initItemAnim();
        initProgressDialog();
        initPopupMenu();
        getLoaderManager().initLoader(0, null, this);

    }

    private void initPopupMenu() {
        mPopupMenuHelper = new VideoMenuHelper(getActivity()) {
            @Override
            public VideoInfos getVideoInfos(int position) {
                return mVideoAdapter.getItem(position);
            }
        };
        mVideoAdapter.setPopupMenuClickedListener(new IPopupMenuCallback.IListener() {
            @Override
            public void onPopupMenuClicked(View v, int position) {
                mPopupMenuHelper.showPopupMenu(v, position);
            }
        });
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(null);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void initItemAnim() {
        mAnimator = new ViewsTransitionBuilder<Integer>()
                .fromRecyclerView(mRecyclerView, mListTracker)
                .intoViewPager(mViewPager, mPagerTracker)
                .build();
        mAnimator.addPositionUpdateListener(this);
    }

    private void initSwipeItem() {
        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mVideoAdapter);      // wrap for swiping
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
    }

    private void initView(View view) {
        mLoadingEmptyContainer = (LoadingEmptyContainer) view.findViewById(R.id.loading_empty_container);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mViewPager = (ViewPager) view.findViewById(R.id.transition_pager);
        mBackground = view.findViewById(R.id.transition_full_background);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mVideoAdapter = new VideoAdapter(getContext(), this);
        mSwipeRefreshLayout.setRefreshing(true);
        mPagerAdapter = new VideoPagerAdapter(getContext(), mViewPager, this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
    }

    private void initPlayControlerView(View view) {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        mVideoView = (FastVideoView) view.findViewById(R.id.video_view);
        mPlayControlerView = view.findViewById(R.id.video_control_view);
        mPlayPauseBtn = (ImageButton) view.findViewById(R.id.pause);
        mSeekBar = (SeekBar) view.findViewById(R.id.mediacontroller_progress);
        mDurationTextView = (TextView) view.findViewById(R.id.time);
        mPlayedTextView = (TextView) view.findViewById(R.id.time_current);
        view.findViewById(R.id.to_gif).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVideoEditMenu(v);
            }
        });
        mPlayPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    mPlayPauseBtn.setImageResource(R.drawable.ic_play_arrow_white_36dp);
                } else {
                    mVideoView.start();
                    mPlayPauseBtn.setImageResource(R.drawable.ic_pause_white_36dp);
                }
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    mVideoView.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeMessages(PROGRESS_CHANGED);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.sendEmptyMessage(PROGRESS_CHANGED);
            }
        });
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int duration = mVideoView.getDuration();
                Log.d("onCompletion", "" + duration);
                mSeekBar.setMax(duration);
                mDurationTextView.setText(stringForTime(duration));
                mVideoView.start();
                mPlayPauseBtn.setImageResource(R.drawable.ic_pause_white_36dp);
                mHandler.sendEmptyMessage(PROGRESS_CHANGED);
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlayPauseBtn.setImageResource(R.drawable.ic_play_arrow_white_36dp);
            }
        });
    }

    private void showVideoEditMenu(View view) {
        // create the popup menu
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        final Menu menu = popupMenu.getMenu();
        popupMenu.getMenuInflater().inflate(R.menu.menu_video_edit, menu);
        // hook up the click listener
        popupMenu.setOnMenuItemClickListener(this);
        // show it
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.video_menu_to_gif:
                onGifSettingsClick();
                break;
            case R.id.video_menu_frame:
                GifUtils.framePicker(getActivity());
                break;
            case R.id.video_menu_length:
                GifUtils.lengthPicker(getActivity());
                break;
            case R.id.video_menu_scale:
                GifUtils.sizePicker(getActivity());
                break;
            default:
                break;
        }
        return false;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public Loader<VideoListLoader.Result> onCreateLoader(int id, Bundle args) {
        mLoadingEmptyContainer.showLoading();
        return new VideoListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<VideoListLoader.Result> loader, VideoListLoader.Result data) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (data.videosInfoList != null && !data.videosInfoList.isEmpty()) {
            mLoadingEmptyContainer.setVisibility(View.INVISIBLE);
            dataProvider.setData(data.videosInfoList);
            mVideoAdapter.setDatas(dataProvider);
            mPagerAdapter.setDatas(data.videosInfoList);
        } else {
            mLoadingEmptyContainer.showNoResults();
        }
    }

    @Override
    public void onLoaderReset(Loader<VideoListLoader.Result> loader) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (mVideoAdapter != null)
            mVideoAdapter.clearData();
        mPagerAdapter.clearData();
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onItemClick(VideoInfos videoInfos, int position, View image) {
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
                int position = dataProvider.undoLastRemoval();
                if (position >= 0) {
                    mVideoAdapter.notifyItemInserted(position);
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
    public void onPagerItemClick(VideoInfos info, int position, View itemView) {
        if (mVideoView.getVisibility() == View.VISIBLE)
            return;
        mVideoView.setVisibility(View.VISIBLE);
        mVideoView.setTag(info);
        mVideoView.animate().alpha(1).setStartDelay(500).start();
        mVideoView.setVideoPath(info.getPath());
        mVideoView.start();
        mPlayControlerView.setVisibility(View.VISIBLE);
        Log.i("broncho", "onPagerItemClick...");
    }

    @Override
    public boolean onBackPressed() {
        if (mVideoView.getVisibility() == View.VISIBLE) {
            mVideoView.pause();
            mVideoView.stopPlayback();
            mVideoView.animate().alpha(0).start();
            mVideoView.setVisibility(View.GONE);
            mPlayControlerView.setVisibility(View.GONE);
            return true;
        }

        if (!mAnimator.isLeaving()) {
            mAnimator.exit(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean onFloatButtonClick() {
        //if (onGifSettingsClick()) return true;
        return false;
    }

    private boolean onGifSettingsClick() {
        if (mVideoView.getVisibility() != View.VISIBLE)
            return false;
        mVideoView.pause();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        File outputRoot = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES), "Gifs");
        if (!outputRoot.exists()) {
            outputRoot.mkdir();
        }
        String outputName = fileFormat.format(new Date());
        String outputFile = new File(outputRoot, outputName).getAbsolutePath();

        int customGifLength = prefs.getInt(GifUtils.KEY_GIF_LENGTH, GifUtils.DEFAULT_GIF_LENGTH);
        int customGifFrame = prefs.getInt(GifUtils.KEY_GIF_FRAME, GifUtils.DEFAULT_GIF_FRAME);
        int customGifScale = prefs.getInt(GifUtils.KEY_GIF_SIZE, GifUtils.DEFAULT_GIF_SIZE);

        VideoInfos info = (VideoInfos) mVideoView.getTag();
        int videoDuration = mVideoView.getDuration() / 1000;
        int videoCurrenPosition = mVideoView.getCurrentPosition() / 1000;
        int gifLength = videoDuration - videoCurrenPosition;
        if (gifLength <= GifUtils.MIN_GIF_LENGTH) {
            gifLength = GifUtils.MIN_GIF_LENGTH;
            videoCurrenPosition = videoDuration - GifUtils.DEFAULT_GIF_LENGTH < 0 ? 0 : (videoDuration - GifUtils.DEFAULT_GIF_LENGTH);
        } else if (gifLength > customGifLength) {
            gifLength = customGifLength;
        }
        Log.i("broncho", "Video length = " + videoDuration + ", curLength = " + videoCurrenPosition
                + ", length = " + gifLength + ", frame = " + customGifFrame + ", scale = " + customGifScale);

        String cmd = GifUtils.getVideo2gifCommand(videoCurrenPosition, gifLength, customGifFrame, info.getPath(),
                outputFile, (int) (info.getSize()[0] / Math.sqrt(customGifScale)), (int) (info.getSize()[1] / Math.sqrt(customGifScale)));
        String[] command = cmd.split(" ");
        if (command.length != 0) {
            execFFmpegBinary(command);
        } else {
            Toast.makeText(getContext(), "command == null", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    public void onPositionUpdate(float state, boolean isLeaving) {
        mBackground.setVisibility(state == 0f ? View.INVISIBLE : View.VISIBLE);
        mBackground.getBackground().setAlpha((int) (255 * state));
    }

    private void loadFFMpegBinary() {
        try {
            FFmpeg.getInstance(getContext().getApplicationContext()).loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Toast.makeText(getContext(), "load ffmpeg err...", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (FFmpegNotSupportedException e) {
        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            FFmpeg.getInstance(getContext()).execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Snackbar.make(mVideoView, "FAILED with output : " + s, Snackbar.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(String s) {
                    Snackbar.make(mVideoView, "SUCCESS with output : " + s, Snackbar.LENGTH_SHORT).show();
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    progressDialog.setMessage("Processing\n" + s);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
                    progressDialog.dismiss();
                    onBackPressed();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
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
        mVideoAdapter = null;
        mLayoutManager = null;

        super.onDestroyView();
    }


}
