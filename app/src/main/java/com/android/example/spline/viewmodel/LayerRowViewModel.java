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

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.view.View;

import com.android.databinding.library.baseAdapters.BR;
import com.android.example.spline.R;
import com.android.example.spline.model.Layer;
import com.android.example.spline.model.LayerGroup;

/**
 * A viewmodel for rows of the LayerListView. Has to listen for and forward property change
 * notifications from the layer object because this viewmodel isn't used universally across the
 * application in place of Layer.
 */
public class LayerRowViewModel extends BaseObservable {

    private Layer layer;
    private OnPropertyChangedCallback changedCallback;

    public LayerRowViewModel() {

    }

    public LayerRowViewModel(Layer layer) {
        setLayer(layer);
    }

    public void setLayer(Layer layer) {
        if (this.layer != null && changedCallback != null) {
            this.layer.removeOnPropertyChangedCallback(changedCallback);
        }
        this.layer = layer;

        // Because other parts of the application are updating layer model objects directly, we
        // need to listen for changes to the model's properties and notify of those property
        // changes on this object (because they share property names we can get away with this).
        changedCallback = new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                notifyPropertyChanged(propertyId);
            }
        };
        layer.addOnPropertyChangedCallback(changedCallback);
    }

    public Layer getLayer() {
        return layer;
    }

    @Bindable
    public String getName() {
        if (layer == null) {
            return "";
        }

        return layer.getName();
    }

    @Bindable
    public boolean isVisible() {
        if (layer == null) {
            return true;
        }

        return layer.isVisible();
    }

    public void setVisible(boolean visible) {
        if (layer != null && visible != layer.isVisible()) {
            layer.setVisible(visible);
            notifyPropertyChanged(BR.visible);
        }
    }

    @Bindable
    public boolean isAncestorsVisible() {
        if (layer == null) {
            return false;
        }

        return layer.isAncestorsVisible();
    }

    @Bindable({"visible", "ancestorsVisible"})
    public float getAlpha() {
        if (layer == null) {
            return 0;
        }

        return isVisible() && isAncestorsVisible() ? 1.0f : 0.33f;
    }

    @Bindable
    public boolean isSelected() {
        if (layer == null) {
            return false;
        }

        return layer.isSelected();
    }

    @Bindable({"selected"})
    public int getBackgroundColor() {
        return isSelected() ? R.color.colorAccent : R.color.sidePanel;
    }

    @Bindable({"selected"})
    public int getTextColor() {
        return isSelected() ? android.R.color.white : R.color.colorPrimary;
    }

    @Bindable
    public int getParentDepth() {
        if (layer == null) {
            return 0;
        }
        return layer.getParentDepth();
    }

    @Bindable({"parentDepth"})
    public float getRowIndent() {
        if (layer == null) {
            return 0;
        }

        return (float) getParentDepth() - 1 + (layer instanceof LayerGroup ? 0 : 1);
    }

    @Bindable
    public boolean isTwirledDown() {
        if (layer == null) {
            return false;
        }

        return layer instanceof LayerGroup ? ((LayerGroup) layer).isTwirledDown() : false;
    }

    public int getTwirlVisibility() {
        if (layer == null) {
            return View.GONE;
        }

        return layer instanceof LayerGroup ? View.VISIBLE : View.GONE;
    }

    public int getThumbnail() {
        return layer instanceof LayerGroup ? R.drawable.ic_folder_black_24dp :
                R.drawable.ic_rect_24dp;
    }

    @Bindable({"visible", "selected"})
    public int getVisibleToggleVisibility() {
        return !isVisible() || isSelected() ? View.VISIBLE : View.GONE;
    }
}
