package com.example.readerapp.data.models;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class ReadableFileRepository {

    private ReadableFileDao mReadableFileDao;
    private LiveData<List<ReadableFile>> mAllReadableFiles;
    private LiveData<List<ReadableFile>> mFavoriteFiles;
    private LiveData<List<ReadableFile>> mRecentFiles;

    ReadableFileRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mReadableFileDao = db.readableFileDao();
        mAllReadableFiles = mReadableFileDao.getAllFiles();
        mFavoriteFiles = mReadableFileDao.getFavoriteFiles();
        mRecentFiles = mReadableFileDao.getRecentFiles();
    }

    LiveData<List<ReadableFile>> getAllReadableFiles() {
        return mAllReadableFiles;
    }

    public LiveData<List<ReadableFile>> getFavoriteFiles() {return mFavoriteFiles;}

    public LiveData<List<ReadableFile>> getRecentFiles() {return mRecentFiles;}

    void insert(ReadableFile readableFile) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mReadableFileDao.insertFiles(readableFile);
        });
    }

    void insert(ArrayList<ReadableFile> readableFiles) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mReadableFileDao.insertFiles(readableFiles);
        });
    }

    public void update(ReadableFile readableFile) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mReadableFileDao.updateFile(readableFile);
        });
    }

}
