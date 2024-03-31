package com.example.readerapp.data.models.gptResponse;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class GptResponseViewModel extends AndroidViewModel {

    private GptResponseRepository mRepository;

    private final LiveData<List<GptResponse>> mAllGptResponses;

    public GptResponseViewModel (Application application) {
        super(application);
        mRepository = new GptResponseRepository(application);
        mAllGptResponses = mRepository.getAllGptResponses();
    }

    public LiveData<List<GptResponse>> getAllGptResponses() { return mAllGptResponses; }

    public void insert(GptResponse gptResponse) { mRepository.insert(gptResponse); }

    public void insert(ArrayList<GptResponse> gptResponses) { mRepository.insert(gptResponses); }

    public void update(GptResponse gptResponse) {
        mRepository.update(gptResponse);
    }

    public void delete(GptResponse gptResponse) { mRepository.delete(gptResponse); }

}
