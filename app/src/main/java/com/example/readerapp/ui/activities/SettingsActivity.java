package com.example.readerapp.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.readerapp.R;
import com.example.readerapp.ui.fragments.SettingsFragment;
import com.example.readerapp.ui.viewModels.SettingsViewModel;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private SharedPreferences sharedPreferences;
    Context context;
    SettingsViewModel settingsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        context = this;

        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("theme")) {
                    settingsViewModel.updateAppThemeToSettingsSelection();
                } else if (key.equals("language")) {
                    settingsViewModel.updateAppLanguageToSettingsSelection();
                }
            }
        };

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener to avoid memory leaks
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
}
