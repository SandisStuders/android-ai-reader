package com.example.readerapp.data.models;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ReadableFile.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ReadableFileDao readableFileDao();
}
