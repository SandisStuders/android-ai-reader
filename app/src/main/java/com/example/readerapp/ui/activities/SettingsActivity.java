package com.example.readerapp.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.PreferenceManager;

import com.example.readerapp.R;
import com.example.readerapp.ui.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private SharedPreferences sharedPreferences;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        context = this;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("theme")) {
                    changeAppTheme(context);
                } else if (key.equals("language")) {
                    changeAppLanguage(context);
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

    private void changeAppTheme(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int theme = Integer.parseInt(sharedPreferences.getString("theme", "0"));

        if (theme == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if (theme == 1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (theme == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void changeAppLanguage(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lang = sharedPreferences.getString("language", "0");

        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(lang);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }
}
