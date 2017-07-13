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

import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.example.spline.databinding.FragmentListBinding;
import com.android.example.spline.databinding.LayoutFileRowBinding;
import com.android.example.spline.model.Layer;
import com.android.example.spline.viewmodel.PickerViewModel;

import java.io.File;

/**
 * List fragment used in master-detail DocumentPickerActivity UI
 */
public class ListFragment extends Fragment {

    FragmentListBinding mBinding;
    RecyclerView mList;
    private FileAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentListBinding.inflate(inflater, container, false);

        mList = mBinding.documentList;
        mList.setLayoutManager(new LinearLayoutManager(getActivity()));

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {
        PickerViewModel viewModel = PickerViewModel.getInstance();
        ObservableList<File> files = viewModel.getFiles();

        if (mAdapter == null) {
            mAdapter = new FileAdapter();
            mList.setAdapter(mAdapter);
        }

        mAdapter.setFiles(files);
        mBinding.setViewModel(viewModel);
    }

    private class FileAdapter extends RecyclerView.Adapter<DocumentHolder> {

        private ObservableList.OnListChangedCallback mOnListChangedCallback;
        private ObservableList<File> mFiles;

        public FileAdapter() {
            mOnListChangedCallback = new ObservableList
                    .OnListChangedCallback<ObservableList<Layer>>() {
                @Override
                public void onChanged(ObservableList<Layer> layers) {

                }

                @Override
                public void onItemRangeChanged(ObservableList<Layer> layers, int i, int i1) {

                }

                @Override
                public void onItemRangeInserted(ObservableList<Layer> layers, int start, int
                        count) {
                    notifyItemRangeInserted(start, count);
                }

                @Override
                public void onItemRangeMoved(ObservableList<Layer> sender, int fromPosition, int
                        toPosition, int itemCount) {

                }

                @Override
                public void onItemRangeRemoved(ObservableList<Layer> sender, int positionStart,
                                               int itemCount) {

                }
            };
        }

        public void setFiles(ObservableList<File> files) {
            mFiles = files;
            mFiles.addOnListChangedCallback(mOnListChangedCallback);
            notifyDataSetChanged();
        }

        @Override
        public DocumentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            LayoutFileRowBinding binding = DataBindingUtil
                    .inflate(layoutInflater, R.layout.layout_file_row, parent, false);
            return new DocumentHolder(binding);
        }

        @Override
        public void onBindViewHolder(DocumentHolder holder, int position) {
            File file = mFiles.get(position);
            holder.bindTo(file);
        }

        @Override
        public int getItemCount() {
            return mFiles.size();
        }
    }

    private class DocumentHolder extends RecyclerView.ViewHolder {
        private LayoutFileRowBinding binding;

        public DocumentHolder(LayoutFileRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindTo(File file) {
            binding.setFile(file);
            binding.setViewModel(PickerViewModel.getInstance());
            binding.executePendingBindings();
        }
    }
}
