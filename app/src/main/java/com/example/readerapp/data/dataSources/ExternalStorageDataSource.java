package com.example.readerapp.data.dataSources;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.readerapp.data.models.ReadableFile;
import com.example.readerapp.utils.HelperFunctions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

public class ExternalStorageDataSource {

    private final ContentResolver contentResolver;

    public ExternalStorageDataSource(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public ArrayList<ReadableFile> retrieveReadableFilesByMimeType(String[] projection, String mimeType, String sortOrder) {
        ArrayList<ReadableFile> readableFiles = new ArrayList<>();

        // Query URI for external files content
        Uri queryUri = MediaStore.Files.getContentUri("external");

        // Filter for MIME type
        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?";
        String[] selectionArgs = new String[]{mimeType};

        try (Cursor cursor = contentResolver.query(queryUri, projection, selection, selectionArgs, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
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

    public Book getEpubWithUri(Uri uri) {
        try {
            InputStream fileStream = contentResolver.openInputStream(uri);
            return  (new EpubReader()).readEpub(fileStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getEpubResourceContent(Resource resource) {
        StringBuilder contentBuilder = new StringBuilder();
        try {
            InputStreamReader contentReader = new InputStreamReader(resource.getInputStream());
            BufferedReader r = new BufferedReader(contentReader);
            String aux;
            while ((aux = r.readLine()) != null) {
                contentBuilder.append(aux);
                contentBuilder.append('\n');
            }
            return contentBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
