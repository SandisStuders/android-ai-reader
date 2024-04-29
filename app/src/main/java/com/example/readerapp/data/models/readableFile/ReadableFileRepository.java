package com.example.readerapp.data.models.readableFile;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.readerapp.data.models.AppDatabase;
import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.models.readableFile.ReadableFileDao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReadableFileRepository {

    private final ReadableFileDao mReadableFileDao;
    private final LiveData<List<ReadableFile>> mAllReadableFiles;
    private final LiveData<List<ReadableFile>> mFavoriteFiles;
    private final LiveData<List<ReadableFile>> mRecentFiles;

    public ReadableFileRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mReadableFileDao = db.readableFileDao();
        mAllReadableFiles = mReadableFileDao.getAllFiles();
        mFavoriteFiles = mReadableFileDao.getFavoriteFiles();
        mRecentFiles = mReadableFileDao.getRecentFiles();
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

    public void setRecentTimeToNow(ReadableFile readableFile) {
        long currentTimeMillis = System.currentTimeMillis();
        readableFile.setMostRecentAccessTime(currentTimeMillis);
        update(readableFile);
    }

    public void deleteFilesThatDoNotExist(ArrayList<ReadableFile> storageFiles) {
        if (storageFiles == null) {
            return;
        }

        ArrayList<ReadableFile> databaseFiles = new ArrayList<>();
        if (mAllReadableFiles != null) {
            databaseFiles = (ArrayList<ReadableFile>) mAllReadableFiles.getValue();
        }
        if (databaseFiles == null) {
            return;
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

        deleteFiles(filesToDelete);
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
