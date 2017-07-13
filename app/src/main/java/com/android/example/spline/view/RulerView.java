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
import android.support.annotation.Nullable;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.OverScroller;

/**
 * Draws a pixel-based ruler using RulerDrawable. Handles drags and flings along the ruler's
 * dominant direction (horizontal or vertical).
 */
public class RulerView extends View {

    private static final int MODE_RULER_DRAG = 0;
    private static final int MODE_HIGHLIGHT_DRAG = 1;

    private RulerDrawable mDrawable;
    private boolean mIsVertical;

    private float mViewportStart;
    private float mHighlightStart;
    private float mHighlightSize;
    private InverseBindingListener mViewportStartAttrChangedListener;
    private InverseBindingListener mHighlightStartAttrChangedListener;

    private float mPrevX;
    private float mPrevY;
    private int mMode;
    private VelocityTracker mVelocityTracker;
    private OverScroller mScroller;

    public RulerView(Context context) {
        super(context);
        init(context);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        mDrawable = new RulerDrawable(context);
        setBackground(mDrawable);

        mScroller = new OverScroller(context);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mIsVertical = (bottom - top) > (right - left);
    }

    public void setViewportStartAttrChanged(InverseBindingListener listener) {
        mViewportStartAttrChangedListener = listener;
    }

    public void setHighlightStartAttrChanged(InverseBindingListener listener) {
        mHighlightStartAttrChangedListener = listener;
    }

    public float getViewportStart() {
        return mViewportStart;
    }

    public void setViewportStart(float viewportStart) {
        if (viewportStart != mViewportStart) {
            mViewportStart = viewportStart;
            mDrawable.setViewportStart(viewportStart);
            mDrawable.invalidateSelf();
            if (mViewportStartAttrChangedListener != null) {
                mViewportStartAttrChangedListener.onChange();
            }
        }
    }

    public float getHighlightStart() {
        return mHighlightStart;
    }

    public void setHighlightStart(float highlightStart) {
        if (highlightStart != mHighlightStart) {
            mHighlightStart = highlightStart;
            mDrawable.setHighlightStart(highlightStart);
            mDrawable.invalidateSelf();
            if (mHighlightStartAttrChangedListener != null) {
                mHighlightStartAttrChangedListener.onChange();
            }
        }
    }

    public void setHighlightSize(float highlightSize) {
        if (highlightSize != mHighlightSize) {
            mHighlightSize = highlightSize;
            mDrawable.setHighlightSize(highlightSize);
            mDrawable.invalidateSelf();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float dx = x - mPrevX;
        float dy = y - mPrevY;
        int index = event.getActionIndex();
        int pointerId = event.getPointerId(index);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                mMode = MODE_RULER_DRAG;
                if (mIsVertical) {
                    if (y - mViewportStart >= mHighlightStart &&
                            y - mViewportStart <= mHighlightStart + mHighlightSize) {
                        mMode = MODE_HIGHLIGHT_DRAG;
                    }
                } else {
                    if (x - mViewportStart >= mHighlightStart &&
                            x - mViewportStart <= mHighlightStart + mHighlightSize) {
                        mMode = MODE_HIGHLIGHT_DRAG;
                    }
                }

                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(event);

                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);

                if (mMode == MODE_RULER_DRAG) {
                    if (mIsVertical) {
                        setViewportStart(getViewportStart() + dy);
                    } else {
                        setViewportStart(getViewportStart() + dx);
                    }
                } else if (mMode == MODE_HIGHLIGHT_DRAG) {
                    if (mIsVertical) {
                        // Drag highlight by whole pixels for now
                        setHighlightStart(getHighlightStart() + Math.round(dy));
                    } else {
                        setHighlightStart(getHighlightStart() + Math.round(dx));
                    }
                }

                break;

            case MotionEvent.ACTION_UP:
                if (mMode == MODE_RULER_DRAG) {
                    mVelocityTracker.computeCurrentVelocity(1000);
                    int vx = (int) (VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId));
                    int vy = (int) (VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId));
                    mScroller.fling((int) getViewportStart(), (int) getViewportStart(), vx, vy,
                            -10000, 10000, -10000, 10000);
                    this.invalidate();
                }

                break;
        }

        mPrevX = x;
        mPrevY = y;

        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller != null && mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if(mIsVertical) {
                setViewportStart(y);
            } else {
                setViewportStart(x);
            }

            this.postInvalidate();
        }
    }
}