package com.example.readerapp.data.models;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class ReadableFileViewModel extends AndroidViewModel {

    private ReadableFileRepository mRepository;

    private final LiveData<List<ReadableFile>> mAllReadableFiles;
    private final LiveData<List<ReadableFile>> mFavoriteFiles;

    public ReadableFileViewModel (Application application) {
        super(application);
        mRepository = new ReadableFileRepository(application);
        mAllReadableFiles = mRepository.getAllReadableFiles();
        mFavoriteFiles = mRepository.getFavoriteFiles();
    }

    public LiveData<List<ReadableFile>> getAllReadableFiles() { return mAllReadableFiles; }

    public LiveData<List<ReadableFile>> getAllReadableFilesAlt() { return mRepository.getAllReadableFilesAlt(); }

    public void insert(ArrayList<ReadableFile> readableFile) { mRepository.insert(readableFile); }

    public LiveData<List<ReadableFile>> getFavoriteFiles() {return mFavoriteFiles;}

    public LiveData<List<ReadableFile>> getRecentFiles() {return null;}

    public void update(ReadableFile readableFile) {
        mRepository.update(readableFile);
    }

}
