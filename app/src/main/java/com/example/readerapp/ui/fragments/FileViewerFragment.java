package com.example.readerapp.ui.fragments;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.example.readerapp.utils.HelperFunctions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FileViewerFragment extends Fragment implements FilesRecyclerViewAdapter.FileOptionListener {

    private FragmentFileViewerBinding binding;
    private RecyclerView filesRecyclerView;
    private BottomNavigationView bottomFileListSelectionBar;
    private FilesRecyclerViewAdapter adapter;
    private ReadableFileViewModel mReadableFileViewModel;

//    private ArrayList<ReadableFile> favoriteFileDetails = new ArrayList<>();
//    private ArrayList<ReadableFile> allFileDetails = new ArrayList<>();
//    private ArrayList<ReadableFile> recentFileDetails = new ArrayList<>();
    private String currentListType;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFileViewerBinding.inflate(inflater, container, false);

        filesRecyclerView = binding.filesRecyclerView;
        bottomFileListSelectionBar = binding.bottomFileSelectionBar;

        adapter = new FilesRecyclerViewAdapter(this);
        filesRecyclerView.setAdapter(adapter);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mReadableFileViewModel = new ViewModelProvider(this).get(ReadableFileViewModel.class);
        if (savedInstanceState == null) {
            mReadableFileViewModel.initialize("RECENT");
        } else {
            String savedListType = savedInstanceState.getString("CURRENT_LIST_TYPE", "RECENT");
            mReadableFileViewModel.initialize(savedListType);
        }

        mReadableFileViewModel.getUiState().observe(getViewLifecycleOwner(), uiState -> {
            ArrayList<ReadableFile> initialReadableFileList = (ArrayList<ReadableFile>) uiState.getCurrentReadableFileList().getValue();
            adapter.setReadableFileDetails(initialReadableFileList);
            uiState.getCurrentReadableFileList().observe(getViewLifecycleOwner(), readableFiles -> {
                adapter.setReadableFileDetails((ArrayList<ReadableFile>) readableFiles);
            });
        });

        // to the viewmodel
//        ArrayList<ReadableFile> fileDetails = mReadableFileViewModel.getEpubFileList();
//        mReadableFileViewModel.insert(fileDetails);




//        adapter.setCurrentListType(currentListType);
//        if (Objects.equals(currentListType, "RECENT")) {
//            adapter.setReadableFileDetails(recentFileDetails);
//        } else if (Objects.equals(currentListType, "FAVORITE")) {
//            adapter.setReadableFileDetails(favoriteFileDetails);
//        } else if (Objects.equals(currentListType, "ALL")) {
//            adapter.setReadableFileDetails(allFileDetails);
//        }
        // to the view model

        bottomFileListSelectionBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.recentFiles) {
                    mReadableFileViewModel.changeListType("RECENT");
                    currentListType = "RECENT";
//                    adapter.setCurrentListType(currentListType);
//                    adapter.setReadableFileDetails(recentFileDetails);
                    return true;
                } else if (itemId == R.id.favoriteFiles) {
                    mReadableFileViewModel.changeListType("FAVORITE");
                    currentListType = "FAVORITE";
//                    adapter.setCurrentListType(currentListType);
//                    adapter.setReadableFileDetails(favoriteFileDetails);
                    return true;
                } else if (itemId == R.id.allFiles) {
                    mReadableFileViewModel.changeListType("ALL");
                    currentListType = "ALL";
//                    adapter.setCurrentListType(currentListType);
//                    adapter.setReadableFileDetails(allFileDetails);
                    return true;
                }

                return false;
            }
        });

//        mReadableFileViewModel.getFavoriteFiles().observe(getViewLifecycleOwner(), new Observer<List<ReadableFile>>() {
//            @Override
//            public void onChanged(List<ReadableFile> readableFiles) {
//                if (readableFiles != null) {
//                    favoriteFileDetails = (ArrayList<ReadableFile>) readableFiles;
//                    if (Objects.equals(currentListType, "FAVORITE")) {
//                        adapter.setReadableFileDetails(favoriteFileDetails);
//                    }
//                }
//            }
//        });
//
//        mReadableFileViewModel.getAllReadableFiles().observe(getViewLifecycleOwner(), new Observer<List<ReadableFile>>() {
//            @Override
//            public void onChanged(List<ReadableFile> readableFiles) {
//                if (readableFiles != null) {
//                    ArrayList<ReadableFile> filesOnDevice = mReadableFileViewModel.getEpubFileList();
//                    allFileDetails = (ArrayList<ReadableFile>) readableFiles;
//
//                    Set<String> deviceFilePaths = new HashSet<>();
//                    for (ReadableFile file : filesOnDevice) {
//                        String fileFullPath = file.getRelativePath() + file.getFileName();
//                        deviceFilePaths.add(fileFullPath);
//                    }
//
//                    ArrayList<ReadableFile> filesToDelete = new ArrayList<>();
//                    for (ReadableFile dbFile : readableFiles) {
//                        String fileFullPath = dbFile.getRelativePath() + dbFile.getFileName();
//                        if (!deviceFilePaths.contains(fileFullPath)) {
//                            filesToDelete.add(dbFile);
//                        }
//                    }
//
//                    if (!filesToDelete.isEmpty()) {
//                        mReadableFileViewModel.deleteFiles(filesToDelete);
//                    }
//
//                    if (Objects.equals(currentListType, "ALL")) {
//                        adapter.setReadableFileDetails(allFileDetails);
//                    }
//                }
//            }
//        });
//
//        mReadableFileViewModel.getRecentFiles().observe(getViewLifecycleOwner(), new Observer<List<ReadableFile>>() {
//            @Override
//            public void onChanged(List<ReadableFile> readableFiles) {
//                if (readableFiles != null) {
//                    recentFileDetails = (ArrayList<ReadableFile>) readableFiles;
//                    if (Objects.equals(currentListType, "RECENT")) {
//                        adapter.setReadableFileDetails(recentFileDetails);
//                    }
//                }
//            }
//        });

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
        return currentListType;
    }
}
