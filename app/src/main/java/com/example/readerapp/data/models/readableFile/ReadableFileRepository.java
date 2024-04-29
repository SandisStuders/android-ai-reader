package com.example.readerapp.data.models.readableFile;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.readerapp.data.models.AppDatabase;
import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.models.readableFile.ReadableFileDao;

import java.util.ArrayList;
import java.util.List;

public class ReadableFileRepository {

    private ReadableFileDao mReadableFileDao;
    private LiveData<List<ReadableFile>> mAllReadableFiles;
    private LiveData<List<ReadableFile>> mFavoriteFiles;
    private LiveData<List<ReadableFile>> mRecentFiles;

    public ReadableFileRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mReadableFileDao = db.readableFileDao();
        mAllReadableFiles = mReadableFileDao.getAllFiles();
        mFavoriteFiles = mReadableFileDao.getFavoriteFiles();
        mRecentFiles = mReadableFileDao.getRecentFiles();
    }

    public LiveData<List<ReadableFile>> getAllReadableFiles() {
        return mAllReadableFiles;
    }

    public LiveData<List<ReadableFile>> getFavoriteFiles() {return mFavoriteFiles;}

    public LiveData<List<ReadableFile>> getRecentFiles() {return mRecentFiles;}

    public LiveData<ReadableFile> getReadableFileByPrimaryKey(String fileName, String relativePath) {
        return mReadableFileDao.getReadableFileByPrimaryKey(fileName, relativePath);
    }

    public void insert(ReadableFile readableFile) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mReadableFileDao.insertFiles(readableFile);
        });
    }

    public void insert(ArrayList<ReadableFile> readableFiles) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mReadableFileDao.insertFiles(readableFiles);
        });
    }

    public void update(ReadableFile readableFile) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mReadableFileDao.updateFile(readableFile);
        });
    }

    public void deleteFiles(ArrayList<ReadableFile> readableFiles) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mReadableFileDao.deleteFiles(readableFiles);
        });
    }

}
