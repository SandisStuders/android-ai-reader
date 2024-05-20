package com.example.readerapp.data.repositories;

import android.app.Application;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.example.readerapp.data.dataSources.ExternalStorageDataSource;
import com.example.readerapp.data.models.ReadableFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

public class ExternalStorageRepository {

    private final Set<FileType> readableFileTypes = new HashSet<>(Arrays.asList(
            new FileType("EPUB", "application/epub+zip")
    ));

    private final ExternalStorageDataSource externalStorageDataSource;
    private final Application application;

    public ExternalStorageRepository(Application application) {
        this.application = application;
        this.externalStorageDataSource = new ExternalStorageDataSource(application.getContentResolver());
    }

    public ArrayList<ReadableFile> retrieveReadableFiles() {
        ArrayList<ReadableFile> readableFiles = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            for (FileType fileType : readableFileTypes) {
                String fileExtension = fileType.extension;
                String mimeType = fileType.mimeType;

                String[] projection = new String[]{
                        MediaStore.Files.FileColumns._ID,
                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                        MediaStore.Files.FileColumns.DATE_ADDED,
                        MediaStore.Files.FileColumns.SIZE,
                        MediaStore.Files.FileColumns.RELATIVE_PATH,
                        MediaStore.Files.FileColumns.MIME_TYPE
                };
                String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
                ArrayList<ReadableFile> fileTypeReadableFiles = externalStorageDataSource.retrieveReadableFilesByMimeType(projection, mimeType, sortOrder);

                for (ReadableFile readableFile : fileTypeReadableFiles) {
                    readableFile.setFileType(fileExtension);
                }
                readableFiles.addAll(fileTypeReadableFiles);
            }

        } else {
            for (FileType fileType : readableFileTypes) {
                String fileExtension = fileType.extension;
                File contentPath = Environment.getExternalStorageDirectory();
                ArrayList<ReadableFile> fileTypeReadableFiles = externalStorageDataSource.retrieveReadableFilesByExtension(contentPath, fileExtension);
                readableFiles.addAll(fileTypeReadableFiles);
            }
        }

        return readableFiles;
    }

    public boolean fileExists(ReadableFile readableFile) {
        Uri uri = Uri.parse(readableFile.getContentUri());
        return externalStorageDataSource.fileExists(uri);
    }

    public Book getEpubWithUriString(String uriString) {
        Uri uri = Uri.parse(uriString);
        return externalStorageDataSource.getEpubWithUri(uri);
    }

    public String getEpubResourceContent(Resource resource) {
        return externalStorageDataSource.getEpubResourceContent(resource);
    }

    private static class FileType {
        public String extension;
        public String mimeType;

        FileType(String extension, String mimeType) {
            this.extension = extension;
            this.mimeType = mimeType;
        }
    }

}
