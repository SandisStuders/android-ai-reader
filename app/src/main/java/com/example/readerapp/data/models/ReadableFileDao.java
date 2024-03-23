package com.example.readerapp.data.models;

import androidx.annotation.RequiresPermission;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ReadableFileDao {

    @Query("SELECT * FROM readableFiles")
    List<ReadableFile> getAllFiles();

    @Query("SELECT * FROM readableFiles WHERE fileId IN (:readableFileIds)")
    List<ReadableFile> loadFilesByIds(int[] readableFileIds);

    @Insert
    void insertFiles(ReadableFile... readableFiles);

    @Insert
    public void insertFiles(ArrayList<ReadableFile> readableFiles);

    @Update
    public void updateFile(ReadableFile... readableFiles);

    @Delete
    void deleteFile(ReadableFile user);

}
