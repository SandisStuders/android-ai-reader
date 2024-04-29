package com.example.readerapp.ui.viewModels;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.models.readableFile.ReadableFileRepository;
import com.example.readerapp.data.repositories.ExternalFileRepository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FileViewerViewModel extends AndroidViewModel {

    private final ReadableFileRepository mRepository;
    private final ExternalFileRepository externalFileRepository;
    private LiveData<List<ReadableFile>> mAllReadableFiles;
    private LiveData<List<ReadableFile>> mFavoriteFiles;
    private LiveData<List<ReadableFile>> mRecentFiles;
    private final Context context;

    private LiveData<List<ReadableFile>> activeList;
    private Observer<List<ReadableFile>> activeObserver;
    private String activeListType;

    public FileViewerViewModel(Application application) {
        super(application);
        mRepository = new ReadableFileRepository(application);
        externalFileRepository = new ExternalFileRepository(application);
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
        ArrayList<ReadableFile> storageFiles = externalFileRepository.retrieveReadableFilesByMimeType("application/epub+zip", "EPUB");

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
