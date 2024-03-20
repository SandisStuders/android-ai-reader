package com.example.readerapp.data.models;

import android.net.Uri;

import androidx.annotation.NonNull;

public class ReadableFileDetails {

    private String name;
    private Uri contentUri;
    private String creationDate;
    private String fileSize;
    private String fileType;
    private String relativePath;


    public ReadableFileDetails(String name,
                               Uri contentUri,
                               String creationDate,
                               String fileSize,
                               String fileType,
                               String relativePath) {
        this.name = name;
        this.contentUri = contentUri;
        this.creationDate = creationDate;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.relativePath = relativePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getContentUri() {
        return contentUri;
    }

    public void setContentUri(Uri contentUri) {
        this.contentUri = contentUri;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    @Override
    public String toString() {
        return "ReadableFileDetails{" +
                "name='" + name + '\'' +
                ", contentUri=" + contentUri +
                ", creationDate='" + creationDate + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", fileType='" + fileType + '\'' +
                ", relativePath='" + relativePath + '\'' +
                '}';
    }
}
