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
import android.databinding.InverseBindingListener;
import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;
import android.databinding.adapters.ListenerUtil;

import com.android.example.spline.R;
import com.android.example.spline.view.HuePicker;

@InverseBindingMethods({
        @InverseBindingMethod(type = HuePicker.class,
                attribute = "hue",
                method = "getHue")
})
public class HuePickerBindingAdapters {

    /**
     * This is how you implement 2-way data binding of a custom attribute if you want to maintain
     * a custom listener interface - OnHueChangeListener, in this case - while also leveraging
     * the synthetic attributes hueAttrChanged (the InverseBindingListener). For an example of
     * how to implement custom 2-way binding with just the synthetic AttrChanged attribute's
     * InverseBindingListener, see DocumentView or LayerListView's currentLayer 2-way binding
     * (along with the InverseBindingMethod declarations in DocumentViewModelBindingAdapters).
     *
     * @param view                   The view we're 2-way binding to.
     * @param onHueChangeListener    The OnHueChangeListener bound by the developer on the view
     *                               in question.
     * @param inverseBindingListener Synthetic attribute representing an InverseBindingListener
     *                               for changes to the hue attribute.
     */
    @BindingAdapter(value = {"onHueChange", "hueAttrChanged"}, requireAll = false)
    public static void setListeners(HuePicker view,
                                    final HuePicker.OnHueChangeListener onHueChangeListener,
                                    final InverseBindingListener inverseBindingListener) {
        HuePicker.OnHueChangeListener newListener;
        if (inverseBindingListener == null) {
            // If the synthetic listener param is null, just keep track of the listener set in
            // the binding
            newListener = onHueChangeListener;
        } else {
            // Otherwise define a new listener to wraps both listeners and invoke them at the
            // right time within the new listener if they are non-null
            newListener = new HuePicker.OnHueChangeListener() {
                @Override
                public void onHueChange(HuePicker huePicker, float hue) {
                    if (onHueChangeListener != null) {
                        onHueChangeListener.onHueChange(huePicker, hue);
                    }
                    inverseBindingListener.onChange();
                }
            };
        }

        // Use track listener to record listener we're adding and return any old listeners
        // registered through this method so that on rebind we don't add duplicate listeners
        HuePicker.OnHueChangeListener oldListener = ListenerUtil.trackListener(view, newListener,
                R.id.hueChangeListener);

        // Remove the old listener we set using this BindingAdapter, if there is one
        if (oldListener != null) {
            view.removeHueChangeListener(oldListener);
        }

        // Add our new listener
        if (newListener != null) {
            view.addHueChangeListener(newListener);
        }
    }
}
