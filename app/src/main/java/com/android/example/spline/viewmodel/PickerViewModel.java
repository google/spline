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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.android.example.spline.BR;
import com.android.example.spline.EditorActivity;
import com.android.example.spline.R;
import com.android.example.spline.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Viewmodel for sharing data between the fragments of DocumentPickerActivity. For simplicity, this
 * component maintains a reference to the application context, which isn't recommended for a
 * viewmodel object.
 */
public class PickerViewModel extends BaseObservable {
    private static PickerViewModel sViewModel;

    private final ObservableList<File> mFiles;
    private File mSelectedFile;

    private PickerViewModel() {
        mFiles = new ObservableArrayList<>();
    }

    public static PickerViewModel getInstance() {
        if (sViewModel == null) {
            sViewModel = new PickerViewModel();
        }
        return sViewModel;
    }

    public void addFile(File f) {
        if (!mFiles.contains(f)) {
            mFiles.add(f);
        }
    }

    public ObservableList<File> getFiles() {
        return mFiles;
    }

    public File getFile(int i) {
        return mFiles.get(i);
    }

    @Bindable
    public File getSelectedFile() {
        return mSelectedFile;
    }

    public void setSelectedFile(File file) {
        mSelectedFile = file;
        notifyPropertyChanged(BR.selectedFile);
    }

    public void openFile(Context context, File file) {
        if (file != null) {
            openFile(context, file.getName(), false);
        }
    }

    public void openFile(Context context, String filename, boolean createNew) {
        if (createNew) {
            try {
                File internalDirectory = context.getFilesDir();
                File file = new File(internalDirectory.getAbsolutePath() + "/" + filename);
                file.createNewFile();
                addFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Intent intent = new Intent(context, EditorActivity.class);
        intent.putExtra(EditorActivity.FILENAME, filename);
        context.startActivity(intent);
    }

    public void showNewFileDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.new_file));

        FrameLayout container = new FrameLayout(context);
        final EditText input = new EditText(context);
        input.setMaxLines(1);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = Math.round(context.getResources().getDimension(R.dimen.dialog_margin));
        params.leftMargin = margin;
        params.rightMargin = margin;
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton(context.getString(R.string.create), new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = input.getText().toString();
                if (fileName == null || fileName.length() == 0) {
                    fileName = "untitled.json";
                } else if (fileName.equals(FileUtils.getPrettyFilename(fileName))) {
                    fileName += ".json";
                }
                openFile(context, fileName, true);
            }
        });

        builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
