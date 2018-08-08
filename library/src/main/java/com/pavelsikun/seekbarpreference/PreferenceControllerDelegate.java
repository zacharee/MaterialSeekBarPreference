package com.pavelsikun.seekbarpreference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.text.DecimalFormat;

/**
 * Created by Pavel Sikun on 28.05.16.
 */

public class PreferenceControllerDelegate implements SeekBarView.SeekBarListener {
    public static final DecimalFormat FORMAT = new DecimalFormat("0.##");

    private final String TAG = getClass().getSimpleName();

    private static final int DEFAULT_CURRENT_VALUE = 50;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 100;
    private static final float DEFAULT_SCALE = 1f;
    private static final boolean DEFAULT_DIALOG_ENABLED = true;
    private static final boolean DEFAULT_IS_ENABLED = true;

    private int maxValue;
    private int minValue;
    private float scale;
    private int currentValue;
    private String measurementUnit;
    private boolean dialogEnabled;

//    private TextView valueView;
    private SeekBarView seekBarView;
//    private TextView measurementView;
//    private LinearLayout valueHolderView;
//    private FrameLayout bottomLineView;
//    private LinearLayout buttonHolderView;
//    private ImageView up;
//    private ImageView down;
//    private ImageView reset;

    //view stuff
    private TextView titleView, summaryView;
    private String title;
    private String summary;
    private boolean isEnabled;

    //controller stuff
    private boolean isView;
    private Context context;
    private ViewStateListener viewStateListener;
    private PersistValueListener persistValueListener;
    private ChangeValueListener changeValueListener;

    private int defaultValue;

    public static String formatValue(String value) {
        return FORMAT.format(Double.parseDouble(value));
    }

    interface ViewStateListener {
        boolean isEnabled();
        void setEnabled(boolean enabled);
    }

    PreferenceControllerDelegate(Context context, Boolean isView) {
        this.context = context;
        this.isView = isView;
    }

    void setPersistValueListener(PersistValueListener persistValueListener) {
        this.persistValueListener = persistValueListener;
    }

    void setViewStateListener(ViewStateListener viewStateListener) {
        this.viewStateListener = viewStateListener;
    }

    void setChangeValueListener(ChangeValueListener changeValueListener) {
        this.changeValueListener = changeValueListener;
    }

    void loadValuesFromXml(AttributeSet attrs) {
        if(attrs == null) {
            currentValue = DEFAULT_CURRENT_VALUE;
            minValue = DEFAULT_MIN_VALUE;
            maxValue = DEFAULT_MAX_VALUE;
            scale = DEFAULT_SCALE;
            dialogEnabled = DEFAULT_DIALOG_ENABLED;

            isEnabled = DEFAULT_IS_ENABLED;
        }
        else {
            try {
                @SuppressLint("PrivateApi")
                Class styleable = Class.forName("com.android.internal.R$styleable");

                Field prefResField = styleable.getDeclaredField("Preference");
                prefResField.setAccessible(true);

                Field defValField = styleable.getDeclaredField("Preference_defaultValue");
                defValField.setAccessible(true);

                int[] prefRes = (int[]) prefResField.get(null);
                int defValRes = (Integer) defValField.get(null);

                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
                TypedArray internal = context.obtainStyledAttributes(attrs, prefRes);

                try {
                    minValue = a.getInt(R.styleable.SeekBarPreference_msbp_minValue, DEFAULT_MIN_VALUE);
                    maxValue = (a.getInt(R.styleable.SeekBarPreference_msbp_maxValue, DEFAULT_MAX_VALUE));
                    dialogEnabled = a.getBoolean(R.styleable.SeekBarPreference_msbp_dialogEnabled, DEFAULT_DIALOG_ENABLED);

                    measurementUnit = a.getString(R.styleable.SeekBarPreference_msbp_measurementUnit);
                    currentValue = internal.getInt(defValRes, DEFAULT_CURRENT_VALUE);
                    defaultValue = internal.getInt(defValRes, DEFAULT_CURRENT_VALUE);

                    scale = a.getFloat(R.styleable.SeekBarPreference_msbp_scale, DEFAULT_SCALE);

                    if(isView) {
                        title = a.getString(R.styleable.SeekBarPreference_msbp_view_title);
                        summary = a.getString(R.styleable.SeekBarPreference_msbp_view_summary);
                        currentValue = a.getInt(R.styleable.SeekBarPreference_msbp_view_defaultValue, DEFAULT_CURRENT_VALUE);
                        defaultValue = a.getInt(R.styleable.SeekBarPreference_msbp_view_defaultValue, DEFAULT_CURRENT_VALUE);

                        isEnabled = a.getBoolean(R.styleable.SeekBarPreference_msbp_view_enabled, DEFAULT_IS_ENABLED);
                    }
                }
                finally {
                    a.recycle();
                    internal.recycle();
                }
            } catch (ClassNotFoundException ignored) {

            } catch (IllegalAccessException ignored) {

            } catch (NoSuchFieldException ignored) {}
        }
    }


