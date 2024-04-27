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
import androidx.lifecycle.Observer;

import com.example.readerapp.utils.HelperFunctions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ReadableFileViewModel extends AndroidViewModel {

    private final ReadableFileRepository mRepository;
    private LiveData<List<ReadableFile>> mAllReadableFiles;
    private LiveData<List<ReadableFile>> mFavoriteFiles;
    private LiveData<List<ReadableFile>> mRecentFiles;
    private final Context context;

    private LiveData<List<ReadableFile>> activeList;
    private Observer<List<ReadableFile>> activeObserver;
    private String activeListType;

    public ReadableFileViewModel (Application application) {
        super(application);
        mRepository = new ReadableFileRepository(application);
        context = application.getApplicationContext();
    }

    public void loadData() {
        mAllReadableFiles = mRepository.getAllReadableFiles();
        mFavoriteFiles = mRepository.getFavoriteFiles();
        mRecentFiles = mRepository.getRecentFiles();
    }

    public void initialize(String currentListType, Observer<List<ReadableFile>> newObserver) {
        loadData();
        changeListType(currentListType, newObserver);
        refreshDatabaseWithStorageData();
    }

    public void changeListType(String newListType, Observer<List<ReadableFile>> newObserver) {
        if (activeList != null && activeObserver != null) {
            activeList.removeObserver(activeObserver);
        }

        if (Objects.equals(newListType, "RECENT")) {
            activeList = mRecentFiles;
        } else if (Objects.equals(newListType, "FAVORITE")) {
            activeList = mFavoriteFiles;
        } else if (Objects.equals(newListType, "ALL")) {
            activeList = mAllReadableFiles;
        }

        activeObserver = newObserver;
        if (activeList != null) {
            activeList.observeForever(activeObserver);
        }
        activeListType = newListType;
    }

    public void addToFavorites(ReadableFile readableFile) {
        readableFile.setFavorite(true);
        update(readableFile);
    }

    public void removeFromFavorites(ReadableFile readableFile) {
        readableFile.setFavorite(false);
        update(readableFile);
    }

    public void removeRecentTime(ReadableFile readableFile) {
        readableFile.setMostRecentAccessTime(0);
        update(readableFile);
    }

    public boolean prepareFileOpen(ReadableFile readableFile) {
        if (!fileExists(readableFile)) {
            return false;
        }

        long currentTimeMillis = System.currentTimeMillis();
        readableFile.setMostRecentAccessTime(currentTimeMillis);
        update(readableFile);

        return true;
    }

    private void refreshDatabaseWithStorageData() {
        ArrayList<ReadableFile> storageFiles = getEpubFileList();

        ArrayList<ReadableFile> filesToDelete = findFilesToDelete(storageFiles);
        deleteFiles(filesToDelete);

        insert(storageFiles);
    }

    public LiveData<ReadableFile> getReadableFileByPrimaryKey(String fileName, String relativePath) {
        return mRepository.getReadableFileByPrimaryKey(fileName, relativePath);
    }

    public void insert(ReadableFile readableFile) { mRepository.insert(readableFile); }

    public void insert(ArrayList<ReadableFile> readableFiles) { mRepository.insert(readableFiles); }

    public void update(ReadableFile readableFile) {
        mRepository.update(readableFile);
        Log.d("MyPrompts", "ViewModel: Update: Repository Updated");
    }

    public void deleteFiles(ArrayList<ReadableFile> readableFiles) {
        mRepository.deleteFiles(readableFiles);
    }

    private ArrayList<ReadableFile> getEpubFileList() {
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

    private ArrayList<ReadableFile> findFilesToDelete(ArrayList<ReadableFile> storageFiles) {
        ArrayList<ReadableFile> databaseFiles = new ArrayList<>();
        if (mAllReadableFiles != null) {
            databaseFiles = (ArrayList<ReadableFile>) mAllReadableFiles.getValue();
        }
        if (databaseFiles == null) {
            return new ArrayList<>();
        }
        if (storageFiles == null) {
            return databaseFiles;
        }

        Set<String> deviceFilePaths = new HashSet<>();
        for (ReadableFile file : storageFiles) {
            String fileFullPath = file.getRelativePath() + file.getFileName();
            deviceFilePaths.add(fileFullPath);
        }

        ArrayList<ReadableFile> filesToDelete = new ArrayList<>();
        for (ReadableFile dbFile : databaseFiles) {
            String fileFullPath = dbFile.getRelativePath() + dbFile.getFileName();
            if (!deviceFilePaths.contains(fileFullPath)) {
                filesToDelete.add(dbFile);
            }
        }

        return filesToDelete;
    }

    private boolean fileExists(ReadableFile readableFile) {
        Uri uri = Uri.parse(readableFile.getContentUri());
        boolean exists = false;
        if(uri != null) {
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

    public String getActiveListType() {
        return activeListType;
    }

    private void setActiveListType(String activeListType) {
        this.activeListType = activeListType;
    }
}
