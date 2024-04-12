package com.example.readerapp.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.example.readerapp.R;
import com.example.readerapp.ui.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private SharedPreferences sharedPreferences;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MyLogs", "onCreate started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        context = this;

        Log.d("MyLogs", "onCreate reached line");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("theme")) {
                    changeAppTheme(context);
                }
            }
        };
        Log.d("MyLogs", "onCreate passed line");

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        Log.d("MyLogs", "onResume started");
        super.onResume();
        // Register the listener
        Log.d("MyLogs", "onResume reached line");
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

}
