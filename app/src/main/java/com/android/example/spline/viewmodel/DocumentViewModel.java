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

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;

import com.android.example.spline.BR;
import com.android.example.spline.R;
import com.android.example.spline.model.Document;
import com.android.example.spline.model.Layer;
import com.android.example.spline.model.LayerGroup;
import com.android.example.spline.model.OvalLayer;
import com.android.example.spline.model.RectLayer;
import com.android.example.spline.model.SelectionGroup;
import com.android.example.spline.model.ShapeLayer;
import com.android.example.spline.model.TriangleLayer;
import com.android.example.spline.persistence.DocumentRepository;
import com.android.example.spline.util.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The ViewModel companion to Document, DocumentViewModel encapsulates Document's layers and
 * currentLayer with accessor methods as well as storing other values that don't need to be
 * persisted, such as the current visibility of the right panel.
 */
public class DocumentViewModel extends BaseObservable {

    private Document document;
    private String fileName;
    private String rectString;
    private String triangleString;
    private String ovalString;

    private List<Layer> rectLayers;
    private List<Layer> triangleLayers;
    private List<Layer> ovalLayers;

    private int viewportWidth;
    private int viewportHeight;

    private Context context;
    private DocumentRepository repository;

    private PopupMenu.OnMenuItemClickListener onMenuItemClickListener;

