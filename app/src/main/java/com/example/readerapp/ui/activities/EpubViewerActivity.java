package com.example.readerapp.ui.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.readerapp.R;
import com.example.readerapp.data.services.ChatGptApiService;
import com.example.readerapp.databinding.ActivityEpubViewerBinding;
import com.example.readerapp.ui.customViews.ReaderView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.epub.EpubReader;

public class EpubViewerActivity extends AppCompatActivity implements ReaderView.ActionModeCallback {

    ActivityEpubViewerBinding binding;
    ReaderView epubViewer;
    BottomNavigationView bottomAppBar;
    int currentChapter;
    ArrayList<String> bookData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epub_viewer);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_epub_viewer);
        epubViewer = binding.epubViewer;
        bottomAppBar = binding.bottomAppBar;

        epubViewer.setActionModeCallback(this);

        currentChapter = 0;
        bookData = new ArrayList<>();

        epubViewer.getSettings().setJavaScriptEnabled(true);

        Intent intent = getIntent();
        String uriString = intent.getStringExtra("URI_STRING");
        Uri uri = Uri.parse(uriString);

        ContentResolver contentResolver = getContentResolver();
        try {
            InputStream fileStream = contentResolver.openInputStream(uri);
            Book book = (new EpubReader()).readEpub(fileStream);

            Spine spine = book.getSpine();

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

        bottomAppBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.prevChapter) {
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

        epubViewer.loadDataWithBaseURL(null,
                dataPiece,
                "text/html",
                "UTF-8",
                null);
    }

    @Override
    public void onTextSelected(String selectedText) {
        String processedValue = selectedText.replaceAll("^\"|\"$", "");
        ChatGptApiService chatGptApiService = new ChatGptApiService();
        String prompt = "Please provide a concise explanation of the below excerpt from a book: " + processedValue;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Log.d("MyLogs", prompt);
            String response = chatGptApiService.processPrompt(prompt);

            handler.post(() -> {
                Log.d("MyLogs", "Response: " + response);
                Context context = this;
                Intent intent = new Intent(context, ResponseViewerActivity.class);
                intent.putExtra("SELECTION", processedValue);
                intent.putExtra("RESPONSE", response);
                context.startActivity(intent);
            });
        });
    }
}
