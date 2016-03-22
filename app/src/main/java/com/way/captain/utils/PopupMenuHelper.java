/*
* Copyright (C) 2014 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.way.captain.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.way.captain.R;


/**
 * Simple helper class that does most of the popup menu inflating and handling
 * It has a few hooks around so that if the class wants customization they can add it on
 * without changing this class too much
 */
public abstract class PopupMenuHelper implements PopupMenu.OnMenuItemClickListener {
    protected Activity mActivity;
    protected PopupMenuType mType;
    public PopupMenuHelper(final Activity activity) {
        mActivity = activity;
    }

    /**
     * Call this to inflate and show the pop up menu
     *
     * @param view     the view to anchor the popup menu against
     * @param position the item that was clicked in the popup menu (or -1 if not relevant)
     */
    public void showPopupMenu(final View view, final int position) {
        // create the popup menu
        PopupMenu popupMenu = new PopupMenu(mActivity, view);
        final Menu menu = popupMenu.getMenu();

        // figure what type of pop up menu it is
        mType = onPreparePopupMenu(position);
        if (mType != null) {
            // inflate the menu
            switch (mType) {
                case Gif:
                    popupMenu.getMenuInflater().inflate(R.menu.menu_gif_item, menu);
                    break;
                case Video:
                    popupMenu.getMenuInflater().inflate(R.menu.menu_video_item, menu);
                    break;
                default:
                    break;
            }
            // hook up the click listener
            popupMenu.setOnMenuItemClickListener(this);
            // show it
            popupMenu.show();
        }
    }

    /**
     * This function allows classes to setup any variables before showing the popup menu
     *
     * @param position the position passed in from showPopupMenu
     * @return the pop up menu type, or null if we shouldn't show a pop up menu
     */
    public abstract PopupMenuType onPreparePopupMenu(final int position);

    @Override
    public boolean onMenuItemClick(MenuItem item) {


        return false;
    }


    // the different types of pop up menus
    public static enum PopupMenuType {
        Gif,
        Video,
    }
}
