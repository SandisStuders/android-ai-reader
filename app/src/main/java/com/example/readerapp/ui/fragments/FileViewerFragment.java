package com.example.readerapp.ui.fragments;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.readerapp.data.models.ReadableFile;
import com.example.readerapp.data.models.ReadableFileViewModel;
import com.example.readerapp.databinding.FragmentFileViewerBinding;
import com.example.readerapp.ui.adapters.FilesRecyclerViewAdapter;
import com.example.readerapp.utils.HelperFunctions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileViewerFragment extends Fragment implements FilesRecyclerViewAdapter.FileOptionListener {

    private FragmentFileViewerBinding binding;

    private RecyclerView filesRecyclerView;
    private BottomNavigationView bottomFileListSelectionBar;
    private ArrayList<ReadableFile> favoriteFileDetails = new ArrayList<>();
    private ArrayList<ReadableFile> allFileDetails = new ArrayList<>();
    private ArrayList<ReadableFile> recentFileDetails = new ArrayList<>();

    private ReadableFileViewModel mReadableFileViewModel;
    private String currentListType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFileViewerBinding.inflate(inflater, container, false);

        filesRecyclerView = binding.filesRecyclerView;
        bottomFileListSelectionBar = binding.bottomFileSelectionBar;

        mReadableFileViewModel = new ViewModelProvider(this).get(ReadableFileViewModel.class);

        ArrayList<ReadableFile> fileDetails = getPdfFileList();

        mReadableFileViewModel.insert(fileDetails);

        FilesRecyclerViewAdapter adapter = new FilesRecyclerViewAdapter(this);
        currentListType = "RECENT";
        adapter.setReadableFileDetails(recentFileDetails);
        adapter.setCurrentListType(currentListType);

        filesRecyclerView.setAdapter(adapter);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

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
                    allFileDetails = (ArrayList<ReadableFile>) readableFiles;
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

    private ArrayList<ReadableFile> getPdfFileList() {
        ArrayList<ReadableFile> pdfFiles = new ArrayList<>();

        String[] projection = new String[]{
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.RELATIVE_PATH,
                MediaStore.Files.FileColumns.MIME_TYPE
        };

        // Filter for PDF MIME type
        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?";
//        String[] selectionArgs = new String[]{"application/pdf"};
        String[] selectionArgs = new String[]{"application/epub+zip"};

        // Query URI for external files content
        Uri queryUri = MediaStore.Files.getContentUri("external");

        try (Cursor cursor = getContext().getContentResolver().query(queryUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                Log.d("MyLogs", "CURSOR NOT NULL");
                String[] colNames = cursor.getColumnNames();

                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                int creationDateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                int relativePathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH);

                do {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id);
                    String timestamp = cursor.getString(creationDateColumn);
                    String creationDate = HelperFunctions.timestampToDate(timestamp, "dd-MM-yyyy HH:mm");
                    String size = cursor.getString(sizeColumn);
                    String adjustedFileSize = HelperFunctions.adjustByteSizeString(size);
                    String relativePath = cursor.getString(relativePathColumn);

                    ReadableFile fileDetails = new ReadableFile(name,
                            contentUri.toString(),
                            creationDate,
                            adjustedFileSize,
//                            "PDF",
                            "EPUB",
                            relativePath,
                            0,
                            false);
                    pdfFiles.add(fileDetails);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pdfFiles;
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
    public void fileOpened(ReadableFile readableFile) {
        long currentTimeMillis = System.currentTimeMillis();
        readableFile.setMostRecentAccessTime(currentTimeMillis);
        mReadableFileViewModel.update(readableFile);
    }
}
