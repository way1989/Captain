package com.way.captain.data;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;

/**
 * Created by way on 16/2/1.
 */
public class GifListLoader extends AsyncTaskLoader<GifListLoader.Result> {
    private Result mResult;

    public GifListLoader(Context context) {
        super(context);
    }

    @Override
    public Result loadInBackground() {
        Result result = new Result();
        ArrayList<GifInfos> gifInfoses = GifInfos.getGifInfos();
        if (!gifInfoses.isEmpty())
            result.gifInfosList = gifInfoses;

        return result;
    }

    // Called when there is new data to deliver to the client. The
    // super class will take care of delivering it; the implementation
    // here just adds a little more logic.
    @Override
    public void deliverResult(Result result) {
        mResult = result;

        if (isStarted()) {
            // If the Loader is started, immediately deliver its results.
            super.deliverResult(result);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mResult != null) {
            // If we currently have a result available, deliver it immediately.
            deliverResult(mResult);
        }

        if (takeContentChanged() || mResult == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated if needed.
        mResult = null;
    }

    public static class Result {
        public ArrayList<GifInfos> gifInfosList;
    }
}
