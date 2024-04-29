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

    private final ReadableFileRepository mReadableFileRepository;
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
        mReadableFileRepository = new ReadableFileRepository(application);
        externalFileRepository = new ExternalFileRepository(application);
        context = application.getApplicationContext();
    }

    public void initialize(String currentListType, Observer<List<ReadableFile>> newObserver) {
        loadData();
        changeListType(currentListType, newObserver);
        refreshDatabaseWithStorageData();
    }

    public void loadData() {
        mAllReadableFiles = mReadableFileRepository.getAllReadableFiles();
        mFavoriteFiles = mReadableFileRepository.getFavoriteFiles();
        mRecentFiles = mReadableFileRepository.getRecentFiles();
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

    private void refreshDatabaseWithStorageData() {
        ArrayList<ReadableFile> storageFiles = externalFileRepository.retrieveReadableFilesByMimeType("application/epub+zip", "EPUB");

        mReadableFileRepository.deleteFilesThatDoNotExist(storageFiles);

        insert(storageFiles);
    }

    public void addToFavorites(ReadableFile readableFile) {
        mReadableFileRepository.addToFavorites(readableFile);
    }

    public void removeFromFavorites(ReadableFile readableFile) {
        mReadableFileRepository.removeFromFavorites(readableFile);
    }

    public void removeRecentTime(ReadableFile readableFile) {
        mReadableFileRepository.removeRecentTime(readableFile);
    }

    public boolean prepareFileOpen(ReadableFile readableFile) {
        if (!externalFileRepository.fileExists(readableFile)) {
            return false;
        }

        mReadableFileRepository.setRecentTimeToNow(readableFile);
        return true;
    }

    public LiveData<ReadableFile> getReadableFileByPrimaryKey(String fileName, String relativePath) {
        return mReadableFileRepository.getReadableFileByPrimaryKey(fileName, relativePath);
    }

    public void insert(ReadableFile readableFile) { mReadableFileRepository.insert(readableFile); }

    public void insert(ArrayList<ReadableFile> readableFiles) { mReadableFileRepository.insert(readableFiles); }

    public void update(ReadableFile readableFile) {
        mReadableFileRepository.update(readableFile);
    }

    public String getActiveListType() {
        return activeListType;
    }

    private void setActiveListType(String activeListType) {
        this.activeListType = activeListType;
    }
}
