package com.example.readerapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.readerapp.data.dataSources.AiResponseDao;
import com.example.readerapp.data.dataSources.ReadableFileDao;
import com.example.readerapp.data.models.AiResponse;
import com.example.readerapp.data.models.ReadableFile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ReadableFile.class, AiResponse.class}, version = 15)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ReadableFileDao readableFileDao();
    public abstract AiResponseDao gptResponseDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
