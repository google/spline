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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Document model class containing state information and contents of a spline document, including
 * an ObservableArrayList of Layers and the currently selected layer. Any property that should
 * be persisted across user sessions should be stored here. Any transient or derived properties
 * should be stored in DocumentViewModel, the corresponding ViewModel layer for this class. This
 * class and it's contents are parcelable so as to be saved to a Bundle during onInstanceSave.
 * <p>
 * Document does not need to extend BaseObservable because DocumentViewModel is responsible for that
 */

public class Document implements Parcelable {
    private LayerGroup root;
    private Layer currentLayer;
    private Layer clipboardLayer;
    private LayerGroup currentGroup;
    private float viewportX;
    private float viewportY;

    public Document() {
        setRoot(new LayerGroup());

        // Offset the initial viewport somewhat so that the 0 points on both axis are visible
        viewportX = 30;
        viewportY = 30;
    }

    protected Document(Parcel in) {
        root = in.readParcelable(LayerGroup.class.getClassLoader());
        currentLayer = in.readParcelable(Layer.class.getClassLoader());
        viewportX = in.readFloat();
        viewportY = in.readFloat();
    }

    public LayerGroup getRoot() {
        return root;
    }

    public void setRoot(LayerGroup root) {
        this.root = root;
        root.openTwirl();
        this.currentGroup = root;
    }

    public void addLayer(Layer layer) {
        if (currentGroup != null) {
            currentGroup.addLayer(layer);
        }
    }

    public void removeLayer(Layer layer) {
        if (layer != null && layer.getParent() != null) {
            if (layer.getParent() instanceof LayerGroup) {
                LayerGroup p = (LayerGroup) layer.getParent();
                p.removeLayer(layer);
            }
        }
    }

    public Layer getCurrentLayer() {
        return currentLayer;
    }

    public void setCurrentLayer(Layer currentLayer) {
        if (this.currentLayer != null) {
            this.currentLayer.setSelected(false);
        }

        LayerGroup group = this.root;
        this.currentLayer = currentLayer;
        if (currentLayer != null) {
            currentLayer.setSelected(true);

            if (currentLayer.getParent() instanceof LayerGroup) {
                group = (LayerGroup) currentLayer.getParent();
            }
        }

        this.currentGroup = group;
    }

    public LayerGroup getCurrentGroup() {
        return currentGroup;
    }

    public float getViewportX() {
        return viewportX;
    }

    public void setViewportX(float viewportX) {
        this.viewportX = viewportX;
    }

    public float getViewportY() {
        return viewportY;
    }

    public void setViewportY(float viewportY) {
        this.viewportY = viewportY;
    }

    public void setClipboardLayer(Layer clipboardLayer) {
        this.clipboardLayer = clipboardLayer;
    }

    public Layer getClipboardLayer() {
        return this.clipboardLayer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(root, 0);
        dest.writeParcelable(currentLayer, 0);
        dest.writeFloat(viewportX);
        dest.writeFloat(viewportY);
    }

    public static final Creator<Document> CREATOR = new Creator<Document>() {
        @Override
        public Document createFromParcel(Parcel in) {
            return new Document(in);
        }

        @Override
        public Document[] newArray(int size) {
            return new Document[size];
        }
    };
}
