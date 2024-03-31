package com.example.readerapp.data.models.gptResponse;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.readerapp.data.models.AppDatabase;

import java.util.ArrayList;
import java.util.List;

public class GptResponseRepository {

    private GptResponseDao mGptResponseDao;
    private LiveData<List<GptResponse>> mAllGptResponses;

    GptResponseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mGptResponseDao = db.gptResponseDao();
        mAllGptResponses = mGptResponseDao.getAllGptResponses();
    }

    public LiveData<List<GptResponse>> getAllGptResponses() {
        return mAllGptResponses;
    }

    public void insert(GptResponse gptResponse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mGptResponseDao.insertGptResponses(gptResponse);
        });
    }

    public void insert(ArrayList<GptResponse> gptResponses) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mGptResponseDao.insertGptResponses(gptResponses);
        });
    }

    public void update(GptResponse gptResponse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mGptResponseDao.updateGptResponse(gptResponse);
        });
    }

    public void delete(GptResponse gptResponse) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mGptResponseDao.deleteGptResponse(gptResponse);
        });
    }

}
