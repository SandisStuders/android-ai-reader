package com.example.readerapp.data.models.aiResponse;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class GptResponseViewModel extends AndroidViewModel {

    private AiResponseRepository mRepository;

    private final LiveData<List<AiResponse>> mAllGptResponses;

    public GptResponseViewModel (Application application) {
        super(application);
        mRepository = new AiResponseRepository(application);
        mAllGptResponses = mRepository.getAllGptResponses();
    }

    public LiveData<List<AiResponse>> getAllGptResponses() { return mAllGptResponses; }

    public void insert(AiResponse aiResponse) { mRepository.insert(aiResponse); }

    public void insert(ArrayList<AiResponse> aiRespons) { mRepository.insert(aiRespons); }

    public void update(AiResponse aiResponse) {
        mRepository.update(aiResponse);
    }

    public void delete(AiResponse aiResponse) { mRepository.delete(aiResponse); }

}
