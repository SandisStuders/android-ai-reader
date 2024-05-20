package com.example.readerapp.data.dataSources;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SharedPreferencesDataSource {

    Application application;
    SharedPreferences sharedPreferences;

    public SharedPreferencesDataSource(Application application) {
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

    public String getAppTheme() {
        return sharedPreferences.getString("theme", "0");
    }

    public String getAppLanguage() {
        return sharedPreferences.getString("language", "0");
    }

}
