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

    private ArrayList<ReadableFile> favoriteFileDetails = new ArrayList<>();
    private ArrayList<ReadableFile> allFileDetails = new ArrayList<>();
    private ArrayList<ReadableFile> recentFileDetails = new ArrayList<>();
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

        // to the viewmodel
        ArrayList<ReadableFile> fileDetails = mReadableFileViewModel.getEpubFileList();
        mReadableFileViewModel.insert(fileDetails);

        if (savedInstanceState == null) {
            currentListType = "RECENT";
        } else {
            currentListType = savedInstanceState.getString("CURRENT_LIST_TYPE", "RECENT");
        }


        adapter.setCurrentListType(currentListType);
        if (Objects.equals(currentListType, "RECENT")) {
            adapter.setReadableFileDetails(recentFileDetails);
        } else if (Objects.equals(currentListType, "FAVORITE")) {
            adapter.setReadableFileDetails(favoriteFileDetails);
        } else if (Objects.equals(currentListType, "ALL")) {
            adapter.setReadableFileDetails(allFileDetails);
        }
        // to the view model

        bottomFileListSelectionBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.recentFiles) {
                    currentListType = "RECENT";
                    adapter.setCurrentListType(currentListType);
                    adapter.setReadableFileDetails(recentFileDetails);
                    return true;
                } else if (itemId == R.id.favoriteFiles) {
                    currentListType = "FAVORITE";
                    adapter.setCurrentListType(currentListType);
                    adapter.setReadableFileDetails(favoriteFileDetails);
                    return true;
                } else if (itemId == R.id.allFiles) {
                    currentListType = "ALL";
                    adapter.setCurrentListType(currentListType);
                    adapter.setReadableFileDetails(allFileDetails);
                    return true;
                }

                return false;
            }
        });

        mReadableFileViewModel.getFavoriteFiles().observe(getViewLifecycleOwner(), new Observer<List<ReadableFile>>() {
            @Override
            public void onChanged(List<ReadableFile> readableFiles) {
                if (readableFiles != null) {
                    favoriteFileDetails = (ArrayList<ReadableFile>) readableFiles;
                    if (Objects.equals(currentListType, "FAVORITE")) {
                        adapter.setReadableFileDetails(favoriteFileDetails);
                    }
                }
            }
        });

        mReadableFileViewModel.getAllReadableFiles().observe(getViewLifecycleOwner(), new Observer<List<ReadableFile>>() {
            @Override
            public void onChanged(List<ReadableFile> readableFiles) {
                if (readableFiles != null) {
                    ArrayList<ReadableFile> filesOnDevice = mReadableFileViewModel.getEpubFileList();
                    allFileDetails = (ArrayList<ReadableFile>) readableFiles;

                    Set<String> deviceFilePaths = new HashSet<>();
                    for (ReadableFile file : filesOnDevice) {
                        String fileFullPath = file.getRelativePath() + file.getFileName();
                        deviceFilePaths.add(fileFullPath);
                    }

                    ArrayList<ReadableFile> filesToDelete = new ArrayList<>();
                    for (ReadableFile dbFile : readableFiles) {
                        String fileFullPath = dbFile.getRelativePath() + dbFile.getFileName();
                        if (!deviceFilePaths.contains(fileFullPath)) {
                            filesToDelete.add(dbFile);
                        }
                    }

                    if (!filesToDelete.isEmpty()) {
                        mReadableFileViewModel.deleteFiles(filesToDelete);
                    }

                    if (Objects.equals(currentListType, "ALL")) {
                        adapter.setReadableFileDetails(allFileDetails);
                    }
                }
            }
        });

        mReadableFileViewModel.getRecentFiles().observe(getViewLifecycleOwner(), new Observer<List<ReadableFile>>() {
            @Override
            public void onChanged(List<ReadableFile> readableFiles) {
                if (readableFiles != null) {
                    recentFileDetails = (ArrayList<ReadableFile>) readableFiles;
                    if (Objects.equals(currentListType, "RECENT")) {
                        adapter.setReadableFileDetails(recentFileDetails);
                    }
                }
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
        outState.putString("CURRENT_LIST_TYPE", currentListType);
    }

    @Override
    public void addToFavorites(ReadableFile readableFile) {
        readableFile.setFavorite(true);
        mReadableFileViewModel.update(readableFile);
    }

    @Override
    public void removeFromFavorites(ReadableFile readableFile) {
        readableFile.setFavorite(false);
        mReadableFileViewModel.update(readableFile);
    }

    @Override
    public void removeFromRecent(ReadableFile readableFile) {
        readableFile.setMostRecentAccessTime(0);
        mReadableFileViewModel.update(readableFile);
    }

    @Override
    public void fileOpened(ReadableFile readableFile, View v) {
        Uri contentUri = Uri.parse(readableFile.getContentUri());
        if (!mReadableFileViewModel.fileExists(contentUri)) {
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        readableFile.setMostRecentAccessTime(currentTimeMillis);
        mReadableFileViewModel.update(readableFile);

        Context context = v.getContext();
        String uriString = readableFile.getContentUri();
        String fileName = readableFile.getFileName();
        String fileRelativePath = readableFile.getRelativePath();

        Intent intent = new Intent(context, EpubViewerActivity.class);
        intent.putExtra("URI_STRING", uriString);
        intent.putExtra("FILE_NAME", fileName);
        intent.putExtra("FILE_RELATIVE_PATH", fileRelativePath);
        context.startActivity(intent);
    }
}
