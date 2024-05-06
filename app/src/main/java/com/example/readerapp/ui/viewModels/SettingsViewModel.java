package com.example.readerapp.ui.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.readerapp.data.repositories.SharedPreferencesRepository;

public class SettingsViewModel extends AndroidViewModel {

    SharedPreferencesRepository sharedPreferencesRepository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        sharedPreferencesRepository = new SharedPreferencesRepository(application);
    }

    public void updateAppThemeToSettingsSelection() {
        sharedPreferencesRepository.updateAppThemeToSettingsSelection();
    }

    public void updateAppLanguageToSettingsSelection() {
        sharedPreferencesRepository.updateAppLanguageToSettingsSelection();
    }
}
