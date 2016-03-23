package com.way.captain.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;

import com.way.captain.R;
import com.way.captain.data.GifInfos;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by way on 16/2/28.
 */
public abstract class GifPopupMenuHelper extends PopupMenuHelper {
    private static final String GIF_SHARE_SUBJECT_TEMPLATE = "Gif (%s)";
    protected GifInfos mGifInfos;

    public GifPopupMenuHelper(Activity activity) {
        super(activity);
        mType = PopupMenuType.Gif;
    }

    public abstract GifInfos getGifInfos(int position);

    @Override
    public PopupMenuType onPreparePopupMenu(int position) {
        mGifInfos = getGifInfos(position);
        if (mGifInfos == null)
            return null;
        return PopupMenuType.Gif;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.gif_item_share:
                String subjectDate = DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
                String subject = String.format(GIF_SHARE_SUBJECT_TEMPLATE, subjectDate);
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("image/png");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mGifInfos.getPath())));
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
