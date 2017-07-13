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
 * A data object representing an oval shape. Has corresponding inShapeBounds hit-testing method
 */
public class TriangleLayer extends ShapeLayer {

    /**
     * These empty constructors are necessary to facilitate the copy function of ShapeLayer subclass
     */
    public TriangleLayer() {
        super();
    }

    public TriangleLayer(TriangleLayer layer) {
        super(layer);
    }

    public TriangleLayer copy() {
        return new TriangleLayer(this);
    }

    /**
     * Determines if the point given by the x and y parameters falls within the shape represented
     * by this layer's shape type, assuming the shape is stretched across the layer's bounding box.
     *
     * @param x the x-axis value to test
     * @param y the x-axis value to test
     * @return true if the x, y coordinates fall within the layer's shape, false otherwise
     */
    @Override
    public boolean inShapeBounds(float x, float y) {
        boolean inBounds = inBounds(x, y);
        if (inBounds) {
            float w = Math.abs(getWidth() / 2);
            float h = Math.abs(getHeight());
            if (getWidth() > 0) {
                x -= getLeft();
            } else {
                x -= getRight();
            }

            if (getHeight() > 0) {
                y = getBottom() - y;
            } else {
                y = y - getBottom();
            }

            if (x > w) {
                x = Math.abs(getWidth()) - x;
            }
            return y / x <= h / w;
        }
        return inBounds;
    }
}
