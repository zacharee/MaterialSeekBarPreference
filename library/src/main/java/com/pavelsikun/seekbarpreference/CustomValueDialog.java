package com.pavelsikun.seekbarpreference;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

/**
 * Created by Pavel Sikun on 21.05.16.
 */
class CustomValueDialog {

    private final String TAG = getClass().getSimpleName();

    private Dialog dialog;
    private EditText customValueView;

    private float minValue, maxValue, currentValue, scale;
    private PersistValueListener persistValueListener;

    CustomValueDialog(Context context, int minValue, int maxValue, int currentValue, float scale) {
        this.scale = scale;
        this.minValue = minValue * scale;
        this.maxValue = maxValue * scale;
        this.currentValue = currentValue * scale;

        init(new AlertDialog.Builder(context));
    }

    private void init(AlertDialog.Builder dialogBuilder) {
        View dialogView = LayoutInflater.from(dialogBuilder.getContext()).inflate(R.layout.value_selector_dialog, null);
        dialog = dialogBuilder.setView(dialogView).create();

        TextView minValueView = dialogView.findViewById(R.id.minValue);
        TextView maxValueView = dialogView.findViewById(R.id.maxValue);
        customValueView = dialogView.findViewById(R.id.customValue);

        minValueView.setText(PreferenceControllerDelegate.formatValue(String.valueOf(minValue)));
        maxValueView.setText(PreferenceControllerDelegate.formatValue(String.valueOf(maxValue)));
        customValueView.setHint(PreferenceControllerDelegate.formatValue(String.valueOf(currentValue)));

        LinearLayout colorView = dialogView.findViewById(R.id.dialog_color_area);
        colorView.setBackgroundColor(fetchAccentColor(dialogBuilder.getContext()));

        Button applyButton = dialogView.findViewById(R.id.btn_apply);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryApply();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private int fetchAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);
        a.recycle();

        return color;
    }

    CustomValueDialog setPersistValueListener(PersistValueListener listener) {
        persistValueListener = listener;
        return this;
    }

    void show() {
        dialog.show();
    }

    private void tryApply() {
        float value;

        try {
            value = Float.parseFloat(customValueView.getText().toString());
            if (value > maxValue) {
                Log.e(TAG, "wrong input( > than required): " + customValueView.getText().toString());
                notifyWrongInput();
                return;
            }
            else if (value < minValue) {
                Log.e(TAG, "wrong input( < then required): " + customValueView.getText().toString());
                notifyWrongInput();
                return;
            }
        }
        catch (Exception e) {
            Log.e(TAG, "wrong input(non-integer): " + customValueView.getText().toString());
            notifyWrongInput();
            return;
        }

        if(persistValueListener != null) {
            persistValueListener.persistInt((int)(value / scale));
            dialog.dismiss();
        }
    }

    private void notifyWrongInput() {
        customValueView.setText("");
        customValueView.setHint("Wrong Input!");
    }
}
