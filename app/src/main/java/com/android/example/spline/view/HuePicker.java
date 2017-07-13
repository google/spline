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
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.example.spline.R;
import com.android.example.spline.model.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Horizontal slider control for selecting a hue value. Roughly resembles a SeekBar widget with a
 * gradient background.
 */
public class HuePicker extends RelativeLayout {

    private static final float MIN_HUE = 0f;
    private static final float MAX_HUE = 360f;

    private static final int PADDING_DP = 16;
    private static final int SELECTOR_RADIUS_DP = 8;
    private static final int SELECTOR_ELEVATION_DP = 6;
    private static final int BACKGROUND_HEIGHT_DP = 8;

    private float mDensity;
    private float mSelectorRadius;
    private float mInset;
    private int mPadding;
    private float mWidth;

    private View mSelector;
    private GradientDrawable mSelectorDrawable;
    private MarginLayoutParams mSelectorParams;
    private View mBackground;

    private Color mHueColor;
    private float mHue;
    private List<OnHueChangeListener> mListeners;

    public interface OnHueChangeListener {
        public void onHueChange(HuePicker huePicker, float hue);
    }

    public HuePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        mListeners = new ArrayList<OnHueChangeListener>();

        setClipChildren(false);
        setClipToPadding(false);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mDensity = metrics.density;

        mSelectorRadius = SELECTOR_RADIUS_DP * mDensity;

        // We want the selector to protrude as minimally as possible from the background drawable
        // and we want to inset the gradients in the drawable so that the color under the
        // selector's midpoint always reflects the current color (thus the limits of the background
        // gradient have to be clamped at this inset. The inset value is thus the base side of a
        // triangle extending from the midpoint of the selector placed in the desired position, a
        // hypotenuse equal to the selector radius and a height side equal to half of the height of
        // the background drawable.
        float height = BACKGROUND_HEIGHT_DP * mDensity / 2.0f;
        mInset = Math.round(
                Math.sqrt(mSelectorRadius * mSelectorRadius - height * height)
        );

        mSelectorParams = new LayoutParams(
                Math.round(mSelectorRadius * 2), Math.round(mSelectorRadius * 2)
        );

        mBackground = new View(context);
        mBackground.setBackground(new HueDrawable());
        addView(mBackground);

        mSelector = new View(getContext());
        mSelector.setLayoutParams(mSelectorParams);
        mSelectorDrawable = (GradientDrawable) ContextCompat.getDrawable(
                getContext(),
                R.drawable.drawable_selector);
        mSelector.setBackground(mSelectorDrawable);
        mSelector.setElevation(SELECTOR_ELEVATION_DP * mDensity);
        addView(mSelector);

        // Full saturation / full value color used for the selector background
        mHueColor = new Color(0xffff0000);
        updateSelectorColor();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MarginLayoutParams params = (MarginLayoutParams) getLayoutParams();
        mPadding = Math.round(PADDING_DP * mDensity);

        // If we're attached and the parent supports margins, add padding to the view and reverse it
        // with negative margins, allowing room for the selector to protrude from the drawable.
        if (params != null) {
            setPadding(mPadding, 0, mPadding, 0);
            setLayoutParams(params);
        }
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            mWidth = Math.round(getWidth() - (mPadding + mInset) * 2);
            updateSelectorPos();

            LayoutParams bgParams = new LayoutParams(
                    Math.round(getWidth() - mPadding * 2),
                    Math.round(BACKGROUND_HEIGHT_DP * mDensity)
            );
            int verticalMargin = Math.round(getHeight() / 2 - bgParams.height / 2);
            bgParams.setMargins(0, verticalMargin, 0, verticalMargin);
            mBackground.setLayoutParams(bgParams);
        }
    }

    public float getHue() {
        return mHue;
    }

    public void setHue(float hue) {
        if (mHue != hue) {
            mHue = Math.max(MIN_HUE, Math.min(MAX_HUE, hue));
            updateSelectorColor();
            updateSelectorPos();
            // notifyHueChanged is the hook for both OnHueChangedListener and 2-way binding
            notifyHueChanged();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - mPadding;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                setHue(getHueForX(x - mInset));
                break;
        }
        return true;
    }

    private float getXForHue(float hue) {
        return mWidth * hue / MAX_HUE;
    }

    private float getHueForX(float x) {
        return x / mWidth * MAX_HUE;
    }

    private void updateSelectorColor() {
        mHueColor.setHue(mHue);
        mSelectorDrawable.setColor(mHueColor.getColor());
    }

    private void updateSelectorPos() {
        updateSelectorX();
        float y = getHeight() / 2 - mSelectorRadius;
        mSelector.setTranslationY(y);
    }

    private void updateSelectorX() {
        float x = getXForHue(mHue) + mInset - mSelectorRadius;
        mSelector.setTranslationX(x);
    }

    public void addHueChangeListener(OnHueChangeListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeHueChangeListener(OnHueChangeListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    private void notifyHueChanged() {
        for (OnHueChangeListener listener : mListeners) {
            listener.onHueChange(this, mHue);
        }
    }
}
