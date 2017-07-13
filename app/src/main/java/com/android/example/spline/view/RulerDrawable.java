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
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import com.android.example.spline.R;

/**
 * Draws a pixel-based ruler with markers every 50px and labels every 200px, either horizontally or
 * vertically depending on whether width or height is the larger dimension.
 */
public class RulerDrawable extends Drawable {

    private static final int FONT_SIZE_DP = 11;

    private final float mDensity;

    private Paint mTextPaint;
    private Paint mLinePaint;
    private Paint mHighlightPaint;
    private Path mVerticalTextPath;

    private float mViewportStart;
    private float mHighlightStart;
    private float mHighlightSize;

    public RulerDrawable(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mDensity = metrics.density;

        mTextPaint = new Paint();
        mTextPaint.setColor(context.getColor(R.color.rulerText));
        mTextPaint.setTextSize(FONT_SIZE_DP * mDensity);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);

        mLinePaint = new Paint();
        mLinePaint.setColor(context.getColor(R.color.divider));
        mLinePaint.setStrokeWidth(mDensity);

        mHighlightPaint = new Paint();
        mHighlightPaint.setColor(context.getColor(R.color.rulerHighlight));

        mVerticalTextPath = new Path();
    }

    public void setViewportStart(float viewportStart) {
        mViewportStart = viewportStart;
    }

    public void setHighlightStart(float highlightStart) {
        mHighlightStart = highlightStart;
    }

    public void setHighlightSize(float highlightSize) {
        mHighlightSize = highlightSize;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        boolean isVertical = bounds.height() > bounds.width();

        int borderX1, borderX2, borderY1, borderY2;

        int segmentSpacing = 50;
        int numSegments;

        boolean validHighlight = mHighlightStart < Float.MAX_VALUE
                && mHighlightSize > -Float.MAX_VALUE;

        if (isVertical) {
            borderX1 = borderX2 = (int) (bounds.width() - mDensity / 2);
            borderY1 = 0;
            borderY2 = bounds.height();
            numSegments = bounds.height() / segmentSpacing;

            if (validHighlight) {
                canvas.drawRect(
                        0,
                        mHighlightStart + mViewportStart,
                        bounds.width(),
                        mHighlightStart + mHighlightSize + mViewportStart,
                        mHighlightPaint
                );
            }
        } else {
            borderX1 = 0;
            borderX2 = bounds.width();
            borderY1 = borderY2 = (int) (bounds.height() - mDensity / 2);
            numSegments = bounds.width() / segmentSpacing;

            if (validHighlight) {
                canvas.drawRect(
                        mHighlightStart + mViewportStart,
                        0,
                        mHighlightStart + mHighlightSize + mViewportStart,
                        bounds.height(),
                        mHighlightPaint
                );
            }
        }

        int offset = Math.round(mViewportStart % segmentSpacing);
        int segmentOffset = (int) (mViewportStart / segmentSpacing);

        for (int i = -1; i < numSegments + 2; i++) {
            int h = 4;

            int idx = i - segmentOffset;
            if (idx % 2 == 0) {
                h = 9;
            }

            if (idx % 4 == 0) {
                if (isVertical) {
                    mVerticalTextPath.reset();
                    mVerticalTextPath.moveTo(
                            bounds.width() / 2,
                            i * segmentSpacing + offset + 25 * mDensity);
                    mVerticalTextPath.lineTo(
                            bounds.width() / 2,
                            (i - 1) * segmentSpacing + offset);

                    canvas.drawTextOnPath(Integer.toString(idx * segmentSpacing),
                            mVerticalTextPath,
                            0,
                            0,
                            mTextPaint);

                } else {
                    canvas.drawText(Integer.toString(idx * segmentSpacing),
                            i * segmentSpacing + offset,
                            bounds.height() / 2,
                            mTextPaint);
                }
            }

            if (isVertical) {
                canvas.drawLine(
                        bounds.width() - (h + 4) * mDensity,
                        i * segmentSpacing + offset,
                        bounds.width() - 4 * mDensity,
                        i * segmentSpacing + offset,
                        mLinePaint);
            } else {
                canvas.drawLine(
                        i * segmentSpacing + offset,
                        bounds.height() - (h + 4) * mDensity,
                        i * segmentSpacing + offset,
                        bounds.height() - 4 * mDensity,
                        mLinePaint);
            }
        }

        canvas.drawLine(borderX1, borderY1, borderX2, borderY2, mLinePaint);
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
