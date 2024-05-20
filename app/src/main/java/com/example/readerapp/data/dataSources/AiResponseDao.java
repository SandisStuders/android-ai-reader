package com.example.readerapp.data.dataSources;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.readerapp.data.models.AiResponse;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface AiResponseDao {

    @Query("SELECT * FROM aiResponses ORDER BY id DESC")
    LiveData<List<AiResponse>> getAllGptResponses();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGptResponses(AiResponse... aiResponse);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGptResponses(ArrayList<AiResponse> aiResponses);

    @Update
    void updateGptResponse(AiResponse... aiResponse);

    @Delete
    void deleteGptResponse(AiResponse aiResponse);

}
