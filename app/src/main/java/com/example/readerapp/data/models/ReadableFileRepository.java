package com.example.readerapp.data.models;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class ReadableFileRepository {

    private ReadableFileDao mReadableFileDao;
    private LiveData<List<ReadableFile>> mAllReadableFiles;

    ReadableFileRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mReadableFileDao = db.readableFileDao();
        mAllReadableFiles = mReadableFileDao.getAllFiles();
    }

    LiveData<List<ReadableFile>> getAllReadableFiles() {
        return mAllReadableFiles;
    }

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

}
