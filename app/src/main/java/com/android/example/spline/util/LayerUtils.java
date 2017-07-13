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
package com.android.example.spline.util;

import com.android.example.spline.model.Layer;
import com.android.example.spline.model.SelectionGroup;

/**
 * Static utility methods for sharing common Layer operations across components.
 */
public class LayerUtils {

    /**
     * Determines the resultant selection that should come from adding or subtracting newLayer from
     * the current selection (represented by currentLayer). If multi is false, simply return
     * newLayer.
     * @param currentLayer
     * @param newLayer
     * @param multi whether we are in multi-selection mode.
     * @return
     */
    public static Layer selectionFrom(Layer currentLayer, Layer newLayer, boolean multi) {
        if (multi && currentLayer != null && newLayer != null) {
            SelectionGroup sg;
            if (currentLayer instanceof SelectionGroup) {
                sg = (SelectionGroup) currentLayer;

                // Toggle adding/removing newLayer from the selection
                if (sg.getLayers().contains(newLayer)) {
                    sg.removeLayer(newLayer);
                } else {
                    sg.addLayer(newLayer);
                }

                if (sg.getLayers().size() == 0) {
                    sg = null;
                }
            } else {
                // If currentLayer is only a single layer, create a new selection group and add both
                // layers to it.
                sg = new SelectionGroup();
                sg.addLayer(currentLayer);
                sg.addLayer(newLayer);
            }
            return sg;
        } else {
            return newLayer;
        }
    }
}
