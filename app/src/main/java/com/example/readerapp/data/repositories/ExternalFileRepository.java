package com.example.readerapp.data.repositories;

import android.app.Application;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.readerapp.data.dataSources.ExternalStorageDataSource;
import com.example.readerapp.data.models.readableFile.ReadableFile;

import java.io.InputStream;
import java.util.ArrayList;

public class ExternalFileRepository {

    private final ExternalStorageDataSource externalStorageDataSource;
    private final Application application;

    public ExternalFileRepository(Application application) {
        this.application = application;
        this.externalStorageDataSource = new ExternalStorageDataSource(application.getContentResolver());
    }

    public ArrayList<ReadableFile> retrieveReadableFilesByMimeType(String mimeType, String fileType) {
        String[] projection = new String[]{
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.RELATIVE_PATH,
                MediaStore.Files.FileColumns.MIME_TYPE
        };

        // Filter for MIME type
        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?";
        String[] selectionArgs = new String[]{mimeType};

        ArrayList<ReadableFile> readableFiles = externalStorageDataSource.retrieveReadableFilesByMimeType(projection, selection, selectionArgs);
        for (ReadableFile readableFile : readableFiles) {
            readableFile.setFileType(fileType);
        }

        return readableFiles;
    }

    public boolean fileExists(ReadableFile readableFile) {
        Uri uri = Uri.parse(readableFile.getContentUri());
        return externalStorageDataSource.fileExists(uri);
    }

}
