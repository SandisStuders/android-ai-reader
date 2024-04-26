package com.example.readerapp.data.models.readableFile;

import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.models.readableFile.ReadableFileRepository;
import com.example.readerapp.utils.HelperFunctions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ReadableFileViewModel extends AndroidViewModel {

    private ReadableFileRepository mRepository;

    private final LiveData<List<ReadableFile>> mAllReadableFiles;
    private final LiveData<List<ReadableFile>> mFavoriteFiles;
    private final LiveData<List<ReadableFile>> mRecentFiles;
    private Context context;

    public ReadableFileViewModel (Application application) {
        super(application);
        mRepository = new ReadableFileRepository(application);
        mAllReadableFiles = mRepository.getAllReadableFiles();
        mFavoriteFiles = mRepository.getFavoriteFiles();
        mRecentFiles = mRepository.getRecentFiles();
        context = application.getApplicationContext();
    }

    public LiveData<List<ReadableFile>> getAllReadableFiles() { return mAllReadableFiles; }

    public LiveData<List<ReadableFile>> getFavoriteFiles() { return mFavoriteFiles; }

    public LiveData<List<ReadableFile>> getRecentFiles() { return mRecentFiles; }
    public LiveData<ReadableFile> getReadableFileByPrimaryKey(String fileName, String relativePath) {
        return mRepository.getReadableFileByPrimaryKey(fileName, relativePath);
    }

    public void insert(ReadableFile readableFile) { mRepository.insert(readableFile); }

    public void insert(ArrayList<ReadableFile> readableFiles) { mRepository.insert(readableFiles); }

    public void update(ReadableFile readableFile) {
        mRepository.update(readableFile);
    }

    public void deleteFiles(ArrayList<ReadableFile> readableFiles) {
        mRepository.deleteFiles(readableFiles);
    }

    public ArrayList<ReadableFile> getEpubFileList() {
        ArrayList<ReadableFile> epubFiles = new ArrayList<>();

        String[] projection = new String[]{
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.RELATIVE_PATH,
                MediaStore.Files.FileColumns.MIME_TYPE
        };

        // Filter for EPUB MIME type
        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?";
        String[] selectionArgs = new String[]{"application/epub+zip"};

        // Query URI for external files content
        Uri queryUri = MediaStore.Files.getContentUri("external");

        try (Cursor cursor = context.getContentResolver().query(queryUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
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
                            "EPUB",
                            relativePath,
                            0,
                            false);
                    epubFiles.add(fileDetails);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return epubFiles;
    }

    public boolean fileExists(Uri uri) {
        boolean exists = false;
        if(null != uri) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                inputStream.close();
                exists = true;
            } catch (Exception e) {
                Log.d("MyLogs", "File corresponding to the uri does not exist " + uri.toString());
            }
        }
        return exists;
    }

}
