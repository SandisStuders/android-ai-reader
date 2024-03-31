package com.example.readerapp.data.models.readableFile;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.readerapp.data.models.readableFile.ReadableFile;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ReadableFileDao {

    @Query("SELECT * FROM readableFiles")
    LiveData<List<ReadableFile>> getAllFiles();

    @Query("SELECT * FROM readableFiles WHERE isFavorite = 1")
    LiveData<List<ReadableFile>> getFavoriteFiles();

    @Query("SELECT * FROM readableFiles WHERE mostRecentAccessTime > 0 ORDER BY mostRecentAccessTime DESC LIMIT 15")
    LiveData<List<ReadableFile>> getRecentFiles();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertFiles(ReadableFile... readableFiles);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertFiles(ArrayList<ReadableFile> readableFiles);

    @Update
    void updateFile(ReadableFile... readableFiles);

    @Delete
    void deleteFile(ReadableFile readableFile);

}
