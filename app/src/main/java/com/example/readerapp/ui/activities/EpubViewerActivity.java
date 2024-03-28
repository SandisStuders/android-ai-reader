package com.example.readerapp.ui.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.readerapp.R;
import com.example.readerapp.databinding.ActivityEpubViewerBinding;
import com.example.readerapp.ui.customViews.ReaderView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.epub.EpubReader;

public class EpubViewerActivity extends AppCompatActivity {

    ActivityEpubViewerBinding binding;
    int currentChapter;
    ArrayList<String> bookData;
    ReaderView epubViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MyLogs", "Reader started!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epub_viewer);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_epub_viewer);
        currentChapter = 8;
        bookData = new ArrayList<>();

        epubViewer = binding.epubViewer;
        epubViewer.getSettings().setJavaScriptEnabled(true);

        Intent intent = getIntent();
        String uriString = intent.getStringExtra("URI_STRING");
        Uri uri = Uri.parse(uriString);

        ContentResolver contentResolver = getContentResolver();
        try {
            InputStream fileStream = contentResolver.openInputStream(uri);
            Book book = (new EpubReader()).readEpub(fileStream);

            Spine spine = book.getSpine();
            Log.d("MyLogs", "SPINE SIZE: " + spine.size());

            for(int i = 0; i < spine.size(); i++){
                StringBuilder builder = new StringBuilder();
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(spine.getResource(i).getInputStream()));
                    String aux = "";
                    while ((aux = r.readLine()) != null) {
                        builder.append(aux);
                        builder.append('\n');
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
                bookData.add(builder.toString());

            }
            loadCurrentChapter();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BottomNavigationView bottomAppBar = binding.bottomAppBar;
        bottomAppBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.prevChapter) {
                    Toast toast = Toast.makeText(binding.getRoot().getContext() , "Previous chapter", Toast.LENGTH_SHORT);
                    toast.show();
                    if (currentChapter > 0) {
                        currentChapter--;
                        loadCurrentChapter();
                    }
                    return true;
                } else if (itemId == R.id.selectChapter) {
                    Toast toast = Toast.makeText(binding.getRoot().getContext() , "Select chapter", Toast.LENGTH_SHORT);
                    toast.show();
                    return true;
                } else if (itemId == R.id.nextChapter) {
                    Toast toast = Toast.makeText(binding.getRoot().getContext() , "Next chapter", Toast.LENGTH_SHORT);
                    toast.show();
                    if (currentChapter < bookData.size() - 1) {
                        currentChapter++;
                        loadCurrentChapter();
                    }
                    return true;
                }

                return false;
            }
        });

    }

    private void loadCurrentChapter() {
        String dataPiece = bookData.get(currentChapter);
        Log.d("MyLogs", dataPiece);

        epubViewer.loadDataWithBaseURL(null,
                dataPiece,
                "text/html",
                "UTF-8",
                null);
    }

}
