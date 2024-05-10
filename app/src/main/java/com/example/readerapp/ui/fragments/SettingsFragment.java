package com.example.readerapp.ui.fragments;

import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.readerapp.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        EditTextPreference personalPromptNamePref = findPreference("personal_prompt_name");
        EditTextPreference personalPromptDefinitionPref = findPreference("personal_prompt_define");

        if (personalPromptNamePref != null) {
            personalPromptNamePref.setOnBindEditTextListener(editText -> {
                int maxLength = 20;
                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter.LengthFilter(maxLength);
                editText.setFilters(filters);
            });
        }

        if (personalPromptDefinitionPref != null) {
            personalPromptDefinitionPref.setOnBindEditTextListener(editText -> {
                int maxLength = 400;
                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter.LengthFilter(maxLength);
                editText.setFilters(filters);
            });
        }

    }

}
