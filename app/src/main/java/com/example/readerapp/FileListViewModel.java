package com.example.readerapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FileListViewModel extends ViewModel {

    private final MutableLiveData<String> currentListType = new MutableLiveData<>();

    public void setCurrentListType(String listType) {
        currentListType.setValue(listType);
    }

    public LiveData<String> getCurrentListType() {
        return currentListType;
    }

}
