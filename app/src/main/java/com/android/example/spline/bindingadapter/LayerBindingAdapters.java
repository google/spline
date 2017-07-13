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
package com.android.example.spline.bindingadapter;

import android.content.res.ColorStateList;
import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputEditText;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class defines a BindingAdpater and InverseBindingAdapter for converting Strings
 * representing position and dimension values of a layer into floats for saving to the layer
 * object, and vice-versa.
 */
public class LayerBindingAdapters {

    /* These first set of adapters exist to convert resource ID's to the respective colors or
       drawables they represent. This is an unfortunate side effect of moving from resource literals
       in layout file binding expressions to viewmodel methods that return an ID. While databinding
       knows how to treat the literals, it assumes an int bound to a color or drawable attribute
       is a color, rather than a resource ID. */

    /**
     * Binding adapter for converting a color resource ID to a color int that can be set as the
     * background of the passed view
     * @param view
     * @param resId
     */
    @BindingAdapter("android:background")
    public static void setBackgroundColor(LinearLayout view, int resId) {
        int bgColor = view.getContext().getColor(resId);
        view.setBackgroundColor(bgColor);
    }

    /**
     * Binding adapter for converting a drawable resource ID to a drawable that can be set as the
     * image drawable of the passed ImageView
     * @param view
     * @param resId
     */
    @BindingAdapter("android:src")
    public static void setImage(ImageView view, int resId) {
        Drawable d = view.getContext().getDrawable(resId);
        view.setImageDrawable(d);
    }

    /**
     * Binding adapter for converting a color resource ID to a color int that can be set as the
     * tint of the passed CheckBox
     * @param view
     * @param resId
     */
    @BindingAdapter("android:buttonTint")
    public static void setButtonTint(CheckBox view, int resId) {
        int tintColor = view.getContext().getColor(resId);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{new int[]{}},
                new int[]{
                        tintColor
                }
        );
        view.setButtonTintList(colorStateList);
    }

    /**
     * Binding adapter for converting a color resource ID to a color int that can be set as the
     * tint of the passed ImageView
     * @param view
     * @param resId
     */
    @BindingAdapter("android:tint")
    public static void setImageTint(ImageView view, int resId) {
        int tintColor = view.getContext().getColor(resId);
        view.setColorFilter(tintColor);
    }

    /**
     * Binding adapter for converting a color resource ID to a color int that can be set as the
     * text color of the passed TextView
     * @param view
     * @param resId
     */
    @BindingAdapter("android:textColor")
    public static void setTextColor(TextView view, int resId) {
        int textColor = view.getContext().getColor(resId);
        view.setTextColor(textColor);
    }

    /**
     * Binding adapter for converting view position and dimension values to text for EditTexts. The
     * InverseBindingAdapter that follows does the reverse
     * @param view
     * @param dimValue
     */
    @BindingAdapter("android:text")
    public static void setText(TextInputEditText view, float dimValue) {
        String curString = view.getText().toString();
        int curInt;
        try {
            curInt = Integer.parseInt(curString);
        } catch (NumberFormatException e) {
            curInt = 0;
        }

        // Do not set the text if the parsed string is null, if it ends in a . or if it is equal to
        // the current value to avoid the value changing under the user's cursor (and the cursor
        // jumping) as they type.
        if (curString == null
                || curString.length() == 0
                || curString.charAt(curString.length() - 1) != '.'
                && dimValue != curInt) {

            String dimString = "";
            if (dimValue == (int) dimValue) {
                dimString = Integer.toString((int) dimValue);
            } else {
                dimString = Float.toString(dimValue);
            }

            if (dimString != null && view != null && view.getText() != null &&
                    !dimString.equals(view.getText().toString())) {
                view.setText(dimString);
            }
        }
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static float getText(TextInputEditText dimView) {
        try {
            String dimString = dimView.getText().toString();
            float dimVal = Float.parseFloat(dimString);
            return dimVal;
        } catch (NumberFormatException e) {
            // Return 0 for now
            return 0;
        }
    }
}
