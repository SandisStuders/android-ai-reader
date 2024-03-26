package com.example.readerapp.data.models;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class ReadableFileRepository {

    private ReadableFileDao mReadableFileDao;
    private LiveData<List<ReadableFile>> mAllReadableFiles;
    private LiveData<List<ReadableFile>> mFavoriteFiles;

    ReadableFileRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mReadableFileDao = db.readableFileDao();
        mAllReadableFiles = mReadableFileDao.getAllFiles();
        mFavoriteFiles = mReadableFileDao.getFavoriteFiles();
    }

    LiveData<List<ReadableFile>> getAllReadableFiles() {
        return mAllReadableFiles;
    }

    LiveData<List<ReadableFile>> getAllReadableFilesAlt() {
        return mReadableFileDao.getAllFiles();
    }

    public LiveData<List<ReadableFile>> getFavoriteFiles() {return mFavoriteFiles;}

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

    public LiveData<List<ReadableFile>> getRecentFiles() {return null;}

}
