package com.example.readerapp.data.repositories;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SharedPreferencesRepository {

    Application application;
    SharedPreferences sharedPreferences;

    public SharedPreferencesRepository(Application application) {
        this.application = application;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext());
    }

    public int getTemperature() {
        return sharedPreferences.getInt("temperature", 40);
    }

    public boolean fileNameIncluded() {
        return sharedPreferences.getBoolean("send_file_name", false);
    }

    public String getUsersPersonalPrompt() {
        return sharedPreferences.getString("personal_prompt_define", "");
    }

}
