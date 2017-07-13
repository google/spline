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

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.example.spline.BR;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data object that represents a layer in a spline drawing. It's layering order in the document
 * is determined by its position in Document's layers list. Layer has a number of convenience
 * properties for control points (points representing the four corners of the layer's bounding box
 * and midpoints of the box's edges) - values that are generated on request and which are not
 * persisted in the Parcel.
 */
public class Layer extends BaseObservable implements Parcelable {

    public static final int UNDEFINED = Integer.MIN_VALUE;

    private UUID id;
    private String name;
    private transient Observable parent;
    private boolean selected;
    private boolean visible;
    private int opacity;
    private float x, y, width, height;
    private transient float startX, startY, startWidth, startHeight;
    private transient PointF topLeft, midTop, topRight, midLeft, midRight, bottomLeft, midBottom, bottomRight;
    private transient List<PointF> transformVertices;

    public Layer() {
        init();
    }

    public Layer(Parcel in) {
        id = UUID.fromString(in.readString());
        name = in.readString();
        selected = in.readByte() != 0;
        visible = in.readByte() != 0;
        opacity = in.readInt();
        x = in.readFloat();
        y = in.readFloat();
        width = in.readFloat();
        height = in.readFloat();
        initTransformVertices();
    }

    public Layer(Layer l) {
        id = UUID.randomUUID();
        name = new String(l.getName());
        selected = l.isSelected();
        visible = l.isVisible();
        opacity = l.getOpacity();
        x = l.getX();
        y = l.getY();
        width = l.getWidth();
        height = l.getHeight();
        initTransformVertices();
    }

    public Layer copy() {
        return new Layer(this);
    }

    private void init() {
        id = UUID.randomUUID();
        x = UNDEFINED;
        y = UNDEFINED;
        setVisible(true);
        setOpacity(100);
        initTransformVertices();
    }

    private void initTransformVertices() {
        transformVertices = new ArrayList<PointF>();
        topLeft = new PointF();
        transformVertices.add(topLeft);
        midTop = new PointF();
        transformVertices.add(midTop);
        topRight = new PointF();
        transformVertices.add(topRight);
        midLeft = new PointF();
        transformVertices.add(midLeft);
        midRight = new PointF();
        transformVertices.add(midRight);
        bottomLeft = new PointF();
        transformVertices.add(bottomLeft);
        midBottom = new PointF();
        transformVertices.add(midBottom);
        bottomRight = new PointF();
        transformVertices.add(bottomRight);
    }

    public UUID getId() {
        return id;
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public Observable getParent() {
        return parent;
    }

    public void setParent(Observable parent) {
        this.parent = parent;
        if (this.parent != null) {
            this.parent.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {

                @Override
                public void onPropertyChanged(Observable observable, int i) {
                    if (i == BR.visible || i == BR.ancestorsVisible) {
                        notifyPropertyChanged(BR.ancestorsVisible);
                    }
                }
            });
        }
        notifyPropertyChanged(BR.parent);
        notifyPropertyChanged(BR.parentDepth);
    }

    /**
     * Returns the number of LayerGroups between this layer and the root Document object
     */
    @Bindable
    public int getParentDepth() {

        if (this.parent instanceof Layer) {
            Layer parentLayer = (Layer) this.parent;
            return parentLayer.getParentDepth() + 1;
        }

        return 0;
    }

    public boolean isVisibleInLayerList() {
        if (getParent() == null || !(getParent() instanceof Layer)) {
            return true;
        }
        LayerGroup parentLayer = (LayerGroup) getParent();
        return parentLayer.isTwirledDown() && parentLayer.isVisibleInLayerList();
    }

    @Bindable
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        notifyPropertyChanged(BR.selected);
    }

    @Bindable
    public boolean isVisible() {
        return this.visible;
    }

    @Bindable
    public boolean isAncestorsVisible() {
        if (parent == null || !(parent instanceof Layer)) {
            return true;
        }
        Layer parentLayer = (Layer) parent;
        return parentLayer.isVisible() && parentLayer.isAncestorsVisible();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        notifyPropertyChanged(BR.visible);
    }

    @Bindable
    public int getOpacity() {
        return this.opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
        notifyPropertyChanged(BR.opacity);
    }

    @Bindable
    public float getCompOpacity() {
        float o = this.opacity;
        if (this.parent instanceof Layer) {
            Layer parentLayer = (Layer) parent;
            o *= parentLayer.getCompOpacity() / 100f;
        }

        return o;
    }

    public void startDrag() {
        this.startX = getX();
        this.startY = getY();
    }

    public void startResize() {
        recordStartPosition();
    }

    public void endResize() {
        recordStartPosition();
    }

    private void recordStartPosition() {
        this.startX = getX();
        this.startY = getY();
        this.startWidth = getWidth();
        this.startHeight = getHeight();
    }

    @Bindable
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
        notifyPropertyChanged(BR.x);
        notifyPropertyChanged(BR.left);
        notifyPropertyChanged(BR.right);
    }

    @Bindable
    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        notifyPropertyChanged(BR.y);
        notifyPropertyChanged(BR.top);
        notifyPropertyChanged(BR.bottom);
    }

    @Bindable
    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
        notifyPropertyChanged(BR.width);
        notifyPropertyChanged(BR.right);
    }

    @Bindable
    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
        notifyPropertyChanged(BR.height);
        notifyPropertyChanged(BR.bottom);
    }

    @Bindable
    public float getLeft() {
        return getX();
    }

    @Bindable
    public float getRight() {
        return getX() + getWidth();
    }

    @Bindable
    public float getTop() {
        return getY();
    }

    @Bindable
    public float getBottom() {
        return getY() + getHeight();
    }

    public float getMidX() {
        return getX() + getWidth() / 2;
    }

    public float getMidY() {
        return getY() + getHeight() / 2;
    }

    /**
     * Convenience methods for the control points of the layer's bounding box
     */

    public PointF getTopLeft() {
        topLeft.x = getLeft();
        topLeft.y = getTop();
        return topLeft;
    }

    public PointF getMidTop() {
        midTop.x = getMidX();
        midTop.y = getTop();
        return midTop;
    }

    public PointF getTopRight() {
        topRight.x = getRight();
        topRight.y = getTop();
        return topRight;
    }

    public PointF getMidLeft() {
        midLeft.x = getLeft();
        midLeft.y = getMidY();
        return midLeft;
    }

    public PointF getMidRight() {
        midRight.x = getRight();
        midRight.y = getMidY();
        return midRight;
    }

    public PointF getBottomLeft() {
        bottomLeft.x = getLeft();
        bottomLeft.y = getBottom();
        return bottomLeft;
    }

    public PointF getMidBottom() {
        midBottom.x = getMidX();
        midBottom.y = getBottom();
        return midBottom;
    }

    public PointF getBottomRight() {
        bottomRight.x = getRight();
        bottomRight.y = getBottom();
        return bottomRight;
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }

    public float getStartWidth() {
        return startWidth;
    }

    public float getStartHeight() {
        return startHeight;
    }

    /**
     * Updates the values of each of the control points by invoking their getters and returns the
     * member list that contains a reference to each of them.
     *
     * @return
     */
    public List<PointF> getTransformVertices() {
        // Refresh points
        getTopLeft();
        getMidTop();
        getTopRight();
        getMidLeft();
        getMidRight();
        getBottomLeft();
        getMidBottom();
        getBottomRight();
        return transformVertices;
    }

    public void resize(PointF p, float dx, float dy) {
        if (p == getTopLeft() || p == getMidLeft() || p == getBottomLeft()) {
            setX(getX() + dx);
            setWidth(getWidth() - dx);
        }

        if (p == getTopLeft() || p == getMidTop() || p == getTopRight()) {
            setY(getY() + dy);
            setHeight(getHeight() - dy);
        }

        if (p == getTopRight() || p == getMidRight() || p == getBottomRight()) {
            setWidth(getWidth() + dx);
        }

        if (p == getBottomLeft() || p == getMidBottom()
                || p == getBottomRight()) {
            setHeight(getHeight() + dy);
        }
    }


    /**
     * Determines if the point given by the x and y parameters falls within the layer's
     * bounding box.
     *
     * @param x the x-axis value to test
     * @param y the x-axis value to test
     * @return true if the x, y coordinates fall within the layer's bounding box, false otherwise
     */
    public boolean inBounds(float x, float y) {
        return inInsetBounds(x, y, 0);
    }

    public boolean inInsetBounds(float x, float y, float inset) {
        float minX = Math.min(getLeft(), getRight());
        float maxX = Math.max(getLeft(), getRight());
        float minY = Math.min(getTop(), getBottom());
        float maxY = Math.max(getTop(), getBottom());

        return x >= minX + inset && x <= maxX - inset && y >= minY + inset && y <= maxY - inset;
    }

    public Layer findLayerById(UUID id) {
        if (getId().equals(id)) {
            return this;
        } else {
            if (this instanceof LayerGroup) {
                LayerGroup group = (LayerGroup) this;
                for (Layer l : group.getLayers()) {
                    Layer res = l.findLayerById(id);
                    if (res != null) {
                        return res;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id.toString());
        dest.writeString(name);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeByte((byte) (visible ? 1 : 0));
        dest.writeInt(opacity);
        dest.writeFloat(x);
        dest.writeFloat(y);
        dest.writeFloat(width);
        dest.writeFloat(height);
    }

    public static final Creator<Layer> CREATOR = new Creator<Layer>() {
        @Override
        public Layer createFromParcel(Parcel in) {
            return new Layer(in);
        }

        @Override
        public Layer[] newArray(int size) {
            return new Layer[size];
        }
    };
}
