package com.example.readerapp.data.repositories;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
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

    public void updateAppThemeToSettingsSelection() {
        int theme = Integer.parseInt(sharedPreferences.getString("theme", "0"));

        if (theme == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if (theme == 1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (theme == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void updateAppLanguageToSettingsSelection() {
        String lang = sharedPreferences.getString("language", "0");

        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }

}
