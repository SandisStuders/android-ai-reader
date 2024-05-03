package com.example.readerapp.ui.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.example.readerapp.R;
import com.example.readerapp.databinding.ActivityMainBinding;
import com.example.readerapp.ui.fragments.FileViewerFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Toolbar appToolbar;
    Context context;

    int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    loadFileViewerFragment();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MyLogs", "Main Activity Created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                String alertText = getString(R.string.storage_access_alert_text);
                String alertTitle = getString(R.string.storage_access_alert_title);
                String alertButtonOk = getString(R.string.alert_button_ok);

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
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }

        if (savedInstanceState == null) { // Check to prevent adding the fragment again on rotate
            loadFileViewerFragment();
        } else {
            Log.d("MyLogs", "onCreate method with savedInstanceState bundle");
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("MyLogs", "We're in request permission result function");
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFileViewerFragment();
                Log.d("MyLogs", "Permission was granted!");
            } else {
                Log.d("MyLogs", "Permission was not granted!");
            }
        }
    }

    @Override
    protected void onResume() {
        Log.d("MyLogs", "Main Activity Resumed");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    private void loadFileViewerFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.file_viewer_fragment, new FileViewerFragment(), "FileViewerFragmentTag")
                .commit();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("MyLogs", "Activity onSaveInstanceState gets called");
    }
}
