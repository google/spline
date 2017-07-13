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
package com.android.example.spline.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

/**
 * Data model class that wraps a standard Android color int with observable Hue, Saturation, Value,
 * Red, Green and Blue fields. The HSV and color fields are updated whenever there is a change in
 * RGB values and vice-versa.
 */
public class Color extends BaseObservable {

    private int color;
    private float hue;
    private float saturation;
    private float value;
    private int red;
    private int blue;
    private int green;
    private int alpha;

    public Color() {
        alpha = 255;
    }

    public Color(int color) {
        this.color = color;
        updateValues();
    }

    public Color(Color color) {
        if (color != null) {
            this.color = color.getColor();
        }
        updateValues();
    }

    public void updateValues() {
        updateValues(true);
    }

    public void updateValues(boolean updateHSV) {
        if (updateHSV) {
            float[] hsv = new float[3];
            android.graphics.Color.colorToHSV(this.color, hsv);

            this.hue = hsv[0];
            this.saturation = hsv[1];
            this.value = hsv[2];
        }

        int oldRgbColor = android.graphics.Color.argb(this.alpha, this.red, this.green, this.blue);
        float[] oldRgbHsv = new float[3];
        android.graphics.Color.colorToHSV(oldRgbColor, oldRgbHsv);

        if (this.hue != oldRgbHsv[0] ||
                this.saturation != oldRgbHsv[1] ||
                this.value != oldRgbHsv[2]) {
            this.red = android.graphics.Color.red(this.color);
            this.green = android.graphics.Color.green(this.color);
            this.blue = android.graphics.Color.blue(this.color);
            this.alpha = android.graphics.Color.alpha(this.color);
        }
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
        updateValues();
        notifyChange();
    }

    /**
     * The following getters and setters with String in the name are a work around for 2-way data
     * binding of these integer properties to the text attribute of EditText fields. As of Android
     * Studio 2.3, should now use the InverseMethod annotation on standalone converter classes
     * inverse conversion method to achieve this type conversion. These were maintained here because
     * the circular dependencies of color properties on each other make the double bind calls
     * necessitated by the converter classes causes a drift in color properties as one of the
     * properties is adjusted.
     *
     * @return a String to be used for 2-way binding
     */
    public String getHexString() {
        String s = Integer.toHexString(getColor());
        if (s.length() >= 2) {
            return "#" + s.substring(2);
        } else {
            return "#000000";
        }
    }

    public void setHexString(String hexString) {
        if (!getHexString().equals(hexString)) {
            // Only accept a string as a valid color if it contains 6 digits and '#'
            if (hexString.length() == 7) {
                int color = android.graphics.Color.parseColor(hexString);
                setColor(color);
            }
        }
    }

    public String getRedString() {
        return Integer.toString(this.red);
    }

    public void setRedString(String redString) {
        try {
            int r = Integer.parseInt(redString);
            if (r != getRed()) {
                setRed(r);
            }
        } catch (NumberFormatException e) {
            // Don't do anything for now
        }
    }

    public String getGreenString() {
        return Integer.toString(this.green);
    }

    public void setGreenString(String greenString) {
        try {
            int g = Integer.parseInt(greenString);
            if (g != getGreen()) {
                setGreen(g);
            }
        } catch (NumberFormatException e) {
            // Don't do anything for now
        }
    }

    public String getBlueString() {
        return Integer.toString(this.blue);
    }

    public void setBlueString(String blueString) {
        try {
            int b = Integer.parseInt(blueString);
            if (b != getBlue()) {
                setBlue(b);
            }
        } catch (NumberFormatException e) {
            // Don't do anything for now
        }
    }

    @Bindable
    public float getHue() {
        return this.hue;
    }

    public void setHue(float hue) {
        float[] hsv = {hue, this.saturation, this.value};
        this.color = android.graphics.Color.HSVToColor(this.alpha, hsv);
        updateValues(false);
        this.hue = Math.min(Math.max(0f, hue), 359.9f);

        // For each HSV and RGB property, a change in the property changes both the property itself
        // and the resultant color
        notifyPropertyChanged(BR.hue);
        notifyPropertyChanged(BR.color);
    }

    @Bindable
    public float getSaturation() {
        return this.saturation;
    }

    public void setSaturation(float saturation) {
        float[] hsv = {this.hue, saturation, this.value};
        this.color = android.graphics.Color.HSVToColor(this.alpha, hsv);
        updateValues(false);
        // Clamp the saturation value
        this.saturation = Math.min(Math.max(0f, saturation), 1.0f);

        notifyPropertyChanged(BR.saturation);
        notifyPropertyChanged(BR.color);
    }

    @Bindable
    public float getValue() {
        return this.value;
    }

    public void setValue(float value) {
        float[] hsv = {this.hue, this.saturation, value};
        this.color = android.graphics.Color.HSVToColor(this.alpha, hsv);
        updateValues(false);
        // Clamp the value
        this.value = Math.min(Math.max(0f, value), 1.0f);

        notifyPropertyChanged(BR.value);
        notifyPropertyChanged(BR.color);
    }

    public int getRed() {
        return this.red;
    }

    public void setRed(int red) {
        this.color = android.graphics.Color.argb(this.alpha, red, this.green, this.blue);
        updateValues();
        // Setting red will modify HSV and color properties, so notifyingChange is simpler syntax
        // than calling notifyPropertyChanged on five different Binding Resources
        notifyChange();
    }

    public int getGreen() {
        return this.green;
    }

    public void setGreen(int green) {
        this.color = android.graphics.Color.argb(this.alpha, this.red, green, this.blue);
        updateValues();
        notifyChange();
    }

    public int getBlue() {
        return this.blue;
    }

    public void setBlue(int blue) {
        this.color = android.graphics.Color.argb(this.alpha, this.red, this.green, blue);
        updateValues();
        notifyChange();
    }

    @Bindable
    public int getAlpha() {
        return this.alpha;
    }

    public void setAlpha(int alpha) {
        this.color = android.graphics.Color.argb(alpha, this.red, this.green, this.blue);
        updateValues();

        notifyPropertyChanged(BR.alpha);
    }
}
