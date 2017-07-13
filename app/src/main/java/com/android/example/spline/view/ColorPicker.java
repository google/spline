/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.example.spline.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.android.example.spline.R;
import com.android.example.spline.databinding.LayoutColorPickerBinding;
import com.android.example.spline.model.Color;

/**
 * Interactive color picker custom view that inflates components from layout_color_picker,
 * including HuePicker and SaturationValuePicker, binding them to a spline Color object
 */

public class ColorPicker extends RelativeLayout {

    private LayoutColorPickerBinding mBinding;
    private Color mColor;

    public ColorPicker(Context context) {
        super(context);
        init(context);
    }

    public ColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        if(!isInEditMode()) {
            LayoutInflater inflater = LayoutInflater.from(context);
            mBinding = DataBindingUtil.inflate(
                    inflater, R.layout.layout_color_picker, this, true);

            if (mColor == null) {
                mColor = defaultColor();
            }
        }
    }

    public void setColor(Color color) {
        if (color != null) {
            mBinding.setColor(color);
        }
    }

    public Color defaultColor() {
        Color color = new Color();
        color.setAlpha(255);
        color.setValue(1.0f);
        color.setSaturation(1.0f);
        color.setHue(0);
        return color;
    }
}
