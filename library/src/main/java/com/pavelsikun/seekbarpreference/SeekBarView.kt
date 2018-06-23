package com.pavelsikun.seekbarpreference

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.graphics.ColorUtils
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.pavelsikun.seekbarpreference.PreferenceControllerDelegate.formatValue
import com.rey.material.widget.Slider

class SeekBarView : ConstraintLayout, View.OnClickListener, Slider.OnPositionChangeListener {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr)

    lateinit var seekBar: Slider
    lateinit var valueView: AppCompatTextView
    lateinit var measurementView: AppCompatTextView
    lateinit var valueHolderView: LinearLayout
    lateinit var buttonHolderView: LinearLayout
    lateinit var bottomLineView: FrameLayout
    lateinit var up: ImageView
    lateinit var down: ImageView
    lateinit var reset: ImageView
    lateinit var delegate: PreferenceControllerDelegate

    var dialogEnabled = true
        set(value) {
            field = value
            valueHolderView.isClickable = value
            valueHolderView.isEnabled = value
            valueHolderView.setOnClickListener(if (value) this else null)
            bottomLineView.visibility = if (value) View.VISIBLE else View.INVISIBLE
        }

    private var listener: SeekBarListener? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        seekBar = findViewById(R.id.seekbar)
        valueView = findViewById(R.id.seekbar_value)
        measurementView = findViewById(R.id.measurement_unit)
        valueHolderView = findViewById(R.id.value_holder)
        buttonHolderView = findViewById(R.id.button_holder)
        bottomLineView = findViewById(R.id.bottom_line)
        up = findViewById(R.id.up)
        down = findViewById(R.id.down)
        reset = findViewById(R.id.reset)

        val colorAttr = context.theme.obtainStyledAttributes(TypedValue().data, intArrayOf(R.attr.colorAccent))
        val color = colorAttr.getColor(0, 0)
        colorAttr.recycle()

        seekBar.setPrimaryColor(color)
        seekBar.setSecondaryColor(ColorUtils.setAlphaComponent(color, 0x33))

        up.setOnClickListener(this)
        down.setOnClickListener(this)
        reset.setOnClickListener(this)
        valueHolderView.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when {
            v.id == R.id.value_holder -> CustomValueDialog(context, delegate.minValue, delegate.maxValue, delegate.currentValue, delegate.scale)
                    .setPersistValueListener { value ->
                        delegate.currentValue = value
                        true
                    }
                    .show()
            v.id == R.id.up -> {
                delegate.currentValue += 1
            }
            v.id == R.id.down -> {
                delegate.currentValue -= 1
            }
            v.id == R.id.reset -> {
                listener?.onProgressReset()
            }
        }
    }

    override fun onPositionChanged(view: Slider?, fromUser: Boolean, oldPos: Float, newPos: Float, oldValue: Int, newValue: Int) {
        listener?.onProgressChanged(newValue)
        valueView.text = formatValue((delegate.currentScaledValue).toString())

        updateFill(newValue)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        valueView.isEnabled = enabled
        valueHolderView.isClickable = enabled
        valueHolderView.isEnabled = enabled

        seekBar.isEnabled = enabled
        seekBar.isClickable = enabled

        measurementView.isEnabled = enabled
        bottomLineView.isEnabled = enabled
        buttonHolderView.isEnabled = enabled
        up.isEnabled = enabled
        down.isEnabled = enabled
        reset.isEnabled = enabled
    }

    fun onBind() {
        seekBar.setValueRange(delegate.minValue, delegate.maxValue, false)
        setValue(delegate.currentValue.toFloat(), false)
        seekBar.setOnPositionChangeListener(this)
    }

    fun setOnProgressChangeListener(listener: SeekBarListener) {
        this.listener = listener
    }

    fun setValue(value: Float, animate: Boolean) {
        seekBar.setOnPositionChangeListener(null)
        seekBar.setValue(value,true)
        seekBar.setOnPositionChangeListener(this)

        valueView.text = formatValue((delegate.currentScaledValue).toString())

        updateFill(value.toInt())
    }

    fun setValueRange(min: Int, max: Int, animate: Boolean) {
        seekBar.setValueRange(min, max, animate)
    }

    private fun updateFill(value: Int) {
        if (!seekBar.isThumbStrokeAnimatorRunning) {
            if (value == delegate.defaultValue)
                seekBar.setThumbFillPercent(0)
            else
                seekBar.setThumbFillPercent(1)
        }
    }

    interface SeekBarListener {
        fun onProgressChanged(newValue: Int)
        fun onProgressReset()
    }
}