package com.example.readerapp.data.repositories;

import android.app.Application;

import com.example.readerapp.data.dataSources.InternalStorageDataSource;

public class InternalStorageRepository {

    Application application;
    InternalStorageDataSource internalStorageDataSource;

    public InternalStorageRepository(Application application) {
        this.application = application;
        this.internalStorageDataSource = new InternalStorageDataSource(application);
    }

    public boolean directoryExistsInCache(String directoryRelativePath) {
        return internalStorageDataSource.directoryExistsInCache(directoryRelativePath);
    }

    public String getCacheDirectoryPath() {
        return internalStorageDataSource.getCacheDirectoryPath();
    }

    public String getCacheDirectoryPath(String directoryRelativePath) {
        return internalStorageDataSource.getCacheDirectoryPath(directoryRelativePath);
    }

    public void emptyCache() {
        internalStorageDataSource.emptyCache();
    }

    public void writeByteArrayToCache(byte[] resource, String resourceRelativePath) {
        internalStorageDataSource.writeByteArrayToCache(resource, resourceRelativePath);
    }

}
