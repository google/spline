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
import android.databinding.Observable;
import android.databinding.ObservableList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import com.android.example.spline.R;
import com.android.example.spline.model.Layer;
import com.android.example.spline.model.LayerGroup;
import com.android.example.spline.model.OvalLayer;
import com.android.example.spline.model.RectLayer;
import com.android.example.spline.model.SelectionGroup;
import com.android.example.spline.model.ShapeLayer;
import com.android.example.spline.model.TriangleLayer;
import com.android.example.spline.util.LayerUtils;

import java.util.List;

/**
 * The heart of the spline editor, DocumentView renders shape layers on a canvas and allows for
 * selection and touch manipulation (dragging and resizing) of the layers.
 */
public class DocumentView extends View {

    // The radius within which a touch is considered to have touched a control point
    private static final int TOUCH_RADIUS_DP = 24;
    private static final float EDIT_CTRL_STROKE_DP = 2;
    private static final int EDIT_VERTEX_WIDTH_DP = 24;

    private static final int MODE_DEFAULT = 0;
    private static final int MODE_LAYER_SELECTION = 1;
    private static final int MODE_LAYER_PRE_DRAG = 2;
    private static final int MODE_LAYER_DRAG = 3;
    private static final int MODE_LAYER_TRANSFORM_DRAG = 4;
    private static final int MODE_VIEWPORT_DRAG = 5;

    private VelocityTracker mVelocityTracker = null;
    private OverScroller mScroller;
    private float mDensity;
    private float mTouchSlop;
    private int mLongPressTimeout;
    private float mTouchRadius;
    private float mEditCtrlStrokeWidth;
    private int mEditColor;

    private int mMode;
    private PointF mCurrentPoint;
    private float mViewportX;
    private float mViewportY;
    private int mViewportWidth;
    private int mViewportHeight;
    private float mTouchDownX;
    private float mTouchDownY;
    private float mPrevX;
    private float mPrevY;
    private float mPrevRawX;
    private float mPrevRawY;
    private boolean mTouchDownInCurrentLayerBounds;

    private LayerGroup mRoot;
    private Layer mCurrentLayer;
    private Layer mLayerDown;
    private ObservableList.OnListChangedCallback<ObservableList<Layer>> mOnListChangedCallback;
    private Observable.OnPropertyChangedCallback mOnPropertyChangedCallback;
    private InverseBindingListener mCurrentLayerAttrChangedListener;
    private InverseBindingListener mViewportXAttrChangedListener;
    private InverseBindingListener mViewportYAttrChangedListener;
    private InverseBindingListener mViewportWidthAttrChangedListener;
    private InverseBindingListener mViewportHeightAttrChangedListener;

    public DocumentView(Context context) {
        super(context);
        init(context);
    }

    public DocumentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mDensity = metrics.density;

        ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
        mLongPressTimeout = vc.getLongPressTimeout();

        mScroller = new OverScroller(context);

        mTouchRadius = TOUCH_RADIUS_DP * mDensity;
        mEditCtrlStrokeWidth = EDIT_CTRL_STROKE_DP * mDensity;
        mEditColor = getResources().getColor(R.color.colorAccent, context.getTheme());

        mOnListChangedCallback = new ObservableList
                .OnListChangedCallback<ObservableList<Layer>>() {
            @Override
            public void onChanged(ObservableList<Layer> layers) {
                invalidate();
            }

            @Override
            public void onItemRangeChanged(ObservableList<Layer> layers, int i, int i1) {
                invalidate();
            }

            @Override
            public void onItemRangeInserted(ObservableList<Layer> layers, int start, int count) {
                invalidate();
                addPropertyChangedCallbacks(layers, start, start + count);
            }

            @Override
            public void onItemRangeMoved(ObservableList<Layer> layers, int i, int i1, int i2) {
                invalidate();
            }

            @Override
            public void onItemRangeRemoved(ObservableList<Layer> layers, int i, int i1) {
                invalidate();
            }
        };

        mOnPropertyChangedCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                invalidate();
            }
        };
    }

    private void addPropertyChangedCallbacks(List<Layer> layers) {
        addPropertyChangedCallbacks(layers, 0, layers.size());
    }

    private void addPropertyChangedCallbacks(List<Layer> layers, int start, int end) {
        for (int i = start; i < end; i++) {
            Layer l = layers.get(i);
            l.addOnPropertyChangedCallback(mOnPropertyChangedCallback);
            if (l instanceof LayerGroup) {
                LayerGroup lg = (LayerGroup) l;
                ObservableList<Layer> childLayers = lg.getLayers();
                // Add list listener for future changes to the layer group's list of children
                childLayers.addOnListChangedCallback(mOnListChangedCallback);

                // Recursive call to add property listeners to each child layer
                addPropertyChangedCallbacks(childLayers);
            }
        }
    }

    public void setRoot(LayerGroup root) {
        mRoot = root;
        if (root != null) {
            mRoot.getLayers().addOnListChangedCallback(mOnListChangedCallback);
            addPropertyChangedCallbacks(mRoot.getLayers());
        }
    }

    public Layer getCurrentLayer() {
        return mCurrentLayer;
    }

    public void setCurrentLayer(Layer layer) {
        if (mCurrentLayer != layer) {
            mCurrentLayer = layer;

            if (mCurrentLayerAttrChangedListener != null) {
                mCurrentLayerAttrChangedListener.onChange();
            }
            invalidate();
        }
    }

    public void setCurrentLayerAttrChanged(InverseBindingListener listener) {
        mCurrentLayerAttrChangedListener = listener;
    }

    public float getViewportX() {
        return mViewportX;
    }

    public void setViewportX(float viewportX) {
        if (viewportX != mViewportX) {
            mViewportX = viewportX;
            if (mViewportXAttrChangedListener != null) {
                mViewportXAttrChangedListener.onChange();
            }
            invalidate();
        }
    }

    public void setViewportXAttrChanged(InverseBindingListener listener) {
        mViewportXAttrChangedListener = listener;
    }

    public float getViewportY() {
        return mViewportY;
    }

    public void setViewportY(float viewportY) {
        if (viewportY != mViewportY) {
            mViewportY = viewportY;
            if (mViewportYAttrChangedListener != null) {
                mViewportYAttrChangedListener.onChange();
            }
            invalidate();
        }
    }

    public void setViewportYAttrChanged(InverseBindingListener listener) {
        mViewportYAttrChangedListener = listener;
    }

    public float getViewportWidth() {
        return mViewportWidth;
    }

    public void setViewportWidth(int viewportWidth) {
        if (viewportWidth != mViewportWidth) {
            mViewportWidth = viewportWidth;
            if (mViewportWidthAttrChangedListener != null) {
                mViewportWidthAttrChangedListener.onChange();
            }
        }
    }

    public void setViewportWidthAttrChanged(InverseBindingListener listener) {
        mViewportWidthAttrChangedListener = listener;
    }

    public float getViewportHeight() {
        return mViewportHeight;
    }

    public void setViewportHeight(int viewportHeight) {
        if (viewportHeight != mViewportHeight) {
            mViewportHeight = viewportHeight;
            if (mViewportHeightAttrChangedListener != null) {
                mViewportHeightAttrChangedListener.onChange();
            }
        }
    }

    public void setViewportHeightAttrChanged(InverseBindingListener listener) {
        mViewportHeightAttrChangedListener = listener;
    }

    @Override
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        setViewportWidth(width);
        setViewportHeight(height);
        super.onSizeChanged(width, height, oldWidth, oldHeight);
    }

    /**
     * View contents are entirely custom drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        float vx = getViewportX();
        float vy = getViewportY();

        if (mRoot != null) {
            drawLayers(canvas, vx, vy, mRoot.getLayers());
        }

        // Drag current layer bounding box and control points afterwards to draw on top
        if (mCurrentLayer != null) {
            Layer l = mCurrentLayer;
            Paint strokePaint = new Paint();
            strokePaint.setColor(mEditColor);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(mEditCtrlStrokeWidth);

            // Draw bounding box
            canvas.drawRect(
                    l.getLeft() + vx,
                    l.getTop() + vy,
                    l.getRight() + vx,
                    l.getBottom() + vy,
                    strokePaint
            );

            Paint pointPaint = new Paint();
            pointPaint.setColor(mEditColor);
            pointPaint.setStrokeWidth(mEditCtrlStrokeWidth);
            pointPaint.setAntiAlias(true);

            // Draw control points
            drawRoundRect(canvas, l.getLeft() + vx, l.getTop() + vy, pointPaint);
            drawRoundRect(canvas, l.getMidX() + vx, l.getTop() + vy, pointPaint);
            drawRoundRect(canvas, l.getRight() + vx, l.getTop() + vy, pointPaint);
            drawRoundRect(canvas, l.getLeft() + vx, l.getMidY() + vy, pointPaint);
            drawRoundRect(canvas, l.getRight() + vx, l.getMidY() + vy, pointPaint);
            drawRoundRect(canvas, l.getLeft() + vx, l.getBottom() + vy, pointPaint);
            drawRoundRect(canvas, l.getMidX() + vx, l.getBottom() + vy, pointPaint);
            drawRoundRect(canvas, l.getRight() + vx, l.getBottom() + vy, pointPaint);
        }
    }

    private void drawLayers(Canvas canvas, float vx, float vy, List<Layer> layers) {
        if (layers != null) {
            Paint p = new Paint();
            for (Layer layer : layers) {
                if (layer.isVisible()) {
                    if (layer instanceof LayerGroup) {
                        LayerGroup group = (LayerGroup) layer;
                        drawLayers(canvas, vx, vy, group.getLayers());
                    } else if (layer instanceof ShapeLayer) {
                        ShapeLayer shapeLayer = (ShapeLayer) layer;
                        p.setColor(shapeLayer.getColorInt());
                        p.setAlpha(Math.round(layer.getCompOpacity() / 100f * 255));

                        // Use different canvas draw method depending on shape
                        if (shapeLayer instanceof RectLayer) {
                            p.setAntiAlias(false);
                            canvas.drawRect(
                                    layer.getLeft() + vx,
                                    layer.getTop() + vy,
                                    layer.getRight() + vx,
                                    layer.getBottom() + vy,
                                    p
                            );
                        } else if (shapeLayer instanceof TriangleLayer) {
                            p.setAntiAlias(true);
                            Path path = new Path();
                            path.moveTo(layer.getLeft() + vx, layer.getBottom() + vy);
                            path.lineTo(layer.getRight() + vx, layer.getBottom() + vy);
                            path.lineTo(layer.getMidX() + vx, layer.getTop() + vy);
                            canvas.drawPath(path, p);
                        } else if (shapeLayer instanceof OvalLayer) {
                            p.setAntiAlias(true);
                            canvas.drawOval(
                                    layer.getLeft() + vx,
                                    layer.getTop() + vy,
                                    layer.getRight() + vx,
                                    layer.getBottom() + vy,
                                    p
                            );
                        }
                    }
                }
            }
        }
    }

    public void drawRoundRect(Canvas canvas, float x, float y, Paint pointPaint) {
        float w = EDIT_VERTEX_WIDTH_DP / 2;
        canvas.drawRoundRect(x - w, y - w, x + w, y + w, w / 4, w / 4, pointPaint);
    }

    /**
     * @return true if the current touch should trigger a context menu - i.e., was this a touch on
     * the currently selected layer
     */
    public boolean shouldShowContextMenu() {
        return mMode < MODE_LAYER_DRAG;
    }

    public boolean shouldShowCurrentLayerContextItems() {
        return mCurrentLayer != null && mTouchDownInCurrentLayerBounds;
    }

    /**
     * Adds support for different mouse pointer icons depending on document state and mouse position
     */
    @Override
    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        int icon = PointerIcon.TYPE_DEFAULT;
        Layer l = mCurrentLayer;
        float x = event.getX() - getViewportX();
        float y = event.getY() - getViewportY();

        if (mMode == MODE_LAYER_DRAG || mMode == MODE_LAYER_PRE_DRAG) {
            icon = PointerIcon.TYPE_GRABBING;
        } else {
            if (l != null) {
                if (inPointTouchRadius(x, y, l.getTopLeft())
                        || inPointTouchRadius(x, y, l.getBottomRight())) {
                    icon = PointerIcon.TYPE_TOP_LEFT_DIAGONAL_DOUBLE_ARROW;
                } else if (inPointTouchRadius(x, y, l.getTopRight())
                        || inPointTouchRadius(x, y, l.getBottomLeft())) {
                    icon = PointerIcon.TYPE_TOP_RIGHT_DIAGONAL_DOUBLE_ARROW;
                } else if (inPointTouchRadius(x, y, l.getMidTop())
                        || inPointTouchRadius(x, y, l.getMidBottom())) {
                    icon = PointerIcon.TYPE_VERTICAL_DOUBLE_ARROW;
                } else if (inPointTouchRadius(x, y, l.getMidLeft())
                        || inPointTouchRadius(x, y, l.getMidRight())) {
                    icon = PointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW;
                } else if (l.inBounds(x, y)) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_MOVE:
                            // Only change to hand if this is a primary button click
                            if (event.getActionButton() == MotionEvent.BUTTON_PRIMARY) {
                                icon = PointerIcon.TYPE_GRABBING;
                            } else {
                                icon = PointerIcon.TYPE_DEFAULT;
                            }
                            break;
                        case MotionEvent.ACTION_HOVER_MOVE:
                            icon = PointerIcon.TYPE_GRAB;
                            break;
                        case MotionEvent.ACTION_UP:
                        default:
                            if (event.getActionButton() == MotionEvent.BUTTON_PRIMARY) {
                                icon = PointerIcon.TYPE_GRAB;
                            } else {
                                icon = PointerIcon.TYPE_DEFAULT;
                            }
                    }
                }
            }
        }
        return PointerIcon.getSystemIcon(getContext(), icon);
    }

    private boolean inPointTouchRadius(float x, float y, PointF p) {
        float dx = x - p.x;
        float dy = y - p.y;
        return Math.sqrt(dx * dx + dy * dy) < mTouchRadius;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - getViewportX();
        float y = event.getY() - getViewportY();
        float dx;
        float dy;
        int index = event.getActionIndex();
        int pointerId = event.getPointerId(index);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(event);

                mMode = MODE_LAYER_SELECTION;
                mLayerDown = null;
                mCurrentPoint = null;
                mTouchDownInCurrentLayerBounds = false;

                // Disable interaction after long press
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if (mMode < MODE_LAYER_DRAG) {
                            mMode = MODE_DEFAULT;
                        }
                    }
                }, mLongPressTimeout);

                mTouchDownX = x;
                mTouchDownY = y;
                mPrevRawX = event.getX();
                mPrevRawY = event.getY();

                if (mCurrentLayer != null) {

                    if (mCurrentLayer instanceof ShapeLayer) {
                        ShapeLayer s = (ShapeLayer) mCurrentLayer;
                        mTouchDownInCurrentLayerBounds = s.inShapeBounds(x, y);
                    } else {
                        mTouchDownInCurrentLayerBounds = mCurrentLayer.inBounds(x, y);
                    }

                    // Skip vertex check if we're inside the hit area of all vertices
                    if (mCurrentLayer.inInsetBounds(x, y, mTouchRadius)) {
                        mMode = MODE_LAYER_PRE_DRAG;
                    } else {
                        PointF closestPoint = null;
                        double closestDist = -1;

                        // Find the closest control point of the layer's bounding box
                        List<PointF> transformVertices = mCurrentLayer.getTransformVertices();
                        for (PointF p : transformVertices) {
                            dx = x - p.x;
                            dy = y - p.y;
                            float dist = (float) Math.sqrt(dx * dx + dy * dy);
                            if (closestPoint == null || dist < closestDist) {
                                closestPoint = p;
                                closestDist = dist;
                            }
                        }
                        // If that closest point falls within the touch radius, change the mode to
                        // transform drag
                        if (closestPoint != null && closestDist < mTouchRadius) {
                            boolean vertexChanged = false;
                            if (closestPoint != mCurrentPoint) {
                                vertexChanged = true;
                            }

                            mCurrentPoint = closestPoint;
                            mMode = MODE_LAYER_TRANSFORM_DRAG;
                            mCurrentLayer.startResize();

                            if (vertexChanged) {
                                invalidate();
                            }
                        } else if (mCurrentLayer.inBounds(x, y)) {
                            // Otherwise, if we're within the layer's bounds move to pre drag
                            mMode = MODE_LAYER_PRE_DRAG;
                        }
                    }
                }

                // If none of the preceding checks changed the mode, look for the top layer under
                // the touch down coordinates (if one exists)
                if (mMode == MODE_LAYER_SELECTION && mRoot != null) {
                    mLayerDown = getTopLayerHit(x, y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);

                if (mMode == MODE_LAYER_SELECTION || mMode == MODE_LAYER_PRE_DRAG) {
                    dx = x - mTouchDownX;
                    dy = y - mTouchDownY;
                    float delta = (float) Math.sqrt(dx * dx + dy * dy);

                    // Move to a drag mode if we've exceeded the system touch slop
                    if (delta > mTouchSlop) {
                        // If our touch doesn't fall within the currently selected layer,
                        // consider this a drag of the viewport
                        if (mMode == MODE_LAYER_SELECTION) {
                            mMode = MODE_VIEWPORT_DRAG;
                        } else {
                            mMode = MODE_LAYER_DRAG;
                            mCurrentLayer.startDrag();
                        }
                    }
                }

                if (mMode == MODE_VIEWPORT_DRAG) {
                    setViewportX(getViewportX() + event.getX() - mPrevRawX);
                    setViewportY(getViewportY() + event.getY() - mPrevRawY);
                    invalidate();
                }

                if (mMode == MODE_LAYER_DRAG) {
                    // For now, simply round deltas to the nearest pixel. Effectively makes the
                    // atomic drag unit the pixel.
                    dx = Math.round(x - mTouchDownX);
                    dy = Math.round(y - mTouchDownY);
                    int metaState = event.getMetaState();
                    boolean isShiftPressed = (metaState & KeyEvent.META_SHIFT_ON) != 0;

                    if (isShiftPressed) {
                        if (Math.abs(dx) > Math.abs(dy)) {
                            mCurrentLayer.setX(mCurrentLayer.getStartX() + dx);
                            mCurrentLayer.setY(mCurrentLayer.getStartY());
                        } else {
                            mCurrentLayer.setX(mCurrentLayer.getStartX());
                            mCurrentLayer.setY(mCurrentLayer.getStartY() + dy);
                        }
                    } else {
                        mCurrentLayer.setX(mCurrentLayer.getStartX() + dx);
                        mCurrentLayer.setY(mCurrentLayer.getStartY() + dy);
                    }

                    invalidate();
                }

                if (mMode == MODE_LAYER_TRANSFORM_DRAG) {
                    // For now, simply round deltas to the nearest pixel. Effectively makes the
                    // atomic drag unit the pixel.
                    dx = Math.round(x - mPrevX);
                    dy = Math.round(y - mPrevY);
                    mCurrentLayer.resize(mCurrentPoint, dx, dy);

                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:

                if (mMode == MODE_LAYER_TRANSFORM_DRAG) {
                    mCurrentLayer.endResize();
                }

                if (mMode == MODE_VIEWPORT_DRAG) {
                    mVelocityTracker.computeCurrentVelocity(1000);
                    int vx = (int) (VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId));
                    int vy = (int) (VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId));
                    mScroller.fling((int) getViewportX(), (int) getViewportY(), vx, vy, -10000, 10000,
                            -10000, 10000);
                    this.invalidate();
                }

                // We didn't end up dragging the current layer, so check if we selected another
                // layer on top of the current layer that may be within its bounds.
                if (mMode == MODE_LAYER_PRE_DRAG) {
                    mLayerDown = getTopLayerHit(mTouchDownX, mTouchDownY);
                    if (mLayerDown != mCurrentLayer) {
                        int downIdx = 0; //mLayers.indexOf(mLayerDown);
                        int currentIdx = 0; //mLayers.indexOf(mCurrentLayer);
                        boolean outsideCurrentShape = true;
                        if (mCurrentLayer != null && mCurrentLayer instanceof ShapeLayer) {
                            outsideCurrentShape = !((ShapeLayer) mCurrentLayer).inShapeBounds(
                                    mTouchDownX, mTouchDownY);
                        }

                        if (downIdx > currentIdx || outsideCurrentShape) {
                            mMode = MODE_LAYER_SELECTION;
                        }
                    }
                }

                // Do the actual layer selection if ending a touch in selection mode (i.e., did not
                // exceed the touch slop
                if (mMode == MODE_LAYER_SELECTION) {
                    dx = x - mTouchDownX;
                    dy = y - mTouchDownY;
                    float delta = (float) Math.sqrt(dx * dx + dy * dy);
                    if (delta < mTouchSlop) {

                        int metaState = event.getMetaState();
                        boolean isShiftPressed = (metaState & KeyEvent.META_SHIFT_ON) != 0;

                        Layer selection = LayerUtils.selectionFrom(
                                mCurrentLayer, mLayerDown, isShiftPressed);
                        setCurrentLayer(selection);
                    }
                }

            case MotionEvent.ACTION_CANCEL:
                mMode = MODE_DEFAULT;
                break;
        }

        mPrevX = x;
        mPrevY = y;
        mPrevRawX = event.getX();
        mPrevRawY = event.getY();

        // Call super at the end so that it can trigger onCreateContextMenu, but only after the
        // subclass has a chance to record state info about the touch down to help the host activity
        // determine if a context menu should be shown for this touch
        super.onTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller != null && mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            setViewportX(x);
            setViewportY(y);

            this.postInvalidate();
        }
    }

    public Layer getTopLayerHit(float x, float y) {
        LayerGroup root;

        // If the current layer is a selection, use the most recently selected layer's parent as
        // the root of a search for the top layer hit, if available.
        if (mCurrentLayer instanceof SelectionGroup) {
            SelectionGroup group = (SelectionGroup) mCurrentLayer;
            List<Layer> layers = group.getLayers();
            if (layers.size() > 0) {
                Layer mostRecent = layers.get(layers.size() - 1);
                if (mostRecent != null && mostRecent.getParent() instanceof LayerGroup) {
                    root = (LayerGroup) mostRecent.getParent();
                } else {
                    root = mRoot;
                }
            } else {
                root = mRoot;
            }
        } else if (mCurrentLayer instanceof LayerGroup) {
            root = (LayerGroup) mCurrentLayer;
        } else if (mCurrentLayer != null && mCurrentLayer.getParent() instanceof LayerGroup) {
            root = (LayerGroup) mCurrentLayer.getParent();
        } else {
            root = null;
        }

        return getTopLayerHit(x, y, root);
    }

    public Layer getTopLayerHit(float x, float y, LayerGroup root) {
        Layer topLayerHit = null;

        List<Layer> layers;
        if (root == null) {
            root = mRoot;
        }
        layers = root.getLayers();

        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer l = layers.get(i);
            // Only select visible layers
            if (l != mCurrentLayer && l.isVisible() && l.inBounds(x, y)) {
                // ShapeLayer is only a hit if the point is within the shape bounds
                if (l instanceof ShapeLayer && !((ShapeLayer) l).inShapeBounds(x, y)) {
                    continue;
                }
                topLayerHit = l;
                break;
            }
        }

        if (topLayerHit == null && root != null
                && root.getParent() instanceof LayerGroup) {
            return getTopLayerHit(x, y, ((LayerGroup) root.getParent()));
        }

        return topLayerHit;
    }
}
