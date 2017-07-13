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
package com.android.example.spline.bindingadapter;

import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;

import com.android.example.spline.view.DocumentView;
import com.android.example.spline.view.LayerListView;
import com.android.example.spline.view.RulerView;

/**
 * These InverseBindingMethod annotations are necessary for 2-way binding of the currentLayer
 * properties of both of the views that bind to them. Unlike HuePickerBindingAdapter, the synthetic
 * AttrChanged InverseBindingListener attributes are used to implement 2-way binding. As a result,
 * there is no other BindingAdapter code that needs to be implemented.
 */
@InverseBindingMethods({
        @InverseBindingMethod(type = RulerView.class,
                attribute = "viewportStart",
                method = "getViewportStart"),

        @InverseBindingMethod(type = RulerView.class,
                attribute = "highlightStart",
                method = "getHighlightStart"),

        @InverseBindingMethod(type = DocumentView.class,
                attribute = "viewportX",
                method = "getViewportX"),

        @InverseBindingMethod(type = DocumentView.class,
                attribute = "viewportY",
                method = "getViewportY"),

        @InverseBindingMethod(type = DocumentView.class,
                attribute = "viewportWidth",
                method = "getViewportWidth"),

        @InverseBindingMethod(type = DocumentView.class,
                attribute = "viewportHeight",
                method = "getViewportHeight"),

        @InverseBindingMethod(type = DocumentView.class,
                attribute = "currentLayer",
                method = "getCurrentLayer"),

        @InverseBindingMethod(type = LayerListView.class,
                attribute = "currentLayer",
                method = "getCurrentLayer")
})
public class DocumentViewModelBindingAdapters {

}
