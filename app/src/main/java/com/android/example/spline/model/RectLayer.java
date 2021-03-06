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

/**
 * A data object representing an rect shape. Has corresponding inShapeBounds hit-testing method
 */
public class RectLayer extends ShapeLayer {

    /**
     * These empty constructors are necessary to facilitate the copy function of ShapeLayer subclass
     */
    public RectLayer() {
        super();
    }

    public RectLayer(RectLayer layer) {
        super(layer);
    }

    public RectLayer copy() {
        return new RectLayer(this);
    }

    /**
     * Determines if the point given by the x and y parameters falls within the shape represented
     * by this layer's shape type, assuming the shape is stretched across the layer's bounding box.
     *
     * @param x the x-axis value to test
     * @param y the x-axis value to test
     * @return true if the x, y coordinates fall within the layer's shape, false otherwise
     */
    public boolean inShapeBounds(float x, float y) {
        return inBounds(x, y);
    }
}
