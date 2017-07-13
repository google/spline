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

import android.databinding.BindingAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;

/**
 * Binding adapters for binding File objects to UI elements
 */
public class FileBindingAdapters {

    /**
     * Simple binding adapter to convert long timestamp to a human-readable date.
     * @param view
     * @param date
     */
    @BindingAdapter("android:text")
    public static void setText(TextView view, long date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String formatted = format.format(date);
        view.setText(formatted);
    }
}
