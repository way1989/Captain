package com.way.captain.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.way.captain.R;

/**
 * Created by android on 16-2-3.
 */
public class GifUtils {

    public static final int DEFAULT_GIF_LENGTH = 30;
    public static final int MIN_GIF_LENGTH = 2;
    public static final int MAX_GIF_LENGTH = 30;
    public static final int DEFAULT_GIF_SIZE = 9;
    public static final int DEFAULT_GIF_FRAME = 12;
    public static final String KEY_GIF_LENGTH = "gif_length";
    public static final String KEY_GIF_FRAME = "gif_frame";
    public static final String KEY_GIF_SIZE = "gif_size";

    public static String getVideo2gifCommand(long start, long length, int frame,
                                             String sourcePath, String outPath, int width, int height) {
        StringBuilder command = new StringBuilder("-ss ");
        command.append(start);
        command.append(" -t ");
        command.append(length);
        command.append(" -i ");
        command.append(sourcePath);
        command.append(" -s ");
        command.append(width + "x" + height);
        command.append(" -f ");
        command.append("gif");
        command.append(" -r ");
        command.append(frame);
        command.append(" ");
        command.append(outPath);
        Log.i("broncho", "command = " + command.toString());
        return command.toString();
    }

    public static void lengthPicker(final Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        int length = prefs.getInt(KEY_GIF_LENGTH, DEFAULT_GIF_LENGTH);
        final SharedPreferences.Editor editor = prefs.edit();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.number_picker, null);
        TextView unitTextView = (TextView) view.findViewById(R.id.unit);
        TextView tipsTextView = (TextView) view.findViewById(R.id.tips);
        unitTextView.setText(R.string.gif_max_time_length_unit);
        tipsTextView.setText(R.string.max_time_setting_tips);
        NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.numberPicker);
        numberPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        numberPicker.setMinValue(2);
        numberPicker.setMaxValue(30);
        numberPicker.setValue(length);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                editor.putInt(KEY_GIF_LENGTH, newVal).apply();
            }
        });
        builder.setTitle(R.string.gif_max_length_setting_dialog_title);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNeutralButton(R.string.default_values, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putInt(KEY_GIF_LENGTH, DEFAULT_GIF_LENGTH).apply();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public static void framePicker(final Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        int length = prefs.getInt(KEY_GIF_FRAME, DEFAULT_GIF_FRAME);
        final SharedPreferences.Editor editor = prefs.edit();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.number_picker, null);
        TextView unitTextView = (TextView) view.findViewById(R.id.unit);
        TextView tipsTextView = (TextView) view.findViewById(R.id.tips);
        unitTextView.setText(R.string.gif_rate_unit);
        tipsTextView.setText(R.string.rate_rang_setting_tips);
        NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.numberPicker);
        numberPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        numberPicker.setMinValue(12);
        numberPicker.setMaxValue(25);
        numberPicker.setValue(length);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                editor.putInt(KEY_GIF_FRAME, newVal).apply();
            }
        });
        builder.setTitle(R.string.gif_rate_setting_dialog_title);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNeutralButton(R.string.default_values, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putInt(KEY_GIF_FRAME, DEFAULT_GIF_FRAME).apply();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public static void sizePicker(final Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        int length = prefs.getInt(KEY_GIF_SIZE, DEFAULT_GIF_SIZE);
        final SharedPreferences.Editor editor = prefs.edit();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.number_picker, null);
        TextView unitTextView = (TextView) view.findViewById(R.id.unit);
        TextView tipsTextView = (TextView) view.findViewById(R.id.tips);
        unitTextView.setText(R.string.gif_size_unit);
        tipsTextView.setText(R.string.gif_size_tips);
        NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.numberPicker);
        numberPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(9);
        numberPicker.setValue(length);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                editor.putInt(KEY_GIF_SIZE, newVal).apply();
            }
        });
        builder.setTitle(R.string.gif_quality_setting_dialog_title);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNeutralButton(R.string.default_values, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putInt(KEY_GIF_SIZE, DEFAULT_GIF_SIZE).apply();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
}
