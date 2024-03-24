package com.example.readerapp.data.models;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class ReadableFileViewModel extends AndroidViewModel {

    private ReadableFileRepository mRepository;

    private final LiveData<List<ReadableFile>> mAllReadableFiles;

    public ReadableFileViewModel (Application application) {
        super(application);
        mRepository = new ReadableFileRepository(application);
        mAllReadableFiles = mRepository.getAllReadableFiles();
    }

    public LiveData<List<ReadableFile>> getAllReadableFiles() { return mAllReadableFiles; }

    public void insert(ArrayList<ReadableFile> readableFile) { mRepository.insert(readableFile); }

}
