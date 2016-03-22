package com.way.captain.data;

import android.text.TextUtils;
import android.text.format.Formatter;

import com.way.captain.App;
import com.way.captain.utils.AppUtils;

import java.io.File;
import java.text.Collator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by way on 16/2/1.
 */
public class GifInfos {
    public static final String TYPE_GIF = "GIF";
    private static final String GIF = ".gif";
    private static final Comparator<File> mComparator = new Comparator<File>() {
//        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(File lhs, File rhs) {
           // return sCollator.compare(lhs.lastModified(), rhs.lastModified());
            return new Long(rhs.lastModified()).compareTo(new Long(lhs.lastModified()));
        }
    };
    private String mGifName;
    private String mGifPath;
    private String mLastModifyTime;
    private String mFileSize;

    public GifInfos(String gifPath) {
        parseFromPath(gifPath);
        mGifPath = gifPath;
    }

    public static ArrayList<GifInfos> getGifInfos() {
        ArrayList<GifInfos> gifInfoses = new ArrayList<>();
        List<File> gifFiles = getFiles(AppUtils.GIF_PRODUCTS_FOLDER_PATH, new ArrayList<File>());

        if (!gifFiles.isEmpty()) {
            Collections.sort(gifFiles, mComparator);
            for (File file : gifFiles) {
                gifInfoses.add(new GifInfos(file.getAbsolutePath()));
            }
        }
        return gifInfoses;
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
                    if (name.equalsIgnoreCase(GIF)) {
                        files.add(file);
                    }
                }
            }
        }

        return files;
    }

    public String getName() {
        return mGifName;
    }

    public String getPath() {
        return mGifPath;
    }

    public String getLastModifyTime() {
        return mLastModifyTime;
    }

    public String getFileSize() {
        return mFileSize;
    }

    private void parseFromPath(String gifPath) {
        File f = new File(gifPath);
        String name = f.getName();
        int length = name.length();
        mGifName = f.getName().substring(0, length - 4);
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        Date date = new Date(f.lastModified());
        mLastModifyTime = formatter.format(date);
        mFileSize = formatFileSize(f.length());
    }

    private String formatFileSize(long filesize) {
        return Formatter.formatFileSize(App.getContext(), filesize);
    }
}
