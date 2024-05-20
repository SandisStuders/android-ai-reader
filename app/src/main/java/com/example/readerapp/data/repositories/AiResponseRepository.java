package com.example.readerapp.data.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.readerapp.data.AppDatabase;
import com.example.readerapp.data.models.AiResponse;
import com.example.readerapp.data.dataSources.AiResponseDao;

import java.util.ArrayList;
import java.util.List;

public class AiResponseRepository {

    private AiResponseDao mAiResponseDao;
    private LiveData<List<AiResponse>> mAllGptResponses;

    public AiResponseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mAiResponseDao = db.gptResponseDao();
        mAllGptResponses = mAiResponseDao.getAllGptResponses();
    }

    public LiveData<List<AiResponse>> getAllGptResponses() {
        return mAllGptResponses;
    }

    public void insert(AiResponse aiResponse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mAiResponseDao.insertGptResponses(aiResponse);
        });
    }

    public void insert(ArrayList<AiResponse> aiRespons) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mAiResponseDao.insertGptResponses(aiRespons);
        });
    }

    public void update(AiResponse aiResponse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mAiResponseDao.updateGptResponse(aiResponse);
        });
    }

    public void delete(AiResponse aiResponse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mAiResponseDao.deleteGptResponse(aiResponse);
        });
    }

}
