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
package com.android.example.spline;

import android.databinding.InverseBindingListener;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.example.spline.databinding.LayoutLayerRowBinding;
import com.android.example.spline.model.Layer;
import com.android.example.spline.model.LayerGroup;
import com.android.example.spline.viewmodel.LayerRowCallbacks;
import com.android.example.spline.viewmodel.LayerRowViewModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RecyclerView adapter for the LayerListView layer tree component. Converts Layer tree specified by
 * setRoot into a list of visible "twirled down" layers similar to file tree interfaces in file
 * explorers and IDEs. Handles twirling of LayerGroups and addition / removal of items.
 */
public class LayerListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_INVALID = -1;
    public static final int TYPE_LAYER = 1;

    private Layer mCurrentLayer;
    private LayerRowCallbacks mLayerRowCallbacks;
    private LayerGroup mRoot;
    private ObservableList<Layer> mTwirledDownLayers;
    private ObservableList.OnListChangedCallback<ObservableList<Layer>> mOnListChangedCallback;
    private InverseBindingListener mCurrentLayerAttrChangedListener;

    public LayerListAdapter() {

        mOnListChangedCallback = new ObservableList
                .OnListChangedCallback<ObservableList<Layer>>() {
            @Override
            public void onChanged(ObservableList<Layer> layers) {

            }

            @Override
            public void onItemRangeChanged(ObservableList<Layer> layers, int i, int i1) {

            }

            @Override
            public void onItemRangeInserted(ObservableList<Layer> layers, int start, int count) {
                addListChangedCallbacks(layers, start, start + count);

                for (int i = start; i < start + count; i++) {
                    addLayer(layers.get(i));
                }
            }

            @Override
            public void onItemRangeMoved(ObservableList<Layer> layers, int i, int i1, int i2) {

            }

            @Override
            public void onItemRangeRemoved(ObservableList<Layer> layers, int start, int count) {
                // Because we don't know which item(s) were removed, we have to
                // reconstruct it ourselves
                Set<Layer> currentTwirled = new HashSet<Layer>(getTwirledDownLayersForGroup(mRoot));
                Set<Layer> oldTwirled = new HashSet<Layer>(mTwirledDownLayers);
                oldTwirled.removeAll(currentTwirled);
                for (Layer l : oldTwirled) {
                    removeLayer(l);
                }
            }
        };
    }

    public void setRowCallbacks(LayerRowCallbacks callbacks) {
        mLayerRowCallbacks = callbacks;
    }

    public Layer getCurrentLayer() {
        return mCurrentLayer;
    }

    public void setCurrentLayer(Layer layer) {
        if (layer != mCurrentLayer) {
            mCurrentLayer = layer;

            if (mCurrentLayer != null && mCurrentLayer.getParent() instanceof LayerGroup) {
                LayerGroup parent = (LayerGroup) mCurrentLayer.getParent();
                openTwirlLayerGroup(parent);
            }

            if (mCurrentLayerAttrChangedListener != null) {
                mCurrentLayerAttrChangedListener.onChange();
            }
        }
    }

    public void setRoot(LayerGroup root) {
        mRoot = root;
        if (root != null) {
            mRoot.getLayers().addOnListChangedCallback(mOnListChangedCallback);
            addListChangedCallbacks(mRoot.getLayers());
        }
        mTwirledDownLayers = getTwirledDownLayersForGroup(mRoot);
    }

    private void addListChangedCallbacks(List<Layer> layers) {
        addListChangedCallbacks(layers, 0, layers.size());
    }

    private void addListChangedCallbacks(List<Layer> layers, int start, int end) {
        for (int i = start; i < end; i++) {
            Layer l = layers.get(i);
            if (l instanceof LayerGroup) {
                LayerGroup lg = (LayerGroup) l;
                ObservableList<Layer> childLayers = lg.getLayers();
                // Add list listener for future changes to the layer group's list of children
                childLayers.addOnListChangedCallback(mOnListChangedCallback);
                // Recursive call to add property listeners to each child layer
                addListChangedCallbacks(childLayers);
            }
        }
    }

    public ObservableList<Layer> getTwirledDownLayersForGroup(LayerGroup root) {
        ObservableList<Layer> twirledDownLayers = new ObservableArrayList<>();

        for (Layer l : root.getLayers()) {
            twirledDownLayers.add(l);
            if (l instanceof LayerGroup) {
                LayerGroup group = (LayerGroup) l;
                if (group.isTwirledDown()) {
                    int i = twirledDownLayers.indexOf(l);
                    List<Layer> childLayers = getTwirledDownLayersForGroup(group);
                    twirledDownLayers.addAll(i + 1, childLayers);
                }
            }
        }

        return twirledDownLayers;
    }

    public void setCurrentLayerAttrChanged(InverseBindingListener currentLayerAttrChanged) {
        mCurrentLayerAttrChangedListener = currentLayerAttrChanged;
    }

    public void openTwirlLayerGroup(LayerGroup group) {
        if (!group.isTwirledDown()) {
            group.openTwirl();
            twirlLayerGroup(group, group.isTwirledDown());
        }
    }

    public void toggleTwirlLayerGroup(LayerGroup group) {
        group.toggleTwirl();
        twirlLayerGroup(group, group.isTwirledDown());
    }

    public void twirlLayerGroup(LayerGroup group, boolean twirledDown) {
        if (mTwirledDownLayers != null) {
            int i = mTwirledDownLayers.indexOf(group);
            List<Layer> childLayers = getTwirledDownLayersForGroup(group);

            if (twirledDown) {
                mTwirledDownLayers.addAll(i + 1, childLayers);
                notifyItemRangeInserted(i + 1, childLayers.size());
            } else {
                mTwirledDownLayers.removeAll(childLayers);
                notifyItemRangeRemoved(i + 1, childLayers.size());
            }
        }
    }

    public void addLayer(Layer layer) {
        if (layer.isVisibleInLayerList()) {
            List<Layer> newTDL = getTwirledDownLayersForGroup(mRoot);
            int i = newTDL.indexOf(layer);
            mTwirledDownLayers.add(i, layer);
            notifyItemRangeInserted(i, 1);
        }
    }

    public void removeLayer(Layer layer) {
        if (layer != null && mTwirledDownLayers.contains(layer)) {
            int i = mTwirledDownLayers.indexOf(layer);
            mTwirledDownLayers.remove(layer);
            notifyItemRangeRemoved(i, 1);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;

        switch (viewType) {
            case TYPE_LAYER:
                vh = LayerRowHolder.create(
                        LayoutInflater.from(
                                parent.getContext()), parent, mLayerRowCallbacks
                );
                break;
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        switch (viewType) {
            case TYPE_LAYER:
                Layer layer = mTwirledDownLayers.get(position);
                LayerRowHolder lh = (LayerRowHolder) holder;
                lh.bindTo(layer);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mTwirledDownLayers.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Stub for now, will be needed for folders
        return TYPE_LAYER;
    }

    public static class LayerRowHolder extends RecyclerView.ViewHolder {
        private LayoutLayerRowBinding binding;
        private LayerRowCallbacks callbacks;
        private LayerRowViewModel viewModel;

        public LayerRowHolder(
                LayoutLayerRowBinding binding, LayerRowCallbacks callbacks) {
            super(binding.getRoot());
            this.binding = binding;
            this.callbacks = callbacks;
            this.viewModel = new LayerRowViewModel();
        }

        static LayerRowHolder create(LayoutInflater inflater,
                                     ViewGroup parent, LayerRowCallbacks callbacks) {
            LayoutLayerRowBinding binding = LayoutLayerRowBinding.inflate(inflater, parent, false);
            return new LayerRowHolder(binding, callbacks);
        }

        public void bindTo(final Layer layer) {
            viewModel.setLayer(layer);
            binding.setRow(viewModel);
            binding.setCallbacks(callbacks);
            binding.executePendingBindings();
        }
    }
}
