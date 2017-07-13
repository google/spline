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
import android.databinding.InverseBindingListener;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.example.spline.R;
import com.android.example.spline.model.Color;

/**
 * Two-dimensional selector that allows the user to select a hue and saturation value with touch
 */
public class SaturationValuePicker extends RelativeLayout {

    private static final int SELECTOR_PADDING_DP = 16;
    private static final int SELECTOR_RADIUS_DP = 12;
    private static final int SELECTOR_ELEVATION_DP = 6;

    private float mDensity;
    private float mSelectorRadius;
    private float mInset;
    private int mPadding;

    private float mWidth;
    private float mHeight;
    private Color mColor;
    private float mHue;
    private float mSaturation;
    private float mValue;
    private SaturationValueDrawable mSVDrawable;
    private View mSelector;
    private MarginLayoutParams mSelectorParams;
    private GradientDrawable mSelectorDrawable;
    private InverseBindingListener mSaturationAttrChangedListener;
    private InverseBindingListener mValueAttrChangedListener;

    public SaturationValuePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        mColor = new Color();

        setClipChildren(false);
        setClipToPadding(false);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mDensity = metrics.density;

        mSelectorRadius = SELECTOR_RADIUS_DP * mDensity;

        View v = new View(context);
        v.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // We want the selector to protrude as minimally as possible from the selection drawable and
        // we want to inset the gradients in the drawable so that the color under the selector's
        // midpoint always reflects the current color (thus the limits of the saturation-value
        // gradients have to be clamped at this inset. The inset value is thus the shorter side of
        // a 45 degree triangle (1 / root 2) with hypotnus equal to the radius of the selector.
        mInset = mSelectorRadius / (float) Math.sqrt(2);
        mSVDrawable = new SaturationValueDrawable(mInset);
        v.setBackground(mSVDrawable);
        addView(v);

        mSelectorParams = new LayoutParams(
                Math.round(mSelectorRadius * 2), Math.round(mSelectorRadius * 2)
        );
        mSelector = new View(context);
        mSelector.setLayoutParams(mSelectorParams);
        mSelectorDrawable = (GradientDrawable) ContextCompat.getDrawable(
                context, R.drawable.drawable_selector);
        mSelector.setBackground(mSelectorDrawable);
        mSelector.setElevation(SELECTOR_ELEVATION_DP * mDensity);
        addView(mSelector);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MarginLayoutParams params = (MarginLayoutParams) getLayoutParams();
        mPadding = Math.round(SELECTOR_PADDING_DP * mDensity);

        // If we're attached and the parent supports margins, add padding to the view and reverse it
        // with negative margins, allowing room for the selector to protrude from the drawable.
        if (params != null) {
            setPadding(mPadding, mPadding, mPadding, mPadding);
            params.setMargins(-mPadding, -mPadding, -mPadding, -mPadding);
            setLayoutParams(params);
        }
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            mWidth = Math.round(getWidth() - (mPadding + mInset) * 2);
            mHeight = Math.round(getHeight() - (mPadding + mInset) * 2);
            updateSelectorPos();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - mPadding;
        float y = event.getY() - mPadding;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Reject touches in the padding region
                if (x < 0 || y < 0 || x > mWidth + mInset * 2 || y > mHeight + mInset * 2) {
                    return false;
                }
            case MotionEvent.ACTION_MOVE:
                setSaturation(getSaturationForX(x - mInset));
                setValue(getValueForY(y - mInset));
                break;
        }

        return true;
    }

    public void setHue(float hue) {
        if (hue != mHue) {
            mHue = hue;
            mColor.setHue(hue);
            mSVDrawable.setHue(hue);
            updateSelectorColor();
        }
    }

    public float getSaturation() {
        return mSaturation;
    }

    public void setSaturation(float saturation) {
        if (saturation != mSaturation) {
            mSaturation = saturation;
            mColor.setSaturation(saturation);
            updateSelectorColor();
            updateSelectorX();
            if (mSaturationAttrChangedListener != null) {
                mSaturationAttrChangedListener.onChange();
            }
        }
    }

    public float getValue() {
        return mValue;
    }

    public void setValue(float value) {
        if (value != mValue) {
            mValue = value;
            mColor.setValue(mValue);
            updateSelectorColor();
            updateSelectorY();
            if (mValueAttrChangedListener != null) {
                mValueAttrChangedListener.onChange();
            }
        }
    }

    private void updateSelectorColor() {
        mSelectorDrawable.setColor(mColor.getColor());
    }

    private void updateSelectorPos() {
        updateSelectorX();
        updateSelectorY();
    }

    private float getXForSaturation(float saturation) {
        return mWidth * saturation;
    }

    private float getSaturationForX(float x) {
        return x / mWidth;
    }

    private float getYForValue(float value) {
        return mHeight * (1.0f - value);
    }

    private float getValueForY(float y) {
        return 1.0f - (y / mHeight);
    }

    private void updateSelectorX() {
        float x = getXForSaturation(mSaturation) + mInset - mSelectorRadius;
        mSelector.setTranslationX(x);
    }

    private void updateSelectorY() {
        float y = getYForValue(mValue) + mInset - mSelectorRadius;
        mSelector.setTranslationY(y);
    }

    public void setSaturationAttrChanged(InverseBindingListener listener) {
        mSaturationAttrChangedListener = listener;
    }

    public void setValueAttrChanged(InverseBindingListener listener) {
        mValueAttrChangedListener = listener;
    }
}
