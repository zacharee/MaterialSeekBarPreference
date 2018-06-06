package com.pavelsikun.seekbarpreference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.graphics.ColorUtils;
import android.transition.Slide;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rey.material.util.ColorUtil;
import com.rey.material.widget.Slider;

/**
 * Created by Pavel Sikun on 28.05.16.
 */

class PreferenceControllerDelegate implements Slider.OnPositionChangeListener, View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private static final int DEFAULT_CURRENT_VALUE = 50;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 100;
    private static final int DEFAULT_INTERVAL = 1;
    private static final boolean DEFAULT_DIALOG_ENABLED = true;
    private static final boolean DEFAULT_IS_ENABLED = true;

    private static final int DEFAULT_DIALOG_STYLE = R.style.MSB_Dialog_Default;

    private int maxValue;
    private int minValue;
    private int interval;
    private int currentValue;
    private String measurementUnit;
    private boolean dialogEnabled;

    private TextView valueView;
    private Slider seekBarView;
    private TextView measurementView;
    private LinearLayout valueHolderView;
    private FrameLayout bottomLineView;
    private LinearLayout buttonHolderView;
    private ImageView up;
    private ImageView down;
    private ImageView reset;

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
            interval = DEFAULT_INTERVAL;
            dialogEnabled = DEFAULT_DIALOG_ENABLED;

            isEnabled = DEFAULT_IS_ENABLED;
        }
        else {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
            try {
                interval = a.getInt(R.styleable.SeekBarPreference_msbp_interval, DEFAULT_INTERVAL);
                int saved_minValue = a.getInt(R.styleable.SeekBarPreference_msbp_minValue, DEFAULT_MIN_VALUE);
                minValue = saved_minValue / interval;
                int saved_maxValue = a.getInt(R.styleable.SeekBarPreference_msbp_maxValue, DEFAULT_MAX_VALUE);
                maxValue = (saved_maxValue) / interval;
                dialogEnabled = a.getBoolean(R.styleable.SeekBarPreference_msbp_dialogEnabled, DEFAULT_DIALOG_ENABLED);

                measurementUnit = a.getString(R.styleable.SeekBarPreference_msbp_measurementUnit);
                currentValue = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "defaultValue", DEFAULT_CURRENT_VALUE);

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

        if(isView) {
            titleView = view.findViewById(android.R.id.title);
            summaryView = view.findViewById(android.R.id.summary);

            titleView.setText(title);
            summaryView.setText(summary);
        }

        view.setClickable(false);

        seekBarView = view.findViewById(R.id.seekbar);
        measurementView = view.findViewById(R.id.measurement_unit);
        valueView = view.findViewById(R.id.seekbar_value);
        buttonHolderView = view.findViewById(R.id.button_holder);
        up = view.findViewById(R.id.up);
        down = view.findViewById(R.id.down);
        reset = view.findViewById(R.id.reset);

        up.setOnClickListener(this);
        down.setOnClickListener(this);
        reset.setOnClickListener(this);

        setMaxValue(maxValue);
        seekBarView.setOnPositionChangeListener(this);

        measurementView.setText(measurementUnit);

        setCurrentValue(currentValue);
        valueView.setText(String.valueOf(currentValue));

        bottomLineView = view.findViewById(R.id.bottom_line);
        valueHolderView = view.findViewById(R.id.value_holder);

        setDialogEnabled(dialogEnabled);
        setEnabled(isEnabled(), true);

        TypedArray colorAttr = context.getTheme().obtainStyledAttributes(new TypedValue().data, new int[] { R.attr.colorAccent });
        int color = colorAttr.getColor(0, 0);
        colorAttr.recycle();

        seekBarView.setPrimaryColor(color);
        seekBarView.setSecondaryColor(ColorUtils.setAlphaComponent(color, 0x33));
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

    @Override
    public void onPositionChanged(Slider view, boolean fromUser, float oldPos, float newPos, int oldValue, int newValue) {
        if (changeValueListener != null) {
            if (!changeValueListener.onChange(newValue)) return;
        }

        currentValue = newValue;
        valueView.setText(String.valueOf(newValue));
    }

//    @Override
//    public void onStartTrackingTouch(SeekBar seekBar) {
//    }
//
//    @Override
//    public void onStopTrackingTouch(SeekBar seekBar) {
//        setCurrentValue(currentValue);
//    }

    @Override
    public void onClick(final View v) {
        if (v == valueHolderView) {
            new CustomValueDialog(context, minValue, maxValue, currentValue)
                    .setPersistValueListener(new PersistValueListener() {
                        @Override
                        public boolean persistInt(int value) {
                            setCurrentValue(value);
                            seekBarView.setOnPositionChangeListener(null);
                            seekBarView.setValue(currentValue - minValue, false);
                            seekBarView.setOnPositionChangeListener(PreferenceControllerDelegate.this);

                            valueView.setText(String.valueOf(currentValue));
                            return true;
                        }
                    })
                    .show();
        } else if (v == up) {
            setCurrentValue(getCurrentValue() + 1);
        } else if (v == down) {
            setCurrentValue(getCurrentValue() - 1);
        } else if (v == reset) {
            if (changeValueListener != null) {
                changeValueListener.onReset();
            }
        }
    }


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
            valueView.setEnabled(enabled);
            valueHolderView.setClickable(enabled);
            valueHolderView.setEnabled(enabled);

            measurementView.setEnabled(enabled);
            bottomLineView.setEnabled(enabled);

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
            seekBarView.setValueRange(minValue, maxValue, false);

            seekBarView.setValue(currentValue, false);
        }
    }

    int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;

        if (seekBarView != null) {
            seekBarView.setValueRange(minValue, maxValue, false);

            seekBarView.setValue(currentValue, false);
        }
    }

    int getInterval() {
        return interval;
    }

    void setInterval(int interval) {
        this.interval = interval;
    }

    int getCurrentValue() {
        return currentValue;
    }

    void setCurrentValue(int value) {
        if(value < minValue) value = minValue;
        if(value > maxValue) value = maxValue;

        if (changeValueListener != null) {
            changeValueListener.onChange(value);
        }
        currentValue = value;
        if(seekBarView != null)
            seekBarView.setValue(currentValue, false);

        if(persistValueListener != null) {
            persistValueListener.persistInt(value);
        }
    }

    String getMeasurementUnit() {
        return measurementUnit;
    }

    void setMeasurementUnit(String measurementUnit) {
        this.measurementUnit = measurementUnit;
        if(measurementView != null) {
            measurementView.setText(measurementUnit);
        }
    }

    boolean isDialogEnabled() {
        return dialogEnabled;
    }

    void setDialogEnabled(boolean dialogEnabled) {
        this.dialogEnabled = dialogEnabled;

        if(valueHolderView != null && bottomLineView != null) {
            valueHolderView.setOnClickListener(dialogEnabled ? this : null);
            valueHolderView.setClickable(dialogEnabled);
            bottomLineView.setVisibility(dialogEnabled ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
