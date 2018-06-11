package com.pavelsikun.seekbarpreference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private boolean isView = false;
    private Context context;
    private ViewStateListener viewStateListener;
    private PersistValueListener persistValueListener;
    private ChangeValueListener changeValueListener;

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
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
            try {
                minValue = a.getInt(R.styleable.SeekBarPreference_msbp_minValue, DEFAULT_MIN_VALUE);
                maxValue = (a.getInt(R.styleable.SeekBarPreference_msbp_maxValue, DEFAULT_MAX_VALUE));
                dialogEnabled = a.getBoolean(R.styleable.SeekBarPreference_msbp_dialogEnabled, DEFAULT_DIALOG_ENABLED);

                measurementUnit = a.getString(R.styleable.SeekBarPreference_msbp_measurementUnit);
                currentValue = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "defaultValue", DEFAULT_CURRENT_VALUE);

                scale = a.getFloat(R.styleable.SeekBarPreference_msbp_scale, DEFAULT_SCALE);

                if(isView) {
                    title = a.getString(R.styleable.SeekBarPreference_msbp_view_title);
                    summary = a.getString(R.styleable.SeekBarPreference_msbp_view_summary);
                    currentValue = a.getInt(R.styleable.SeekBarPreference_msbp_view_defaultValue, DEFAULT_CURRENT_VALUE);

                    isEnabled = a.getBoolean(R.styleable.SeekBarPreference_msbp_view_enabled, DEFAULT_IS_ENABLED);
                }
            }
            finally {
                a.recycle();
            }
        }
    }


    void onBind(View view) {
        LinearLayout widgetHolder = view.findViewById(R.id.layout_wrapper);
        seekBarView = (SeekBarView) LayoutInflater.from(context).inflate(R.layout.seekbar, widgetHolder, false);
        widgetHolder.addView(seekBarView);
        seekBarView.setDelegate(this);

        if(isView) {
            titleView = view.findViewById(android.R.id.title);
            summaryView = view.findViewById(android.R.id.summary);

            titleView.setText(title);
            summaryView.setText(summary);
        }

        view.setClickable(false);

//        seekBarView = view.findViewById(R.id.seekbar);
//        measurementView = view.findViewById(R.id.measurement_unit);
//        valueView = view.findViewById(R.id.seekbar_value);
//        buttonHolderView = view.findViewById(R.id.button_holder);
//        up = view.findViewById(R.id.up);
//        down = view.findViewById(R.id.down);
//        reset = view.findViewById(R.id.reset);

//        up.setOnClickListener(this);
//        down.setOnClickListener(this);
//        reset.setOnClickListener(this);

        seekBarView.onBind();
        setMaxValue(maxValue);
        setMinValue(minValue);
        setCurrentValue(currentValue);

        seekBarView.setOnProgressChangeListener(this);

//        measurementView.setText(measurementUnit);
//        valueView.setText(formatValue(String.valueOf(currentValue * scale)));

//        bottomLineView = view.findViewById(R.id.bottom_line);
//        valueHolderView = view.findViewById(R.id.value_holder);

        setDialogEnabled(dialogEnabled);
        setEnabled(isEnabled(), true);
        setMeasurementUnit(measurementUnit);
    }

//    @Override
//    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        int newValue = minValue + (progress * interval);
//
//        if (changeValueListener != null) {
//            if (!changeValueListener.onChange(newValue)) {
//                return;
//            }
//        }
//        currentValue = newValue;
//        valueView.setText(String.valueOf(newValue));
//    }

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
        if (changeValueListener != null) {
            changeValueListener.onReset();
        }
    }

    //    @Override
//    public void onStartTrackingTouch(SeekBar seekBar) {
//    }
//
//    @Override
//    public void onStopTrackingTouch(SeekBar seekBar) {
//        setCurrentValue(currentValue);
//    }


    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
        if(titleView != null) {
            titleView.setText(title);
        }
    }

    String getSummary() {
        return summary;
    }

    void setSummary(String summary) {
        this.summary = summary;
        if(seekBarView != null) {
            summaryView.setText(summary);
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
        if (seekBarView != null) seekBarView.measurementView.setText(measurementUnit);
    }

    boolean isDialogEnabled() {
        return dialogEnabled;
    }

    void setDialogEnabled(boolean dialogEnabled) {
        this.dialogEnabled = dialogEnabled;
        if (seekBarView != null) seekBarView.setDialogEnabled(dialogEnabled);
    }
}
