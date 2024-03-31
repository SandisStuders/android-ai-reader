package com.example.readerapp.data.models.gptResponse;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "gptResponses", primaryKeys = {"sourceReadableFile", "sourceFileRelativePath", "selectedText"})
public class GptResponse {

    @NonNull
    @ColumnInfo(name = "sourceReadableFile")
    private String sourceReadableFile;

    @NonNull
    @ColumnInfo(name = "sourceFileRelativePath")
    private String sourceFileRelativePath;

    @NonNull
    @ColumnInfo(name = "selectedText")
    private String selectedText;

    @ColumnInfo(name = "response")
    private String response;

    @ColumnInfo(name = "sourceChapter")
    private int sourceChapter;

    public GptResponse(@NonNull String sourceReadableFile,
                       @NonNull String sourceFileRelativePath,
                       @NonNull String selectedText,
                       String response,
                       int sourceChapter) {
        this.sourceReadableFile = sourceReadableFile;
        this.sourceFileRelativePath = sourceFileRelativePath;
        this.selectedText = selectedText;
        this.response = response;
        this.sourceChapter = sourceChapter;
    }

    @NonNull
    public String getSourceReadableFile() {
        return sourceReadableFile;
    }

    public void setSourceReadableFile(@NonNull String sourceReadableFile) {
        this.sourceReadableFile = sourceReadableFile;
    }

    @NonNull
    public String getSourceFileRelativePath() {
        return sourceFileRelativePath;
    }

    public void setSourceFileRelativePath(@NonNull String sourceFileRelativePath) {
        this.sourceFileRelativePath = sourceFileRelativePath;
    }

    @NonNull
    public String getSelectedText() {
        return selectedText;
    }

    public void setSelectedText(@NonNull String selectedText) {
        this.selectedText = selectedText;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getSourceChapter() {
        return sourceChapter;
    }

    public void setSourceChapter(int sourceChapter) {
        this.sourceChapter = sourceChapter;
    }

    @Override
    public String toString() {
        return "GptResponse{" +
                "sourceReadableFile='" + sourceReadableFile + '\'' +
                ", sourceFileRelativePath='" + sourceFileRelativePath + '\'' +
                ", selectedText='" + selectedText + '\'' +
                ", response='" + response + '\'' +
                ", sourceChapter=" + sourceChapter +
                '}';
    }
}
