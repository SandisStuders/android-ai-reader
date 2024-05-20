package com.example.readerapp.data.repositories;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.readerapp.data.dataSources.SharedPreferencesDataSource;

public class SharedPreferencesRepository {

    Application application;
    SharedPreferencesDataSource sharedPreferencesDataSource;

    public SharedPreferencesRepository(Application application) {
        this.application = application;
        this.sharedPreferencesDataSource = new SharedPreferencesDataSource(application);
    }

    public int getTemperature() {
        return sharedPreferencesDataSource.getTemperature();
    }

    public boolean fileNameIncluded() {
        return sharedPreferencesDataSource.fileNameIncluded();
    }

    public String getUsersPersonalPrompt() {
        return sharedPreferencesDataSource.getUsersPersonalPrompt();
    }

    public void updateAppThemeToSettingsSelection() {
        int theme = Integer.parseInt(sharedPreferencesDataSource.getAppTheme());

        if (theme == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if (theme == 1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (theme == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void updateAppLanguageToSettingsSelection() {
        String lang = sharedPreferencesDataSource.getAppLanguage();

        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }

}
