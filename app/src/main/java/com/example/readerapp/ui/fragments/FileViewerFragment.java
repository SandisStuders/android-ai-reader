package com.example.readerapp.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readerapp.R;
import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.models.readableFile.ReadableFileViewModel;
import com.example.readerapp.databinding.FragmentFileViewerBinding;
import com.example.readerapp.ui.activities.EpubViewerActivity;
import com.example.readerapp.ui.adapters.FilesRecyclerViewAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileViewerFragment extends Fragment implements FilesRecyclerViewAdapter.FileOptionListener {

    private FragmentFileViewerBinding binding;
    private RecyclerView filesRecyclerView;
    private BottomNavigationView bottomFileListSelectionBar;
    private FilesRecyclerViewAdapter adapter;
    private ReadableFileViewModel mReadableFileViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFileViewerBinding.inflate(inflater, container, false);

        filesRecyclerView = binding.filesRecyclerView;
        bottomFileListSelectionBar = binding.bottomFileSelectionBar;

        adapter = new FilesRecyclerViewAdapter(this);
        filesRecyclerView.setAdapter(adapter);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Observer<List<ReadableFile>> observer = new Observer<List<ReadableFile>>() {
            @Override
            public void onChanged(List<ReadableFile> readableFiles) {
                adapter.setReadableFileDetails((ArrayList<ReadableFile>) readableFiles);
            }
        };

        mReadableFileViewModel = new ViewModelProvider(this).get(ReadableFileViewModel.class);
        if (savedInstanceState == null) {
            mReadableFileViewModel.initialize("RECENT", observer);
        } else {
            String savedListType = savedInstanceState.getString("CURRENT_LIST_TYPE", "RECENT");
            mReadableFileViewModel.initialize(savedListType, observer);
        }

        bottomFileListSelectionBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.recentFiles) {
                    mReadableFileViewModel.changeListType("RECENT", observer);
                    return true;
                } else if (itemId == R.id.favoriteFiles) {
                    mReadableFileViewModel.changeListType("FAVORITE", observer);
                    return true;
                } else if (itemId == R.id.allFiles) {
                    Log.d("MyLogs", "Navigated to All files list");
                    mReadableFileViewModel.changeListType("ALL", observer);
                    return true;
                }

                return false;
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String currentListType = Objects.requireNonNull(mReadableFileViewModel.getActiveListType());
        outState.putString("CURRENT_LIST_TYPE", currentListType);
    }

    @Override
    public void addToFavorites(ReadableFile readableFile) {
        mReadableFileViewModel.addToFavorites(readableFile);
    }

    @Override
    public void removeFromFavorites(ReadableFile readableFile) {
        mReadableFileViewModel.removeFromFavorites(readableFile);
    }

    @Override
    public void removeFromRecent(ReadableFile readableFile) {
        mReadableFileViewModel.removeRecentTime(readableFile);
    }

    @Override
    public void fileOpened(ReadableFile readableFile, View v) {
        if (!mReadableFileViewModel.prepareFileOpen(readableFile)) {
            return;
        }

        Context context = v.getContext();
        Intent intent = new Intent(context, EpubViewerActivity.class);
        intent.putExtra("URI_STRING", readableFile.getContentUri());
        intent.putExtra("FILE_NAME", readableFile.getFileName());
        intent.putExtra("FILE_RELATIVE_PATH", readableFile.getRelativePath());
        context.startActivity(intent);
    }

    @Override
    public String itemLongClick() {
        return mReadableFileViewModel.getActiveListType();
    }
}
