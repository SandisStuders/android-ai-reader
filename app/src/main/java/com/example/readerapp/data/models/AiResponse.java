package com.example.readerapp.data.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "aiResponses")
public class AiResponse {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @NonNull
    @ColumnInfo(name = "readableFileName")
    private String readableFileName;

    @NonNull
    @ColumnInfo(name = "readableFileRelativePath")
    private String readableFileRelativePath;

    @ColumnInfo(name = "readableFileSourceChapter")
    private int readableFileSourceChapter;

    @NonNull
    @ColumnInfo(name = "selectedText")
    private String selectedText;

    @ColumnInfo(name = "response")
    private String response;

    public AiResponse(@NonNull String readableFileName,
                      @NonNull String readableFileRelativePath,
                      @NonNull String selectedText,
                      String response,
                      int readableFileSourceChapter) {
        this.readableFileName = readableFileName;
        this.readableFileRelativePath = readableFileRelativePath;
        this.selectedText = selectedText;
        this.response = response;
        this.readableFileSourceChapter = readableFileSourceChapter;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getReadableFileName() {
        return readableFileName;
    }

    public void setReadableFileName(@NonNull String readableFileName) {
        this.readableFileName = readableFileName;
    }

    @NonNull
    public String getReadableFileRelativePath() {
        return readableFileRelativePath;
    }

    public void setReadableFileRelativePath(@NonNull String readableFileRelativePath) {
        this.readableFileRelativePath = readableFileRelativePath;
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

    public int getReadableFileSourceChapter() {
        return readableFileSourceChapter;
    }

    public void setReadableFileSourceChapter(int readableFileSourceChapter) {
        this.readableFileSourceChapter = readableFileSourceChapter;
    }

    @Override
    public String toString() {
        return "GptResponse{" +
                "readableFileName='" + readableFileName + '\'' +
                ", readableFileRelativePath='" + readableFileRelativePath + '\'' +
                ", readableFileSourceChapter=" + readableFileSourceChapter +
                ", selectedText='" + selectedText + '\'' +
                ", response='" + response + '\'' +
                '}';
    }
}
