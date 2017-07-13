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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.android.example.spline.viewmodel.PickerViewModel;

import java.io.File;

/**
 * The launcher activity for the application that lets the user select a file from a list of files
 * in a master-detail interface.
 */
public class DocumentPickerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);

        PickerViewModel viewModel = PickerViewModel.getInstance();
        File internalDirectory = getFilesDir();
        for (File f : internalDirectory.listFiles()) {
            viewModel.addFile(f);
        }

        // Only add our fragments on first run of the activity
        if (savedInstanceState == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            ft.add(R.id.list_fragment_container, new ListFragment());
            DetailFragment df = new DetailFragment();
            ft.add(R.id.detail_fragment_container, df);

            ft.commit();
        }
    }
}
