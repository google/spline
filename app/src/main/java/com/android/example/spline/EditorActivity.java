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

import android.app.ActivityManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.example.spline.databinding.ActivityEditorBinding;
import com.android.example.spline.view.DocumentView;
import com.android.example.spline.view.LayerListView;
import com.android.example.spline.viewmodel.DocumentViewModel;

import java.util.List;

/**
 * Spline's main activity. Loads a file from internal storage based on the fileName passed in the
 * intent extras. Binds the content view to Document object, configures the LayerListAdapter
 * and wires up add layer type menu.
 */
public class EditorActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String FILENAME = "filename";
    private static final String DOCUMENT = "document";

    private static final int DELETE = R.id.action_delete;
    private static final int CUT = R.id.action_cut;
    private static final int COPY = R.id.action_copy;
    private static final int PASTE = R.id.action_paste;
    private static final int DUPLICATE = R.id.action_duplicate;
    private static final int GROUP = R.id.action_group;

    private DocumentViewModel mViewModel;
    private PopupMenu mLayerTypePopup;
    private ActivityEditorBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_editor);
        setSupportActionBar(mBinding.toolbar);

        String fileName = "untitled.json";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getString(FILENAME) != null) {
            fileName = bundle.getString(FILENAME);
        }

        mViewModel = new DocumentViewModel(this, fileName);
        mViewModel.loadDocument();

        mBinding.setViewModel(mViewModel);


        // Do some necessary non-data binding view work

        // TaskDescription constructor requires us to specify an overview title bar color, so we
        // grab primary color to match the original color
        int primaryColor = getColor(R.color.colorPrimary);
        ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(
                mViewModel.getPrettyFileName(), null, primaryColor
        );

        // Set our overview task description with custom name matching the current doc file name
        setTaskDescription(td);

        mLayerTypePopup = new PopupMenu(this, mBinding.addLayerBtn);
        mLayerTypePopup.getMenuInflater().inflate(
                R.menu.menu_layer_types, mLayerTypePopup.getMenu());
        mLayerTypePopup.setOnMenuItemClickListener(mViewModel.getOnMenuItemClickListener());

        registerForContextMenu(mBinding.documentView);
        registerForContextMenu(mBinding.layerList);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mViewModel.saveDocument();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_R:
                mViewModel.addRectLayer();
                return true;
            case KeyEvent.KEYCODE_T:
                mViewModel.addTriangleLayer();
                return true;
            case KeyEvent.KEYCODE_O:
                mViewModel.addOvalLayer();
                return true;
        }

        if (mBinding.documentView.hasFocus()) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DEL:
                case KeyEvent.KEYCODE_FORWARD_DEL:
                    return onContextMenuAction(DELETE);
            }

            if (event.isCtrlPressed()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_X:
                        return onContextMenuAction(CUT);
                    case KeyEvent.KEYCODE_C:
                        return onContextMenuAction(COPY);
                    case KeyEvent.KEYCODE_V:
                        return onContextMenuAction(PASTE);
                    case KeyEvent.KEYCODE_D:
                        return onContextMenuAction(DUPLICATE);
                    case KeyEvent.KEYCODE_G:
                        return onContextMenuAction(GROUP);
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, v, info);

        boolean shouldShowContextMenu = true;
        boolean showCurrentLayerItems = false;
        boolean hasClipboardContents = mViewModel.hasClipboardContents();

        if (v instanceof DocumentView) {
            DocumentView dv = (DocumentView) v;
            shouldShowContextMenu = dv.shouldShowContextMenu();
            showCurrentLayerItems = dv.shouldShowCurrentLayerContextItems();
        } else if (v instanceof LayerListView) {
            //LayerListView llv = (LayerListView) v;
            showCurrentLayerItems = true;
        }

        if (shouldShowContextMenu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_context, menu);

            MenuItem item = menu.findItem(CUT);
            item.setVisible(showCurrentLayerItems);

            item = menu.findItem(COPY);
            item.setVisible(showCurrentLayerItems);

            item = menu.findItem(PASTE);
            item.setVisible(showCurrentLayerItems || hasClipboardContents);
            item.setEnabled(hasClipboardContents);

            item = menu.findItem(DUPLICATE);
            item.setVisible(showCurrentLayerItems);

            item = menu.findItem(DELETE);
            item.setVisible(showCurrentLayerItems);

            item = menu.findItem(GROUP);
            item.setVisible(showCurrentLayerItems);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return onContextMenuAction(item.getItemId()) || super.onContextItemSelected(item);
    }

    private boolean onContextMenuAction(int id) {
        switch (id) {
            case CUT:
                mViewModel.cutCurrentLayer();
                return true;
            case COPY:
                mViewModel.copyCurrentLayer();
                return true;
            case PASTE:
                mViewModel.pasteClipboard();
                return true;
            case DUPLICATE:
                mViewModel.duplicateCurrentLayer();
                return true;
            case DELETE:
                mViewModel.deleteCurrentLayer();
                return true;
            case GROUP:
                mViewModel.convertSelectionToGroup();
                return true;
        }
        return false;
    }

    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, Menu menu, int deviceId) {
        super.onProvideKeyboardShortcuts(data, menu, deviceId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(DOCUMENT, mViewModel.getDocument());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        MenuPopupHelper menuHelper = new MenuPopupHelper(this,
                (MenuBuilder) mLayerTypePopup.getMenu(),
                mBinding.addLayerBtn);
        menuHelper.setForceShowIcon(true);
        menuHelper.show();
    }
}
