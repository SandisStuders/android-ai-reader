package com.example.readerapp.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.example.readerapp.R;
import com.example.readerapp.databinding.ActivityFileViewerBinding;
import com.example.readerapp.ui.fragments.FileViewerFragment;
import com.google.android.material.navigation.NavigationBarView;

public class FileViewerActivity extends AppCompatActivity {

    ActivityFileViewerBinding binding;
    Toolbar appToolbar;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);
        context = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_file_viewer);

        if (savedInstanceState == null) { // Check to prevent adding the fragment again on rotate
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.file_viewer_fragment, new FileViewerFragment())
                    .commit();
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

}
