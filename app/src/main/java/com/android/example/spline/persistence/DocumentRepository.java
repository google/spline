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
package com.android.example.spline.persistence;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.android.example.spline.model.Document;
import com.android.example.spline.model.Layer;
import com.android.example.spline.model.LayerGroup;
import com.android.example.spline.model.OvalLayer;
import com.android.example.spline.model.RectLayer;
import com.android.example.spline.model.SelectionGroup;
import com.android.example.spline.model.ShapeLayer;
import com.android.example.spline.model.TriangleLayer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.UUID;

/**
 * A singleton class for loading and persisting documents to the local file system or cloud storage
 * as JSON flat files.
 */

public class DocumentRepository {

    private static final String TYPE = "type";
    private static final String CURRENT_LAYER = "currentLayer";
    private static final String CLIPBOARD_LAYER = "clipboardLayer";
    private static final String LAYERS = "layers";
    private static final String LAYER_GROUP = "LayerGroup";
    private static final String SELECTION_GROUP = "SelectionGroup";
    private static final String RECT_LAYER = "RectLayer";
    private static final String OVAL_LAYER = "OvalLayer";
    private static final String TRIANGLE_LAYER = "TriangleLayer";

    private static DocumentRepository instance = null;

    private Gson gson;

    protected DocumentRepository() {
        GsonBuilder builder = new GsonBuilder();
        DocumentTypeAdapter documentAdapter = new DocumentTypeAdapter();
        builder.registerTypeAdapter(Document.class, documentAdapter);
        builder.setPrettyPrinting();
        gson = builder.create();
    }

    public static DocumentRepository getInstance() {
        if (instance == null) {
            instance = new DocumentRepository();
        }
        return instance;
    }

    public void save(String filename, Document document, Context context) {
        try {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            String json = gson.toJson(document);
            outputStream.write(json.getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Document load(String filename, Context context) {
        String json = null;
        try {
            FileInputStream inputStream = context.openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            json = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gson.fromJson(json, Document.class);
    }

    /**
     * Custom serialize/deserialize class for Document objects. Primary custom function is to handle
     * the serialization/deserialization of the currently selected layer with the layer's UUID or a
     * list of UUIDs of a SelectionGroup's layers to persist the current layer as a reference to a
     * layer in the layer tree, rather than it's own layer.
     */
    private class DocumentTypeAdapter implements
            JsonSerializer<Document>, JsonDeserializer<Document> {

        private Gson g;

        public DocumentTypeAdapter() {
            GsonBuilder builder = new GsonBuilder();
            LayerTypeAdapter layerAdapter = new LayerTypeAdapter();
            builder.registerTypeAdapter(Layer.class, layerAdapter);
            builder.registerTypeAdapter(LayerGroup.class, layerAdapter);
            g = builder.create();
        }

        @Override
        public JsonElement serialize(Document src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = g.toJsonTree(src).getAsJsonObject();

            if (src.getCurrentLayer() != null) {
                Layer current = src.getCurrentLayer();

                // If the current selection was a selection group, serialize the group as an array
                // of ids, other add the single string id of the current layer
                if (current instanceof SelectionGroup) {
                    SelectionGroup sg = (SelectionGroup) current;
                    JsonArray jsonIds = new JsonArray();
                    for (Layer l : sg.getLayers()) {
                        jsonIds.add(l.getId().toString());
                    }
                    obj.add(CURRENT_LAYER, jsonIds);
                } else {
                    obj.addProperty(CURRENT_LAYER, src.getCurrentLayer().getId().toString());
                }
            }

            return obj;
        }

        @Override
        public Document deserialize(JsonElement json, Type typeOfT,
                                    JsonDeserializationContext context) {

            JsonObject obj = json.getAsJsonObject();
            JsonElement currentLayerEl = obj.remove(CURRENT_LAYER);

            Document document = g.fromJson(obj, Document.class);
            LayerGroup root = document.getRoot();

            if (root != null && currentLayerEl != null) {
                Layer currentLayer = null;
                if (currentLayerEl.isJsonArray()) {
                    JsonArray jsonIds = currentLayerEl.getAsJsonArray();

                    SelectionGroup selection = new SelectionGroup();
                    for (JsonElement el : jsonIds) {
                        UUID id = UUID.fromString(el.getAsString());
                        Layer l = root.findLayerById(id);
                        if (l != null) {
                            selection.addLayer(l);
                        }
                    }
                    currentLayer = selection;
                } else {
                    UUID currentLayerId = UUID.fromString(currentLayerEl.getAsString());
                    currentLayer = root.findLayerById(currentLayerId);
                }

                document.setCurrentLayer(currentLayer);
            }
            return document;
        }
    }

    /**
     * Custom serialize/deserialize class for Layer and LayerGroup objects. Calls itself recursively
     * to serialize all Layers in a LayerGroup tree.
     */
    private class LayerTypeAdapter implements JsonSerializer<Layer>, JsonDeserializer<Layer> {

        @Override
        public JsonElement serialize(Layer src, Type typeOfSrc, JsonSerializationContext context) {
            Gson g = new Gson();
            JsonObject obj = g.toJsonTree(src).getAsJsonObject();
            obj.addProperty(TYPE, src.getClass().getSimpleName());

            if (src instanceof LayerGroup) {
                JsonArray jsonLayers = new JsonArray();

                LayerGroup lg = (LayerGroup) src;
                for (Layer l : lg.getLayers()) {
                    jsonLayers.add(serialize(l, null, context));
                }

                obj.add(LAYERS, jsonLayers);
            }

            return obj;
        }

        @Override
        public Layer deserialize(JsonElement json, Type arg1,
                                 JsonDeserializationContext arg2) throws JsonParseException {
            Layer layer = null;
            Type type = LayerGroup.class;
            Gson g = new Gson();

            JsonObject obj = json.getAsJsonObject();
            JsonElement typeEl = obj.get(TYPE);

            if (typeEl != null && typeEl.getAsString() != null) {
                String t = typeEl.getAsString();
                if (t.equals(LAYER_GROUP) || t.equals(SELECTION_GROUP)) {
                    if (t.equals(LAYER_GROUP)) {
                        type = LayerGroup.class;
                    } else {
                        type = SelectionGroup.class;
                    }
                    JsonElement layersEl = obj.remove(LAYERS);
                    LayerGroup layerGroup = g.fromJson(obj, type);

                    if (layersEl != null && layersEl.getAsJsonArray() != null) {
                        JsonArray layersArray = layersEl.getAsJsonArray();

                        for (JsonElement layerEl : layersArray) {
                            Layer l = deserialize(layerEl, null, arg2);
                            if (l != null) {
                                layerGroup.addLayer(l);
                            }
                        }
                    }

                    layer = layerGroup;
                } else {
                    if (t.equals(OVAL_LAYER)) {
                        type = OvalLayer.class;
                    } else if (t.equals(TRIANGLE_LAYER)) {
                        type = TriangleLayer.class;
                    } else {
                        type = RectLayer.class;
                    }

                    ShapeLayer sl = g.fromJson(json, type);
                    // Necessary to add change listener after deserializing because fromJson
                    // doesn't call setColor and instead clobbers whatever color object was set in
                    // the constructor.
                    sl.addOnColorChangeListener();
                    layer = sl;
                }
            }

            return layer;
        }
    }
}
