package com.example.readerapp.ui.activities;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.readerapp.R;
import com.example.readerapp.data.models.aiResponse.AiResponse;
import com.example.readerapp.data.models.aiResponse.GptResponseViewModel;
import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.services.ChatGptApiService;
import com.example.readerapp.databinding.ActivityEpubViewerBinding;
import com.example.readerapp.ui.customViews.ReaderView;
import com.example.readerapp.ui.viewModels.EpubViewerViewModel;
import com.example.readerapp.ui.viewModels.FileViewerViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class EpubViewerActivity extends AppCompatActivity {

    private EpubViewerViewModel epubViewerViewModel;

    ActivityEpubViewerBinding binding;
    ReaderView epubViewer;
    BottomNavigationView bottomAppBar;

    private String baseUrl;
    Context context;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epub_viewer);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_epub_viewer);
        epubViewer = binding.epubViewer;
        bottomAppBar = binding.bottomAppBar;

        WebSettings webSettings = epubViewer.getSettings();
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptEnabled(true);

        Intent intent = getIntent();
        String fileName = intent.getStringExtra("FILE_NAME");
        String fileRelativePath = intent.getStringExtra("FILE_RELATIVE_PATH");

        epubViewerViewModel = new ViewModelProvider(this).get(EpubViewerViewModel.class);

        AtomicReference<Boolean> firstFileObservation = new AtomicReference<>(true);
        epubViewerViewModel.getSourceFileByPrimaryKey(fileName, fileRelativePath).observe(this, readableFile -> {
            if (readableFile != null && firstFileObservation.get()) {
                epubViewerViewModel.initializeSourceFile(readableFile);
                loadCurrentChapter();
                firstFileObservation.set(false);
            }
        });

        String uriString = intent.getStringExtra("URI_STRING");
        epubViewerViewModel.initializeEpubBook(uriString);
        this.baseUrl = epubViewerViewModel.getBookContentBaseUrl();

        bottomAppBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.prevChapter) {
                epubViewerViewModel.decreaseChapter();
                if (epubViewerViewModel.chapterChanged()) {
                    loadCurrentChapter();
                }
                return true;
            } else if (itemId == R.id.selectChapter) {
                String[] chapterTitles = epubViewerViewModel.getChapterTitles();
                showChapterList(chapterTitles);
                return true;
            } else if (itemId == R.id.nextChapter) {
                epubViewerViewModel.increaseChapter();
                if (epubViewerViewModel.chapterChanged()) {
                    loadCurrentChapter();
                }
                return true;
            }

            return false;
        });

        epubViewer.setOnContextualActionSelectedListener(new ReaderView.OnContextualActionSelectedListener() {
            @Override
            public void onExplainItemSelected(String selectedText) {
                receiveAiResponse(selectedText, true);
            }

            @Override
            public void onCopyItemSelected(String selectedText) {
                String processedValue = selectedText.replaceAll("^\"|\"$", "");
                //TODO: JavaScript copies string as JSON string therefore escape characters possible. These should be escaped

                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("default", processedValue);
                clipboardManager.setPrimaryClip(clip);
            }

            @Override
            public void onPersonalPromptItemSelected(String selectedText) {
                receiveAiResponse(selectedText, false);
            }
        });

    }

    private void loadCurrentChapter() {
        String chapterContent = epubViewerViewModel.getCurrentChapterContent();

        epubViewer.loadDataWithBaseURL(baseUrl,
                chapterContent,
                "text/html",
                "UTF-8",
                null);
    }

    public void receiveAiResponse(String selectedText, boolean useDefaultSystemPrompt) {
        if (epubViewerViewModel.selectedTextTooLong(selectedText)) {
            showTextTooLongAlert();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String response = epubViewerViewModel.obtainAiResponse(selectedText, useDefaultSystemPrompt);

            handler.post(() -> {
                if (response == null || response.equals("")) {
                    Snackbar.make(epubViewer, getString(R.string.response_error_snack_bar_text), Snackbar.LENGTH_SHORT)
                            .setAnchorView(bottomAppBar)
                            .show();
                } else {
                    Intent intent = new Intent(this, ResponseViewerActivity.class);
                    intent.putExtra("SELECTION", selectedText);
                    intent.putExtra("RESPONSE", response);
                    intent.putExtra("FILENAME", epubViewerViewModel.getSourceFileName());
                    this.startActivity(intent);
                }
            });
        });
        Toast.makeText(this, getString(R.string.response_generation_started_text), Toast.LENGTH_SHORT).show();
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

    public void showChapterList(String[] chapterTitles) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Chapter");

        builder.setItems(chapterTitles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                epubViewerViewModel.setCurrentChapter(which);
                loadCurrentChapter();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
