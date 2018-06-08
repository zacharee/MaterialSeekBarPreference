package com.pavelsikun.seekbarpreference

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Extension of ImageView that correctly applies maxWidth and maxHeight.
 */
class PreferenceImageView : ImageView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onMeasure(widthMeasureSpecVal: Int, heightMeasureSpecVal: Int) {
        var widthMeasureSpec = widthMeasureSpecVal
        var heightMeasureSpec = heightMeasureSpecVal
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        if (widthMode == View.MeasureSpec.AT_MOST || widthMode == View.MeasureSpec.UNSPECIFIED) {
            val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
            val maxWidth = maxWidth
            if (maxWidth != Integer.MAX_VALUE && (maxWidth < widthSize || widthMode == View.MeasureSpec.UNSPECIFIED)) {
                widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST)
            }
        }

        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode == View.MeasureSpec.AT_MOST || heightMode == View.MeasureSpec.UNSPECIFIED) {
            val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
            val maxHeight = maxHeight
            if (maxHeight != Integer.MAX_VALUE && (maxHeight < heightSize || heightMode == View.MeasureSpec.UNSPECIFIED)) {
                heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST)
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
