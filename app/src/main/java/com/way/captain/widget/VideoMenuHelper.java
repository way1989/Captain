package com.way.captain.widget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;

import com.way.captain.R;
import com.way.captain.data.VideoInfos;
import com.way.captain.utils.PopupMenuHelper;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by way on 16/2/28.
 */
public abstract class VideoMenuHelper extends PopupMenuHelper {
    private static final String VIDEO_SHARE_SUBJECT_TEMPLATE = "Video (%s)";
    protected VideoInfos mVideoInfos;

    public VideoMenuHelper(Activity activity) {
        super(activity);
        mType = PopupMenuType.Video;
    }

    public abstract VideoInfos getVideoInfos(int position);

    @Override
    public PopupMenuType onPreparePopupMenu(int position) {
        mVideoInfos = getVideoInfos(position);
        if (mVideoInfos == null)
            return null;
        return PopupMenuType.Video;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.gif_item_share:
                String subjectDate = DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
                String subject = String.format(VIDEO_SHARE_SUBJECT_TEMPLATE, subjectDate);
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("image/png");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mVideoInfos.getPath())));
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                Intent chooserIntent = Intent.createChooser(sharingIntent, null);
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(chooserIntent);
                return true;
            //case R.id.gif_item_delete:

            //    return true;
            default:
                break;
        }
        return super.onMenuItemClick(item);
    }
}
