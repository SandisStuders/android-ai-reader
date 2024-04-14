package com.example.readerapp.data.models.readableFile;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.models.readableFile.ReadableFileRepository;

import java.util.ArrayList;
import java.util.List;

public class ReadableFileViewModel extends AndroidViewModel {

    private ReadableFileRepository mRepository;

    private final LiveData<List<ReadableFile>> mAllReadableFiles;
    private final LiveData<List<ReadableFile>> mFavoriteFiles;
    private final LiveData<List<ReadableFile>> mRecentFiles;

    public ReadableFileViewModel (Application application) {
        super(application);
        mRepository = new ReadableFileRepository(application);
        mAllReadableFiles = mRepository.getAllReadableFiles();
        mFavoriteFiles = mRepository.getFavoriteFiles();
        mRecentFiles = mRepository.getRecentFiles();
    }

    public LiveData<List<ReadableFile>> getAllReadableFiles() { return mAllReadableFiles; }

    public LiveData<List<ReadableFile>> getFavoriteFiles() { return mFavoriteFiles; }

    public LiveData<List<ReadableFile>> getRecentFiles() { return mRecentFiles; }

    public void insert(ReadableFile readableFile) { mRepository.insert(readableFile); }

    public void insert(ArrayList<ReadableFile> readableFiles) { mRepository.insert(readableFiles); }

    public void update(ReadableFile readableFile) {
        mRepository.update(readableFile);
    }

    public void deleteFiles(ArrayList<ReadableFile> readableFiles) {
        mRepository.deleteFiles(readableFiles);
    }

}
