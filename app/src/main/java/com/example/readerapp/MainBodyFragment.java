package com.example.readerapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.readerapp.databinding.FragmentMainBodyBinding;

import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;

public class MainBodyFragment extends Fragment {

    private FragmentMainBodyBinding binding;

    private final int PICK_FILE_REQUEST_CODE = 1;
    private ActivityResultLauncher<String> filePickerLauncher;

    Button filePickButton, fileCheckButton, pdfFileCheckButton, fileViewerStartButton;
    TextView filePickResult;
    Uri currentFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMainBodyBinding.inflate(inflater, container, false);
        currentFile = null;

        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                if (uri != null) {
                    Intent intent = new Intent(getContext(), ReaderActivity.class);
                    startActivity(intent);
                }
            }
        });

        filePickButton = binding.filePickButton;
        filePickButton.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View v) {
               filePickerLauncher.launch("application/pdf");
           }
        });

        filePickResult = binding.filePickResultTextview;

        fileCheckButton = binding.fileChecker;
        fileCheckButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            //TODO: What to do for older versions?
            @Override
            public void onClick(View v) {
                Log.d("MyLogs", "BUTTON PRESSED");

                // create intent that enables this shit
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);

                // verify that its enabled
                if (Environment.isExternalStorageManager()) {
                    Log.d("MyLogs", "EXTERNAL STORAGE ACCESS GRANTED");
                }

                contentResolverTest();
            }
        });

        pdfFileCheckButton = binding.pdfFileChecker;
        pdfFileCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MyLogs", "PDF BUTTON PRESSED");
                getPdfFileList();
            }
        });

        fileViewerStartButton = binding.fileViewerStart;
        fileViewerStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FileViewerActivity.class);
                startActivity(intent);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void contentResolverTest() {
        Log.d("MyLogs", "Content resolver test engaged");
        ContentResolver resolver = requireActivity().getApplicationContext().getContentResolver();
        if (resolver != null) {
            Log.d("MyLogs", "CONTENT RESOLVER IS NOT NULL");
        } else {
            Log.d("MyLogs", "CONTENT RESOLVER IS NULL");
        }

        Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = resolver.query(contentUri,
                null,
                null,
                null,
                null);

        if (cursor != null) {
            Log.d("MyLogs", "CURSOR IS NOT NULL");
        } else {
            Log.d("MyLogs", "CURSOR IS NULL");
        }

        int colCount = cursor.getColumnCount();
        int rowCount = cursor.getCount();

        Log.d("MyLogs", "CURSOR HAS FOLLOWING NO OF ROWS: " + rowCount);
        Log.d("MyLogs", "CURSOR HAS FOLLOWING NO OF COLUMNS: " + colCount);

    }

    private void listImages() {

        Log.d("MyLogs", "Image listing engaged");
        Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,   // Content URI for external image files
                new String[] {                                  // Projection: Columns to return
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DISPLAY_NAME
                },
                null,                                           // Selection: SQL WHERE clause
                null,                                           // Selection args: replaced ? in selection
                null                                            // Sort order
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Extract data from the cursor
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));

                Log.d("MyLogs", displayName);

                // Construct the content URI for the specific file
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                // Use the URI as needed
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private ArrayList<String> getPdfFileList() {
        ArrayList<String> pdfFiles = new ArrayList<>();

        String[] projection = new String[]{
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.MIME_TYPE
        };

        // Filter for PDF MIME type
        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?";
        String[] selectionArgs = new String[]{"application/pdf"};

        // Query URI for external files content
        Uri queryUri = MediaStore.Files.getContentUri("external");

        try (Cursor cursor = getContext().getContentResolver().query(queryUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);

                do {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    Log.d("MyLogs", name);

                    // Generate the URI
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id);
                    pdfFiles.add(contentUri.toString()); // Add the URI string to your list

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("MyLogs", "Files: " + pdfFiles.size());

        return null;
    }
}
