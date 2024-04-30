package com.example.readerapp.ui.activities;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.readerapp.R;
import com.example.readerapp.data.models.gptResponse.GptResponse;
import com.example.readerapp.data.models.gptResponse.GptResponseViewModel;
import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.services.ChatGptApiService;
import com.example.readerapp.databinding.ActivityEpubViewerBinding;
import com.example.readerapp.ui.customViews.ReaderView;
import com.example.readerapp.ui.viewModels.EpubViewerViewModel;
import com.example.readerapp.ui.viewModels.FileViewerViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class EpubViewerActivity extends AppCompatActivity implements ReaderView.ActionModeCallback {

    private EpubViewerViewModel epubViewerViewModel;

    ActivityEpubViewerBinding binding;
    ReaderView epubViewer;
    BottomNavigationView bottomAppBar;
    int currentChapter;
    ReadableFile sourceFile;
    private GptResponseViewModel mGptResponseViewModel;
    private FileViewerViewModel mFileViewerViewModel;
    Context context;
    private String baseUrl;

    private final int SELECTED_TEXT_MAX_CHARS = 600;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epub_viewer);
        context = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_epub_viewer);
        epubViewer = binding.epubViewer;
        bottomAppBar = binding.bottomAppBar;

        epubViewer.setActionModeCallback(this);
        WebSettings webSettings = epubViewer.getSettings();
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptEnabled(true);

        if (savedInstanceState == null) {
            currentChapter = 0;
        } else {
            currentChapter = savedInstanceState.getInt("CURRENT_CHAPTER", 0);
        }

        Intent intent = getIntent();
        String fileName = intent.getStringExtra("FILE_NAME");
        String fileRelativePath = intent.getStringExtra("FILE_RELATIVE_PATH");

        mGptResponseViewModel = new ViewModelProvider(this).get(GptResponseViewModel.class);
        mFileViewerViewModel = new ViewModelProvider(this).get(FileViewerViewModel.class);
        epubViewerViewModel = new ViewModelProvider(this).get(EpubViewerViewModel.class);

        //  -----------

        AtomicReference<Boolean> firstFileObservation = new AtomicReference<>(true);
        mFileViewerViewModel.getReadableFileByPrimaryKey(fileName, fileRelativePath).observe(this, readableFile -> {
            if (readableFile != null && firstFileObservation.get()) {
                currentChapter = readableFile.getLastOpenChapter();
                sourceFile = readableFile;
                loadCurrentChapter("primary");
                firstFileObservation.set(false);
            }
        });

        String uriString = intent.getStringExtra("URI_STRING");
        Uri uri = Uri.parse(uriString);

        ContentResolver contentResolver = getContentResolver();
        try {
            InputStream fileStream = contentResolver.openInputStream(uri);
            Book book = (new EpubReader()).readEpub(fileStream);

            epubViewerViewModel.emptyCache();
            epubViewerViewModel.downloadResources(book);

            this.baseUrl = epubViewerViewModel.findBaseUrl(book);

            epubViewerViewModel.setChapters(epubViewerViewModel.getChapterContentAndTitles(book));

            loadCurrentChapter("onCreate content resolver");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // ----------

        bottomAppBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                Log.d("MyLogs", "ITEM PRESSED!");

                if (itemId == R.id.prevChapter) {
                    if (currentChapter > 0) {
                        currentChapter--;
                        loadCurrentChapter("navigation prev chapt");
                    }
                    return true;
                } else if (itemId == R.id.selectChapter) {
                    String[] chapterTitles = epubViewerViewModel.getChapterTitles();

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Select a Chapter");

                    builder.setItems(chapterTitles, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currentChapter = which;
                            loadCurrentChapter("navigation select chapt");
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return true;
                } else if (itemId == R.id.nextChapter) {
                    Log.d("MyLogs", "Next item button pressed!");
                    if (currentChapter < epubViewerViewModel.getChapterAmount() - 1) {
                        currentChapter++;
                        loadCurrentChapter("navigation next chapt");
                    }
                    return true;
                }

                return false;
            }
        });

    }

    private void loadCurrentChapter(String source) {
        Log.d("MyLogs", "Loading current chapter! Source: " + source);
        if (sourceFile != null) {
            ReadableFile readableFile = sourceFile;
            readableFile.setLastOpenChapter(currentChapter);
            mFileViewerViewModel.update(readableFile);
            Log.d("MyLogs", "UPDATED THAT FILE BRAH CHAPTER: " + currentChapter);
        }
        String dataPiece = epubViewerViewModel.getChapterContent(currentChapter);
        dataPiece = dataPiece.replaceAll("href=\"http", "hreflink=\"http").replaceAll("<a href=\"[^\"]*", "<a ").replaceAll("hreflink=\"http", "href=\"http");

        Log.d("MyLogs", dataPiece);

        epubViewer.loadDataWithBaseURL(baseUrl,
                dataPiece,
                "text/html",
                "UTF-8",
                null);
    }

    @Override
    public void onTextSelected(String selectedText, boolean useDefaultSystemPrompt) {
        if (epubViewerViewModel.selectedTextTooLong(selectedText)) {
            showTextTooLongAlert();
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this); // 'context' refers to the Activity or Context object
        int temperaturePercentage = sharedPreferences.getInt("temperature", 40);

        String prompt = selectedText.replaceAll("^\"|\"$", "");
        boolean includeFileName = sharedPreferences.getBoolean("send_file_name", false);
        if (includeFileName && sourceFile != null) {
            prompt = "File name: " + sourceFile.getFileName() + "; Selected text: " + prompt;
        }

        String systemPrompt = "";
        if (useDefaultSystemPrompt) {
            systemPrompt = "You are an AI assistant integrated into a mobile reading application. The user has selected certain text from the document they are reading and sent to you as a prompt because they want an explanation on their selection. Interpret the text and try to provide factual knowledge surrounding it, avoid speculations and uncertainties if possible. If the text includes only one term, provide definition for it. Prompt may include the filename as additional context, use it, if it is beneficial. Try to keep your response encompassing but reasonably concise.";
        } else {
            systemPrompt = "You are an AI assistant integrated into a mobile reading application. The user has selected certain text from the document they are reading and sent to you as a prompt. Their prompt also includes more specific instructions on what they'd like to receive in the response. Prompt may include the filename as additional context, use it, if it is beneficial. Try to keep your response encompassing but reasonably concise.";
            String userInstructions = sharedPreferences.getString("personal_prompt_define", "");
            prompt = prompt + "; User's instructions: " + userInstructions;
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

                Intent intent = new Intent(this, ResponseViewerActivity.class);
                intent.putExtra("SELECTION", selectedText);
                intent.putExtra("RESPONSE", response);
                intent.putExtra("FILENAME", fileName);
                this.startActivity(intent);
            });
        });
    }

    public void showTextTooLongAlert() {
        String alertTitle = getString(R.string.document_selection_alert_title);
        String alertText1 = getString(R.string.document_selection_alert_text_1);
        String alertText2 = getString(R.string.document_selection_alert_text_2);
        String alertButtonOk = getString(R.string.alert_button_ok);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(alertText1 + epubViewerViewModel.getSelectedTextMaxChars() + alertText2)
                .setTitle(alertTitle);

        builder.setPositiveButton(alertButtonOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CURRENT_CHAPTER", currentChapter);
    }
}
