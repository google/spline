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

import android.databinding.Bindable;
import android.databinding.Observable;
import android.os.Parcel;

import com.android.example.spline.BR;


/**
 * ShapeLayer has three subclass shape types - rectangle, triangle and oval. Abstract inShapeBounds
 * is implemented can accurately detect a hit within just this shape (not just the layer's bounding
 * box) for each of these types.
 *
 * ShapeLayer also has a color property absent from other layer types
 */
public abstract class ShapeLayer extends Layer {
    private Color color;

    public ShapeLayer() {
        setColor(new Color(0xffC6DAFC));
    }

    public ShapeLayer(Parcel in) {
        super(in);
    }

    public ShapeLayer(ShapeLayer l) {
        super(l);
        setColor(new Color(l.getColor()));
    }

    @Bindable
    public int getColorInt() {
        return color.getColor();
    }

    public void setColorInt() {
        // Placeholder for now
        notifyPropertyChanged(BR.colorInt);
    }

    @Bindable
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        addOnColorChangeListener();
        notifyPropertyChanged(BR.color);
    }

    /**
     * Exists as public method to allow Json deserializing to add color change listener after
     * deserializing
     */
    public void addOnColorChangeListener() {
        this.color.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                ShapeLayer.this.notifyPropertyChanged(BR.color);
            }
        });
    }

    /**
     * Determines if the point given by the x and y parameters falls within the shape represented
     * by this layer's shape type, assuming the shape is stretched across the layer's bounding box.
     *
     * @param x the x-axis value to test
     * @param y the x-axis value to test
     * @return true if the x, y coordinates fall within the layer's shape, false otherwise
     */
    public abstract boolean inShapeBounds(float x, float y);
}
