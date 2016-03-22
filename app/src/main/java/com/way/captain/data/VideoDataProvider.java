package com.way.captain.data;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by android on 16-2-16.
 */
public class VideoDataProvider {
    private ArrayList<VideoInfos> mData;
    private ArrayList<VideoInfos> mDeleteData;
    private VideoInfos mLastRemovedData;
    private int mLastRemovedPosition = -1;

    public VideoDataProvider() {
        mData = new ArrayList<>();
        mDeleteData = new ArrayList<>();
    }

    public void setData(ArrayList<VideoInfos> datas) {
        if (datas == null || datas.isEmpty())
            return;
        mData.clear();
        mData.addAll(datas);
    }

    public int getCount() {
        return mData.size();
    }

    public VideoInfos getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return mData.get(index);
    }

    public int undoLastRemoval() {
        if (mLastRemovedData != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < mData.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = mData.size();
            }

            mData.add(insertedPosition, mLastRemovedData);
            if(mDeleteData.contains(mLastRemovedData))
                mDeleteData.remove(mLastRemovedData);

            mLastRemovedData = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }

    public boolean deleteLastRemoval(){
        if (mLastRemovedData != null && !mDeleteData.isEmpty()) {
            for(VideoInfos info : mDeleteData) {
                File file = new File(info.getPath());
                if (file.exists())
                    file.delete();
            }
            mLastRemovedData = null;
            mLastRemovedPosition = -1;
            mDeleteData.clear();
            return true;
        } else {
            return false;
        }
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        final VideoInfos item = mData.remove(fromPosition);

        mData.add(toPosition, item);
        mLastRemovedPosition = -1;
    }

    public void removeItem(int position) {
        //noinspection UnnecessaryLocalVariable
        final VideoInfos removedItem = mData.remove(position);
        mDeleteData.add(removedItem);
        mLastRemovedData = removedItem;
        mLastRemovedPosition = position;
    }

    public void clear() {
        mData.clear();
    }
}