    void onBind(View view) {
        LinearLayout widgetHolder = view.findViewById(R.id.layout_wrapper);
        seekBarView = (SeekBarView) LayoutInflater.from(context).inflate(R.layout.seekbar, widgetHolder, false);
        widgetHolder.addView(seekBarView);
        seekBarView.setDelegate(this);

        titleView = view.findViewById(android.R.id.title);
        summaryView = view.findViewById(android.R.id.summary);

        if(isView) {
            setTitle(title);
            setSummary(summary);
        }

        view.setClickable(false);

        seekBarView.onBind();
        setMaxValue(maxValue);
        setMinValue(minValue);
        setCurrentValue(currentValue);

        seekBarView.setOnProgressChangeListener(this);

        setDialogEnabled(dialogEnabled);
        setEnabled(isEnabled(), true);
        setMeasurementUnit(measurementUnit);
    }

    void onClick(View v) {
        seekBarView.onClick(v);
    }

    @Override
    public void onProgressChanged(int newValue) {
        if (changeValueListener != null) {
            if (!changeValueListener.onChange(newValue)) return;
        }

        currentValue = newValue;
    }

    @Override
    public void onProgressReset() {
        setCurrentValue(defaultValue);
        if (changeValueListener != null) {
            changeValueListener.onReset();
        }
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
        if(titleView != null) {
            if (title != null) titleView.setText(title);
            else titleView.setVisibility(View.GONE);
        }
    }

    String getSummary() {
        return summary;
    }

    void setSummary(String summary) {
        this.summary = summary;
        if(summaryView != null) {
            if (summary != null) summaryView.setText(summary);
            else summaryView.setVisibility(View.GONE);
        }
    }

    boolean isEnabled() {
        if(!isView && viewStateListener != null) {
            return viewStateListener.isEnabled();
        }
        else return isEnabled;
    }

    void setEnabled(boolean enabled, boolean viewsOnly) {
        Log.d(TAG, "setEnabled = " + enabled);
        isEnabled = enabled;

        if(viewStateListener != null && !viewsOnly) {
            viewStateListener.setEnabled(enabled);
        }

        if(seekBarView != null) { //theoretically might not always work
            Log.d(TAG, "view is disabled!");
            seekBarView.setEnabled(enabled);

            if(isView) {
                titleView.setEnabled(enabled);
                summaryView.setEnabled(enabled);
            }
        }
    }

    void setEnabled(boolean enabled) {
        setEnabled(enabled, false);
    }

    int getMaxValue() {
        return maxValue;
    }

    void setMaxValue(int maxValue) {
        this.maxValue = maxValue;

        if (seekBarView != null) {
            seekBarView.setValueRange(minValue, this.maxValue, false);
            seekBarView.setValue(currentValue, false);
        }
    }

    int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;

        if (seekBarView != null) {
            seekBarView.setValueRange(this.minValue, maxValue, false);
            seekBarView.setValue(currentValue, false);
        }
    }

    float getScale() {
        return scale;
    }

    void setScale(float scale) {
        this.scale = scale;
    }

    int getCurrentValue() {
        return currentValue;
    }

    void setCurrentValue(int value) {
        if(value < minValue) value = minValue;
        if(value > maxValue) value = maxValue;

        if (changeValueListener != null && currentValue != value) {
            if (!changeValueListener.onChange(value)) return;
        }
        currentValue = value;

        if(seekBarView != null) {
            seekBarView.setValue(currentValue, true);
        }

        if(persistValueListener != null) {
            persistValueListener.persistInt(value);
        }
    }

    void setDefaultValue(int value) {
        if (value < minValue) value = minValue;
        if (value > maxValue) value = maxValue;

        defaultValue = value;
    }

    int getDefaultValue() {
        return defaultValue;
    }

    float getCurrentScaledValue() {
        return currentValue * scale;
    }

    void setCurrentScaledValue(float value) {
        setCurrentValue((int)(value / scale));
    }

    String getMeasurementUnit() {
        return measurementUnit;
    }

    void setMeasurementUnit(String measurementUnit) {
        this.measurementUnit = measurementUnit;
        if (seekBarView != null) seekBarView.getMeasurementView().setText(measurementUnit);
    }

    boolean isDialogEnabled() {
        return dialogEnabled;
    }

    void setDialogEnabled(boolean dialogEnabled) {
        this.dialogEnabled = dialogEnabled;
        if (seekBarView != null) seekBarView.setDialogEnabled(dialogEnabled);
    }
}
