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
package com.android.example.spline.viewmodel;

import android.content.Context;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.android.example.spline.LayerListAdapter;
import com.android.example.spline.model.Layer;
import com.android.example.spline.model.LayerGroup;
import com.android.example.spline.model.SelectionGroup;
import com.android.example.spline.util.LayerUtils;

/**
 * A set of callbacks used by layer rows in the LayerListView. A single instance of the callbacks is
 * bound to the rows, allowing the touch handling binding lambdas to perform the required
 * functionality without having to maintain a direct reference to the adapter.
 */
public class LayerRowCallbacks {
    private LayerListAdapter adapter;
    private GestureDetector detector;
    private Layer currentTouchLayer;
    private View currentTouchView;
    private boolean currentTouchIsSecondaryButton;

    class GestureTap extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            if (adapter != null && currentTouchLayer != null) {
                int metaState = event.getMetaState();
                boolean isShiftPressed = (metaState & KeyEvent.META_SHIFT_ON) != 0;

                if (!currentTouchIsSecondaryButton
                        || !(adapter.getCurrentLayer() instanceof SelectionGroup)
                        || ((SelectionGroup) adapter.getCurrentLayer()).getLayers().size() < 2) {
                    Layer selection = LayerUtils.selectionFrom(
                            adapter.getCurrentLayer(), currentTouchLayer, isShiftPressed);
                    adapter.setCurrentLayer(selection);
                }

                if (currentTouchIsSecondaryButton) {
                    currentTouchView.showContextMenu(event.getX(), event.getY());
                }
            }

            return true;
        }
    }

    public LayerRowCallbacks(Context context, LayerListAdapter adapter) {
        this.detector = new GestureDetector(context, new GestureTap());
        this.adapter = adapter;
    }

    public boolean onRowTouch(View view, MotionEvent event, LayerRowViewModel row) {
        if (row != null) {
            currentTouchLayer = row.getLayer();
        }
        currentTouchView = view;
        detector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            currentTouchIsSecondaryButton = event.getButtonState() == MotionEvent.BUTTON_SECONDARY;
        }

        return true;
    }

    public void onTwirl(View v, LayerRowViewModel row) {
        if (adapter != null && row != null && row.getLayer() != null
                && row.getLayer() instanceof LayerGroup) {
            LayerGroup group = (LayerGroup) row.getLayer();
            adapter.toggleTwirlLayerGroup(group);
        }
    }
}
