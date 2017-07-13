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
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Parcel;

import com.android.example.spline.BR;

/**
 * A data object that represents a layer with children. Has no visible properties itself, simply
 * acts as a container for its constituent layers.
 */
public class LayerGroup extends Layer {
    private ObservableList<Layer> layers;
    private boolean twirledDown;

    public LayerGroup() {
        super();
        init();
    }

    public LayerGroup(Parcel in) {
        super(in);
        init();
    }

    public LayerGroup(LayerGroup group) {
        super(group);
        init();
        for (Layer l : group.getLayers()) {
            Layer copy = l.copy();
            copy.setSelected(false);
            this.addLayer(copy);
        }
    }

    public LayerGroup copy() {
        return new LayerGroup(this);
    }

    private void init() {
        layers = new ObservableArrayList<>();
        setName("Group");
    }

    public void addLayer(Layer l) {
        l.setParent(this);
        layers.add(l);
    }

    public void removeLayer(Layer l) {
        layers.remove(l);
    }

    public ObservableList<Layer> getLayers() {
        return layers;
    }

    @Bindable
    public boolean isTwirledDown() {
        return twirledDown;
    }

    public void toggleTwirl() {
        twirledDown = !twirledDown;
        notifyPropertyChanged(BR.twirledDown);
    }

    public void openTwirl() {
        twirledDown = true;
        notifyPropertyChanged(BR.twirledDown);
    }

    /**
     * LayerGroup x, y, width and height getters are based off min/max bounds of its child views
     */
    @Override
    public float getX() {
        float minX = Float.MAX_VALUE;
        for (Layer l : layers) {
            minX = Math.min(minX, l.getX());
        }
        return minX;
    }

    @Override
    public float getY() {
        float minY = Float.MAX_VALUE;
        for (Layer l : layers) {
            minY = Math.min(minY, l.getY());
        }
        return minY;
    }

    @Override
    public float getWidth() {
        float maxRight = -Float.MAX_VALUE;
        for (Layer l : layers) {
            maxRight = Math.max(maxRight, l.getRight());
        }
        return maxRight - getLeft();
    }

    @Override
    public float getHeight() {
        float maxBottom = -Float.MAX_VALUE;
        for (Layer l : layers) {
            maxBottom = Math.max(maxBottom, l.getBottom());
        }
        return maxBottom - getTop();
    }

    /**
     * LayerGroup x, y, width and height setters make modifications to their child layers
     */
    @Override
    public void setX(float x) {
        if (x != getX()) {
            float dX = x - getX();
            for (Layer l : getLayers()) {
                l.setX(l.getX() + dX);
            }
            super.setX(x);
        }
    }

    @Override
    public void setY(float y) {
        if (y != getY()) {
            float dY = y - getY();
            for (Layer l : getLayers()) {
                l.setY(l.getY() + dY);
            }
            super.setY(y);
        }
    }

    @Override
    public void setWidth(float width) {
        if (width != getWidth() && getStartWidth() != 0) {
            float scale = width / getStartWidth();
            for (Layer l : getLayers()) {
                l.setWidth(l.getStartWidth() * scale);
                l.setX(getX() + (l.getStartX() - getStartX()) * scale);
            }
            super.setWidth(width);
        }
    }

    @Override
    public void setHeight(float height) {
        if (height != getHeight() && getStartHeight() != 0) {
            float scale = height / getStartHeight();
            for (Layer l : getLayers()) {
                l.setHeight(l.getStartHeight() * scale);
                l.setY(getY() + (l.getStartY() - getStartY()) * scale);
            }
            super.setHeight(height);
        }
    }

    @Override
    public void startResize() {
        super.startResize();
        for (Layer l : this.layers) {
            l.startResize();
        }
    }

    @Override
    public void endResize() {
        super.endResize();
        for (Layer l : this.layers) {
            l.endResize();
        }
    }

    @Override
    public boolean inBounds(float x, float y) {
        if (getLayers().size() == 0) {
            return false;
        }
        return super.inBounds(x, y);
    }
}
