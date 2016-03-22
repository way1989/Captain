package com.way.captain.data;

import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.text.format.Formatter;

import com.way.captain.App;
import com.way.captain.utils.AppUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by way on 16/2/1.
 */
public class VideoInfos {
    public static final String TYPE_VIDEO = "MP4";
    private static final String VIDEO = ".mp4";
    private static final Comparator<File> mComparator = new Comparator<File>() {
        //private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(File lhs, File rhs) {
            //return sCollator.compare(lhs.lastModified(), rhs.lastModified());
            return new Long(rhs.lastModified()).compareTo(new Long(lhs.lastModified()));
        }
    };
    private String mName;
    private String mLastModifyTime;
    private String mDuration;
    private String mPath;
    private String mFileSize;
    private int[] mSize;

    /**
     *
     */
    public VideoInfos(String path) {
        parseFromPath(path);
        mPath = path;

    }

    public static ArrayList<VideoInfos> getVideoInfos() {
        ArrayList<VideoInfos> videoInfoses = new ArrayList<>();
        List<File> gifFiles = getFiles(AppUtils.VIDEOS_FOLDER_PATH, new ArrayList<File>());

        if (!gifFiles.isEmpty()) {
            Collections.sort(gifFiles, mComparator);
            for (File file : gifFiles) {
                videoInfoses.add(new VideoInfos(file.getAbsolutePath()));
            }
        }
        return videoInfoses;
    }

    /*
     * 获取目录下所有文件
     */
    public static List<File> getFiles(String realpath, List<File> files) {
        if (TextUtils.isEmpty(realpath))
            return files;

        File realFile = new File(realpath);
        if (!realFile.isDirectory())
            return files;

        File[] subfiles = realFile.listFiles();
        for (File file : subfiles) {
            if (file.isDirectory()) {
                getFiles(file.getAbsolutePath(), files);
            } else {
                String name = file.getName();

                int i = name.indexOf('.');
                if (i != -1) {
                    name = name.substring(i);
                    if (name.equalsIgnoreCase(VIDEO)) {
                        files.add(file);
                    }
                }
            }
        }

        return files;
    }

    public String getName() {
        return mName;
    }

    public String getPath() {
        return mPath;
    }

    public String getLastModifyTime() {
        return mLastModifyTime;
    }

    public String getDuration() {
        return mDuration;
    }

    public String getFileSize() {
        return mFileSize;
    }

    public int[] getSize() {
        return mSize;
    }

    private void parseFromPath(String path) {
        File f = new File(path);
        String name = f.getName();
        int length = name.length();
        mName = f.getName().substring(0, length - 4);
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        Date date = new Date(f.lastModified());
        mLastModifyTime = formatter.format(date);
        mDuration = formatFileDuration(path);
        mFileSize = Formatter.formatFileSize(App.getContext(), f.length());
        mSize = formatSize(path);
    }

    private int[] formatSize(String path) {
        int size[] = new int[]{0, 0};
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); // 视频高度
            String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); // 视频宽度
//            String rotation = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION); // 视频旋转方向
            size[0] = Integer.valueOf(width);
            size[1] = Integer.valueOf(height);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return size;
    }

    private String formatFileDuration(String path) {
        String totalDuration = "00:00";
        StringBuilder formatBuilder = new StringBuilder();
        java.util.Formatter formatter = new java.util.Formatter(formatBuilder, Locale.getDefault());
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            // 取得视频的长度(单位为毫秒)
            String time = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            // 取得视频的长度(单位为秒)
            int totalSeconds = Integer.valueOf(time) / 1000;

            int seconds = totalSeconds % 60;
            int minutes = (totalSeconds / 60) % 60;
            int hours = totalSeconds / 3600;

            formatBuilder.setLength(0);
            if (hours > 0) {
                totalDuration = formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
            } else {
                totalDuration = formatter.format("%02d:%02d", minutes, seconds).toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return totalDuration;
    }
}
