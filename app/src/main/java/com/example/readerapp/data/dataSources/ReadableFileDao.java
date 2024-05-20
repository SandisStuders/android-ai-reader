package com.example.readerapp.data.dataSources;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.readerapp.data.models.ReadableFile;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ReadableFileDao {

    @Query("SELECT * FROM readableFiles")
    LiveData<List<ReadableFile>> getAllFiles();

    @Query("SELECT * FROM readableFiles WHERE isFavorite = 1")
    LiveData<List<ReadableFile>> getFavoriteFiles();

    @Query("SELECT * FROM readableFiles WHERE mostRecentAccessTime > 0 ORDER BY mostRecentAccessTime DESC LIMIT 50")
    LiveData<List<ReadableFile>> getRecentFiles();

    @Query("SELECT * FROM readableFiles WHERE fileName = :fileName AND relativePath = :relativePath")
    LiveData<ReadableFile> getReadableFileByPrimaryKey(String fileName, String relativePath);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertFiles(ReadableFile... readableFiles);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertFiles(ArrayList<ReadableFile> readableFiles);

    @Update
    void updateFile(ReadableFile... readableFiles);

    @Delete
    void deleteFile(ReadableFile readableFile);

    @Delete
    void deleteFiles(ArrayList<ReadableFile> readableFiles);

}
