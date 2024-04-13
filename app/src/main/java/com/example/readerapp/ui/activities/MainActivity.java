package com.example.readerapp.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.preference.PreferenceManager;

import com.example.readerapp.R;
import com.example.readerapp.databinding.ActivityMainBinding;
import com.example.readerapp.ui.fragments.FileViewerFragment;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Toolbar appToolbar;
    Context context;
    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    loadFileViewerFragment();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                String alertText = getString(R.string.storage_access_alert_text);
                String alertTitle = getString(R.string.storage_access_alert_title);
                String alertButtonOk = getString(R.string.storage_access_alert_button_ok);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(alertText)
                        .setTitle(alertTitle);

                builder.setPositiveButton(alertButtonOk, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);

                        mStartForResult.launch(intent);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (savedInstanceState == null) { // Check to prevent adding the fragment again on rotate
            loadFileViewerFragment();
        }

        appToolbar = binding.appToolbar;
        setSupportActionBar(appToolbar);

        appToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_history) {
                    Intent intent = new Intent(context, ResponseHistoryViewerActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_settings) {
                    Intent intent = new Intent(context, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    private void loadFileViewerFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.file_viewer_fragment, new FileViewerFragment())
                .commit();
    }

}
