package com.example.readerapp.data.models.gptResponse;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface GptResponseDao {

    @Query("SELECT * FROM gptResponses")
    LiveData<List<GptResponse>> getAllGptResponses();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGptResponses(GptResponse... gptResponses);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGptResponses(ArrayList<GptResponse> gptResponses);

    @Update
    void updateGptResponse(GptResponse... gptResponses);

    @Delete
    void deleteGptResponse(GptResponse gptResponse);

}