    public DocumentViewModel(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;

        repository = DocumentRepository.getInstance();

        rectString = context.getString(R.string.rect);
        triangleString = context.getString(R.string.triangle);
        ovalString = context.getString(R.string.oval);

        rectLayers = new ArrayList<>();
        triangleLayers = new ArrayList<>();
        ovalLayers = new ArrayList<>();

        onMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String title = (String) item.getTitle();
                if (title.equals(rectString)) {
                    addRectLayer();
                } else if (title.equals(triangleString)) {
                    addTriangleLayer();
                } else if (title.equals(ovalString)) {
                    addOvalLayer();
                }
                return true;
            }
        };

        this.document = new Document();
    }

    @Bindable
    public int getRightPanelVisibility() {
        boolean hasCurrentLayer = document.getCurrentLayer() != null;
        boolean hasChildren = true;
        if (hasCurrentLayer && document.getCurrentLayer() instanceof LayerGroup) {
            LayerGroup group = (LayerGroup) document.getCurrentLayer();
            hasChildren = group.getLayers().size() > 0;
        }
        return hasCurrentLayer && hasChildren ? View.VISIBLE : View.GONE;
    }

    @Bindable
    public String getFileName() {
        return fileName;
    }

    @Bindable
    public String getPrettyFileName() {
        return FileUtils.getPrettyFilename(fileName);
    }

    public Document getDocument() {
        return document;
    }

    public void saveDocument() {
        repository.save(fileName, document, context);
    }

    public void loadDocument() {
        Document newDoc = repository.load(fileName, context);
        if (newDoc != null) {
            this.document = newDoc;
        }
    }

    @Bindable
    public LayerGroup getRoot() {
        return document.getRoot();
    }

    @Bindable
    public Layer getCurrentLayer() {
        return document.getCurrentLayer();
    }

    public void setCurrentLayer(Layer layer) {
        if (layer != getCurrentLayer()) {
            document.setCurrentLayer(layer);
            notifyPropertyChanged(BR.currentLayer);
            notifyPropertyChanged(BR.rightPanelVisibility);
        }
    }

    public boolean hasClipboardContents() {
        return document.getClipboardLayer() != null;
    }

    public void deleteCurrentLayer() {
        Layer l = getCurrentLayer();
        if (l != null) {
            if (l instanceof SelectionGroup) {
                SelectionGroup selection = (SelectionGroup) l;
                List<Layer> selectedLayers = selection.getLayers();
                for (Layer layer : selectedLayers) {
                    document.removeLayer(layer);
                }
            }
            document.removeLayer(l);
            l = null;
            setCurrentLayer(null);
        }
    }

    public void cutCurrentLayer() {
        Layer l = getCurrentLayer();
        if (l != null) {
            document.removeLayer(l);
            document.setClipboardLayer(l);
            setCurrentLayer(null);
        }
    }

    public void copyCurrentLayer() {
        Layer l = getCurrentLayer();
        if (l != null) {
            document.setClipboardLayer(l);
        }
    }

    public void pasteClipboard() {
        Layer l = document.getClipboardLayer();
        pasteLayerCopy(l);
    }

    private void pasteLayerCopy(Layer l) {
        if (l != null) {
            Layer copy = l.copy();
            addLayer(copy);
        }
    }

    public void duplicateCurrentLayer() {
        // Duplicate copies current layer, doesn't put this layer on the clipboard
        pasteLayerCopy(getCurrentLayer());
    }

    @Bindable
    public float getViewportX() {
        return document.getViewportX();
    }

    public void setViewportX(float viewportX) {
        if (viewportX != getViewportX()) {
            document.setViewportX(viewportX);
            notifyPropertyChanged(BR.viewportX);
        }
    }

    @Bindable
    public float getViewportY() {
        return document.getViewportY();
    }

    public void setViewportY(float viewportY) {
        if (viewportY != getViewportY()) {
            document.setViewportY(viewportY);
            notifyPropertyChanged(BR.viewportY);
        }
    }

    @Bindable
    public int getViewportWidth() {
        return viewportWidth;
    }

    public void setViewportWidth(int viewportWidth) {
        if (viewportWidth != getViewportWidth()) {
            this.viewportWidth = viewportWidth;
            notifyPropertyChanged(BR.viewportWidth);
        }
    }

    @Bindable
    public int getViewportHeight() {
        return viewportHeight;
    }

    public void setViewportHeight(int viewportHeight) {
        if (viewportHeight != getViewportHeight()) {
            this.viewportHeight = viewportHeight;
            notifyPropertyChanged(BR.viewportHeight);
        }
    }

    public PopupMenu.OnMenuItemClickListener getOnMenuItemClickListener() {
        return onMenuItemClickListener;
    }

    // The following add methods generate shape layers with default position and sizes for those
    // layer types
    public void addRectLayer() {
        ShapeLayer layer = new RectLayer();
        layer.setWidth(600);
        layer.setHeight(300);
        layer.setName(rectString + " " + (rectLayers.size() + 1));
        addLayer(layer);
        rectLayers.add(layer);
    }

    public void addTriangleLayer() {
        ShapeLayer layer = new TriangleLayer();
        float width = (float) (400 / Math.sqrt(3f) * 2f);
        width = Math.round(width * 10f) / 10f;
        layer.setWidth(width);
        layer.setHeight(400);
        layer.setName(triangleString + " " + (triangleLayers.size() + 1));
        addLayer(layer);
        triangleLayers.add(layer);
    }

    public void addOvalLayer() {
        ShapeLayer layer = new OvalLayer();
        layer.setWidth(400);
        layer.setHeight(400);
        layer.setName(ovalString + " " + (ovalLayers.size() + 1));
        addLayer(layer);
        ovalLayers.add(layer);
    }

    private void addLayer(Layer layer) {
        // Center the layer in the current viewport if a majority of the layer in its
        // current position falls outside of the viewport. This effectively centers new layers, who
        // are given an initial x, y values of Integer MIN_VALUE.
        if (layer.getMidX() < -getViewportX()
                || layer.getMidX() > -getViewportX() + getViewportWidth()
                || layer.getMidY() < -getViewportY()
                || layer.getMidY() > -getViewportY() + getViewportHeight()) {
            layer.setX(-getViewportX() + getViewportWidth() / 2 - layer.getWidth() / 2);
            layer.setY(-getViewportY() + getViewportHeight() / 2 - layer.getHeight() / 2);
        }

        layer.setSelected(true);
        document.addLayer(layer);
        setCurrentLayer(layer);
    }

    public void convertSelectionToGroup() {
        Layer l = getCurrentLayer();
        if (l != null) {
            LayerGroup g;
            if (l instanceof SelectionGroup) {
                g = ((SelectionGroup) l).copy();
                deleteCurrentLayer();
            } else {
                document.removeLayer(l);
                g = new LayerGroup();
                g.addLayer(l);
            }
            addLayer(g);
        }
    }
}
