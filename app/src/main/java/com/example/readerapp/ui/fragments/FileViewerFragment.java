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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readerapp.FileListViewModel;
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

public class FileViewerFragment extends Fragment {

    private FragmentFileViewerBinding binding;

    private RecyclerView filesRecyclerView;
    private BottomNavigationView bottomFileListSelectionBar;

    private FileListViewModel fileListViewModel;

    private ReadableFileViewModel mReadableFileViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFileViewerBinding.inflate(inflater, container, false);

        filesRecyclerView = binding.filesRecyclerView;
        bottomFileListSelectionBar = binding.bottomFileSelectionBar;
        fileListViewModel = new ViewModelProvider(this).get(FileListViewModel.class);
        mReadableFileViewModel = new ViewModelProvider(this).get(ReadableFileViewModel.class);

        ArrayList<ReadableFile> fileDetails = getPdfFileList();
        if (fileDetails == null) {
            Log.d("MyLogs", "Obtained fileDetails array is null");
        }
        Log.d("MyLogs", "FILE DETAILS SIZE: " + fileDetails.size());

        mReadableFileViewModel.insert(fileDetails);
        ArrayList<ReadableFile> dataReadableFiles = (ArrayList<ReadableFile>) mReadableFileViewModel.getAllReadableFiles().getValue();

        FilesRecyclerViewAdapter adapter = new FilesRecyclerViewAdapter();
        adapter.setReadableFileDetails(dataReadableFiles);

        mReadableFileViewModel.getAllReadableFiles().observe(getViewLifecycleOwner(), databaseReadableFiles -> {
            // Update the cached copy of the words in the adapter.
            adapter.setReadableFileDetails((ArrayList<ReadableFile>) databaseReadableFiles);
        });

        filesRecyclerView.setAdapter(adapter);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        bottomFileListSelectionBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.recentFiles) {
                    // recent files obtained from database
                    fileListViewModel.setCurrentListType("RECENT");
                    return true;
                } else if (itemId == R.id.favoriteFiles) {
                    // favorites files obtained from database
                    fileListViewModel.setCurrentListType("FAVORITES");
                    return true;
                } else if (itemId == R.id.allFiles) {
                    // all files obtained via getPdfFileList function
                    fileListViewModel.setCurrentListType("ALL");
                    return true;
                }

                return false;
            }
        });

        fileListViewModel.getCurrentListType().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String currentListType) {
                // Here you can notify your adapter about the list type change
                adapter.setCurrentListType(currentListType);
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
                            relativePath);
                    pdfFiles.add(fileDetails);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pdfFiles;
    }

}
