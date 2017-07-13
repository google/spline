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
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Draws a full spectrum of pure hues as a series of horizontally-oriented gradients
 */
public class HueDrawable extends Drawable {

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();

        Paint huePaint = new Paint();
        huePaint.setShader(new LinearGradient(0, 0, b.width(), 0,
                new int[]{
                        0xFFFF0000,
                        0xFFFFFF00,
                        0xFF00FF00,
                        0xFF00FFFF,
                        0xFF0000FF,
                        0xFFFF00FF,
                        0xFFFF0000
                },
                null,
                Shader.TileMode.REPEAT)
        );
        canvas.drawRect(b, huePaint);
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
