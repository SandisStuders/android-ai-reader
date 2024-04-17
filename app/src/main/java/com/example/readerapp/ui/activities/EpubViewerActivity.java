package com.example.readerapp.ui.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.readerapp.R;
import com.example.readerapp.data.models.gptResponse.GptResponse;
import com.example.readerapp.data.models.gptResponse.GptResponseViewModel;
import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.models.readableFile.ReadableFileViewModel;
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
    ReadableFile sourceFile;
    private GptResponseViewModel mGptResponseViewModel;
    private ReadableFileViewModel mReadableFileViewModel;
    Context context;

    private final int SELECTED_TEXT_MAX_CHARS = 600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epub_viewer);
        context = this;

        mGptResponseViewModel = new ViewModelProvider(this).get(GptResponseViewModel.class);
        mReadableFileViewModel = new ViewModelProvider(this).get(ReadableFileViewModel.class);


        binding = DataBindingUtil.setContentView(this, R.layout.activity_epub_viewer);
        epubViewer = binding.epubViewer;
        bottomAppBar = binding.bottomAppBar;

        epubViewer.setActionModeCallback(this);

        if (savedInstanceState == null) {
            currentChapter = 0;
        } else {
            currentChapter = savedInstanceState.getInt("CURRENT_CHAPTER", 0);
        }
        bookData = new ArrayList<>();

        epubViewer.getSettings().setJavaScriptEnabled(true);

        Intent intent = getIntent();

        String fileName = intent.getStringExtra("FILE_NAME");
        String fileRelativePath = intent.getStringExtra("FILE_RELATIVE_PATH");
        Log.d("MyLogs", "GOT INTENT. FILE NAME: " + fileName + " ; RELATIVE PATH: " + fileRelativePath);

        mReadableFileViewModel.getReadableFileByPrimaryKey(fileName, fileRelativePath).observe(this, readableFile -> {
            if (readableFile != null) {
                currentChapter = readableFile.getLastOpenChapter();
                loadCurrentChapter();
                sourceFile = readableFile;
            }
        });

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
                    String[] chapters = new String[bookData.size()];
                    for (int i = 0; i < bookData.size(); i++) {
                        chapters[i] = String.valueOf(i+1);
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Select a Chapter");

                    builder.setItems(chapters, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currentChapter = which;
                            loadCurrentChapter();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

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
        Log.d("MyLogs", "Loading current chapter!");
        if (sourceFile != null) {
            ReadableFile readableFile = sourceFile;
            readableFile.setLastOpenChapter(currentChapter);
            mReadableFileViewModel.update(readableFile);
            Log.d("MyLogs", "UPDATED THAT FILE BRAH CHAPTER: " + currentChapter);
        }
        String dataPiece = bookData.get(currentChapter);

        epubViewer.loadDataWithBaseURL(null,
                dataPiece,
                "text/html",
                "UTF-8",
                null);
    }

    @Override
    public void onTextSelected(String selectedText, boolean useDefaultSystemPrompt) {
        if (selectedText.length() > SELECTED_TEXT_MAX_CHARS) {
            String alertTitle = getString(R.string.document_selection_alert_title);
            String alertText1 = getString(R.string.document_selection_alert_text_1);
            String alertText2 = getString(R.string.document_selection_alert_text_2);
            String alertButtonOk = getString(R.string.alert_button_ok);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(alertText1 + SELECTED_TEXT_MAX_CHARS + alertText2)
                    .setTitle(alertTitle);

            builder.setPositiveButton(alertButtonOk, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this); // 'context' refers to the Activity or Context object
        int temperaturePercentage = sharedPreferences.getInt("temperature", 40);

        String systemPrompt = "";
        if (useDefaultSystemPrompt) {
            systemPrompt = "You are an AI assistant integrated into a mobile reading application. The user has selected certain text from the document they are reading and sent to you as a prompt because they want a bigger explanation on their selection. Interpret the text and try to provide factual knowledge surrounding it, avoid speculations and uncertainties if possible. Try to keep your response encompassing but concise, 100-200 tokens, if possible.";
        } else {
            systemPrompt = sharedPreferences.getString("personal_prompt_define", "defaultDefinition");
        }

        String prompt = selectedText.replaceAll("^\"|\"$", "");
        boolean includeFileName = sharedPreferences.getBoolean("send_file_name", false);
        if (includeFileName && sourceFile != null) {
            prompt = "File name: " + sourceFile.getFileName() + "; Selected text: " + prompt;
        }

        double temperature = ((double) temperaturePercentage) / 100;

        ChatGptApiService chatGptApiService = new ChatGptApiService();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        String finalSystemPrompt = systemPrompt;
        String finalPrompt = prompt;
        executor.execute(() -> {
            Log.d("MyLogs", finalPrompt);
            String response = chatGptApiService.processPrompt(finalSystemPrompt, finalPrompt, temperature);
            String fileName;
            String fileRelativePath;
            if (sourceFile != null) {
                fileName = sourceFile.getFileName();
                fileRelativePath = sourceFile.getRelativePath();
            } else {
                fileRelativePath = "";
                fileName = "";
            }

            handler.post(() -> {
                Log.d("MyLogs", "Response: " + response);

                GptResponse gptResponse = new GptResponse(fileName,
                        fileRelativePath,
                        selectedText,
                        response,
                        currentChapter);
                mGptResponseViewModel.insert(gptResponse);

                Context context = this;
                Intent intent = new Intent(context, ResponseViewerActivity.class);
                intent.putExtra("SELECTION", selectedText);
                intent.putExtra("RESPONSE", response);
                intent.putExtra("FILENAME", fileName);
                context.startActivity(intent);
            });
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CURRENT_CHAPTER", currentChapter);
    }
}
