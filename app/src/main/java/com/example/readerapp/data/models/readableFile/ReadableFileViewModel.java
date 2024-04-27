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
import androidx.lifecycle.MutableLiveData;

import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.models.readableFile.ReadableFileRepository;
import com.example.readerapp.utils.HelperFunctions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ReadableFileViewModel extends AndroidViewModel {

    private ReadableFileRepository mRepository;

    private final LiveData<List<ReadableFile>> mAllReadableFiles;
    private final LiveData<List<ReadableFile>> mFavoriteFiles;
    private final LiveData<List<ReadableFile>> mRecentFiles;
    private Context context;
    private MutableLiveData<UiState> uiState =
            new MutableLiveData<UiState>(new UiState(null));

    public ReadableFileViewModel (Application application) {
        super(application);
        mRepository = new ReadableFileRepository(application);
        mAllReadableFiles = mRepository.getAllReadableFiles();
        mFavoriteFiles = mRepository.getFavoriteFiles();
        mRecentFiles = mRepository.getRecentFiles();
        context = application.getApplicationContext();
    }

    public void initialize(String currentListType) {
        refreshDatabaseWithStorageData();

        LiveData<List<ReadableFile>> initialReadableFiles = null;
        if (Objects.equals(currentListType, "RECENT")) {
            initialReadableFiles = mRecentFiles;
        } else if (Objects.equals(currentListType, "FAVORITE")) {
            initialReadableFiles = mFavoriteFiles;
        } else if (Objects.equals(currentListType, "ALL")) {
            initialReadableFiles = mAllReadableFiles;
        }
        setUiState(new UiState(initialReadableFiles));
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    private void setUiState(UiState uiState) {
        this.uiState = new MutableLiveData<>(uiState);
    }

    private void refreshDatabaseWithStorageData() {
        ArrayList<ReadableFile> storageFiles = getEpubFileList();
        ArrayList<ReadableFile> filesToDelete = findFilesToDelete(storageFiles);
        if (filesToDelete != null) {
            mRepository.deleteFiles(filesToDelete);
        }
        insert(storageFiles);
    }

    public void changeListType(String newListType) {
        if (Objects.equals(newListType, "RECENT")) {
            setUiState(new UiState(mRecentFiles));
        } else if (Objects.equals(newListType, "FAVORITE")) {
            setUiState(new UiState(mFavoriteFiles));
        } else if (Objects.equals(newListType, "ALL")) {
            setUiState(new UiState(mAllReadableFiles));
        }
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

    public ArrayList<ReadableFile> findFilesToDelete(ArrayList<ReadableFile> storageFiles) {
        ArrayList<ReadableFile> databaseFiles = (ArrayList<ReadableFile>) mAllReadableFiles.getValue();
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

    public boolean fileExists(ReadableFile readableFile) {
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

    public boolean prepareFileOpen(ReadableFile readableFile) {
        if (!fileExists(readableFile)) {
            return false;
        }

        long currentTimeMillis = System.currentTimeMillis();
        readableFile.setMostRecentAccessTime(currentTimeMillis);
        update(readableFile);

        return true;
    }

    public class UiState {
        private LiveData<List<ReadableFile>> currentReadableFileList;
        private String currentListType;

        public UiState(LiveData<List<ReadableFile>> currentReadableFileList) {
            this.currentReadableFileList = currentReadableFileList;
        }

        public LiveData<List<ReadableFile>> getCurrentReadableFileList() {
            return currentReadableFileList;
        }

        public void setCurrentReadableFileList(LiveData<List<ReadableFile>> readableFiles) {
            this.currentReadableFileList = readableFiles;
        }
    }

}
