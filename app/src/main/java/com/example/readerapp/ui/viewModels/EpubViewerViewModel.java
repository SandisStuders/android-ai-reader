package com.example.readerapp.ui.viewModels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import com.example.readerapp.data.models.aiResponse.AiResponse;
import com.example.readerapp.data.models.aiResponse.AiResponseRepository;
import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.models.readableFile.ReadableFileRepository;
import com.example.readerapp.data.repositories.AiConnectionRepository;
import com.example.readerapp.data.repositories.EpubDocumentRepository;
import com.example.readerapp.data.repositories.EpubDocumentRepository.Chapter;
import com.example.readerapp.data.services.ChatGptApiService;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EpubViewerViewModel extends AndroidViewModel {

    private final int SELECTED_TEXT_MAX_CHARS = 600;

    Application application;
    Context context;
    ReadableFileRepository readableFileRepository;
    AiConnectionRepository aiConnectionRepository;
    EpubDocumentRepository epubDocumentRepository;

    ArrayList<Chapter> chapters = new ArrayList<>();
    int currentChapter = 0;
    boolean chapterChanged = false;
    ReadableFile sourceFile;


    public EpubViewerViewModel(Application application) {
        super(application);
        context = application.getApplicationContext();
        this.application = application;
        readableFileRepository = new ReadableFileRepository(application);
        aiConnectionRepository = new AiConnectionRepository(application);
        epubDocumentRepository = new EpubDocumentRepository(application);
    }

    public void initializeEpubBook(String uriString) {
        this.chapters = epubDocumentRepository.initializeBookWithUriString(uriString);
    }

    public String getBookContentBaseUrl() {
        return epubDocumentRepository.findBookContentBaseUrl();
    }

    public String[] getChapterTitles() {
        String[] chapterTitles = new String[chapters.size()];
        for (int i = 0; i < chapters.size(); i++) {
            chapterTitles[i] = chapters.get(i).title;
        }
        return chapterTitles;
    }

    public void decreaseChapter() {
        if (currentChapter > 0) {
            currentChapter--;
            chapterChanged = true;
        } else {
            chapterChanged = false;
        }
    }

    public void increaseChapter() {
        if (currentChapter < chapters.size() - 1) {
            currentChapter++;
            chapterChanged = true;
        } else {
            chapterChanged = false;
        }
    }

    public void setCurrentChapter(int currentChapter) {
        if (currentChapter >= 0 && currentChapter < chapters.size()) {
            this.currentChapter = currentChapter;
            chapterChanged = true;
        } else {
            chapterChanged = false;
        }
    }

    public boolean chapterChanged() {
        return chapterChanged;
    }

    public String getCurrentChapterContent() {
        if (sourceFile != null) {
            sourceFile.setLastOpenChapter(currentChapter);
            readableFileRepository.update(sourceFile);
        }
        String dataPiece = chapters.get(currentChapter).content;
        dataPiece = dataPiece.replaceAll("href=\"http", "hreflink=\"http").replaceAll("<a href=\"[^\"]*", "<a ").replaceAll("hreflink=\"http", "href=\"http");

        return dataPiece;
    }

    public String getSourceFileName() {
        if (sourceFile != null) {
            return sourceFile.getFileName();
        } else {
            return "";
        }
    }

    public LiveData<ReadableFile> getSourceFileByPrimaryKey(String fileName, String fileRelativePath) {
        return readableFileRepository.getReadableFileByPrimaryKey(fileName, fileRelativePath);
    }

    public void initializeSourceFile(ReadableFile readableFile) {
        this.sourceFile = readableFile;
        this.currentChapter = readableFile.getLastOpenChapter();
    }

    // AI REPOSITORY

    public boolean selectedTextTooLong(String selectedText) {
        return selectedText.length() > SELECTED_TEXT_MAX_CHARS;
    }

    public int getSelectedTextMaxChars() {
        return SELECTED_TEXT_MAX_CHARS;
    }

    public CompletableFuture<String> obtainAiResponse(String selectedText, boolean useDefaultSystemPrompt) {
        String bookTitle = "";
        if (sourceFile != null) {
            bookTitle = sourceFile.getFileName();
        }
        return aiConnectionRepository.obtainAiResponse(selectedText, bookTitle, useDefaultSystemPrompt, sourceFile);
    }
}
