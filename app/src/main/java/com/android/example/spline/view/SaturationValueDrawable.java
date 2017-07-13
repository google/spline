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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Draws a two-dimentional gradient with saturation increasing with increasing x and value
 * increasing with increasing y. The saturation gradient is applied on top of of the value
 * gradient with PorterDuff multiply mode.
 */
public class SaturationValueDrawable extends Drawable {

    private float mHue;
    private float mInset;

    public SaturationValueDrawable() {
        init(0);
    }

    public SaturationValueDrawable(float inset) {
        init(inset);
    }

    public void init(float inset) {
        mInset = inset;
    }

    public void setHue(float hue) {
        mHue = hue;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        int height = b.height();
        int width = b.width();

        Paint valuePaint = new Paint();
        valuePaint.setShader(
                new LinearGradient(0, mInset, 0, height - mInset,
                        Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP)
        );

        float hsv[] = {mHue, 1.0f, 1.0f};
        int pureHue = Color.HSVToColor(hsv);
        Paint saturationPaint = new Paint();
        saturationPaint.setShader(
                new LinearGradient(mInset, 0, width - mInset, 0,
                        Color.WHITE, pureHue, Shader.TileMode.CLAMP)
        );
        saturationPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        canvas.drawRect(b, valuePaint);
        canvas.drawRect(b, saturationPaint);
    }

    @Override
    public void setAlpha(int i) {
        // Do nothing for now
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // No support for color filters
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
