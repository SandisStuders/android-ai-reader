package com.example.readerapp.data.dataSources;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.utils.HelperFunctions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class ExternalStorageDataSource {

    private final ContentResolver contentResolver;

    public ExternalStorageDataSource(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public ArrayList<ReadableFile> retrieveReadableFilesByMimeType(String[] projection, String selection, String[] selectionArgs) {
        ArrayList<ReadableFile> readableFiles = new ArrayList<>();

        // Query URI for external files content
        Uri queryUri = MediaStore.Files.getContentUri("external");

        try (Cursor cursor = contentResolver.query(queryUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                Log.d("MyLogs", "Cursor is not null");
//                String[] colNames = cursor.getColumnNames();

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
                            "",
                            relativePath,
                            0,
                            false);
                    readableFiles.add(fileDetails);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return readableFiles;
    }

    public ArrayList<ReadableFile> retrieveReadableFilesByExtension(File baseDirectory, String fileExtension) {
        ArrayList<ReadableFile> readableFiles = new ArrayList<>();
        String extensionPattern = "." + fileExtension.toLowerCase();

        File[] listFile = baseDirectory.listFiles();

        if (listFile != null) {
            for (File file : listFile) {
                if (file.isDirectory()) {
                    ArrayList<ReadableFile> subdirectoryReadableFiles = retrieveReadableFilesByExtension(file, fileExtension);
                    readableFiles.addAll(subdirectoryReadableFiles);
                } else {
                    if (file.getName().toLowerCase().endsWith(extensionPattern)) {
                        String name = file.getName();
                        URI contentUri = file.toURI();
                        long size = file.length();
                        String adjustedFileSize = HelperFunctions.adjustByteSizeString(String.valueOf(size));
                        String creationDate = "";
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                            String creationTimestamp = String.valueOf(attrs.creationTime().toMillis()/1000);
                            creationDate = HelperFunctions.timestampToDate(creationTimestamp, "dd-MM-yyyy HH:mm");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String filePath = file.getPath().replace("/storage/emulated/0/", "")
                                .replace(name, "");

                        ReadableFile fileDetails = new ReadableFile(name,
                                contentUri.toString(),
                                creationDate,
                                adjustedFileSize,
                                fileExtension.toUpperCase(),
                                filePath,
                                0,
                                false);

                        readableFiles.add(fileDetails);

                    }
                }
            }
        }
        return readableFiles;
    }

    public boolean fileExists(Uri fileUri) {
        boolean exists = false;
        if(fileUri != null) {
            try {
                InputStream inputStream = contentResolver.openInputStream(fileUri);
                inputStream.close();
                exists = true;
            } catch (Exception e) {
                Log.d("MyLogs", "File corresponding to the uri does not exist " + fileUri.toString());
            }
        }
        return exists;
    }

}
