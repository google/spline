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
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.android.example.spline.LayerListAdapter;
import com.android.example.spline.model.Layer;
import com.android.example.spline.model.LayerGroup;
import com.android.example.spline.viewmodel.LayerRowCallbacks;

/**
 * RecyclerView subclass for spline document layer list. Mainly exists to pass on data bound
 * attributes to the adapter.
 */
public class LayerListView extends RecyclerView {

    private LayerListAdapter mAdapter;

    public LayerListView(Context context) {
        super(context);
        init(context);
    }

    public LayerListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mAdapter = new LayerListAdapter();
        LayerRowCallbacks callbacks = new LayerRowCallbacks(context, mAdapter);
        mAdapter.setRowCallbacks(callbacks);
        setAdapter(mAdapter);
        LinearLayoutManager lm = new LinearLayoutManager(context);
        lm.setOrientation(VERTICAL);
        setLayoutManager(lm);
    }

    public Layer getCurrentLayer() {
        return mAdapter.getCurrentLayer();
    }

    public void setCurrentLayer(Layer currentLayer) {
        mAdapter.setCurrentLayer(currentLayer);
    }

    public void setCurrentLayerAttrChanged(InverseBindingListener listener) {
        mAdapter.setCurrentLayerAttrChanged(listener);
    }

    public void setRoot(LayerGroup root) {
        mAdapter.setRoot(root);
    }
}
