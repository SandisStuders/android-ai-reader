package com.example.readerapp.data.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "readableFiles", primaryKeys = {"fileName", "relativePath"})
public class ReadableFile {

    @NonNull
    @ColumnInfo(name = "fileName")
    private String fileName;

    @ColumnInfo(name = "contentUri")
    private String contentUri;

    @ColumnInfo(name = "creationDate")
    private String creationDate;

    @ColumnInfo(name = "fileSize")
    private String fileSize;

    @ColumnInfo(name = "fileType")
    private String fileType;

    @NonNull
    @ColumnInfo(name = "relativePath")
    private String relativePath;

    @ColumnInfo(name = "mostRecentAccessTime")
    private long mostRecentAccessTime;

    @ColumnInfo(name = "isFavorite")
    private boolean isFavorite;

    public ReadableFile(@NonNull String fileName,
                        String contentUri,
                        String creationDate,
                        String fileSize,
                        String fileType,
                        @NonNull String relativePath,
                        long mostRecentAccessTime,
                        boolean isFavorite) {
        this.fileName = fileName;
        this.contentUri = contentUri;
        this.creationDate = creationDate;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.relativePath = relativePath;
        this.mostRecentAccessTime = mostRecentAccessTime;
        this.isFavorite = isFavorite;
    }

    @Ignore
    public ReadableFile(@NonNull String fileName,
                        String contentUri,
                        String creationDate,
                        String fileSize,
                        String fileType,
                        @NonNull String relativePath) {
        this.fileName = fileName;
        this.contentUri = contentUri;
        this.creationDate = creationDate;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.relativePath = relativePath;
    }

    @NonNull
    public String getFileName() {
        return fileName;
    }

    public void setFileName(@NonNull String fileName) {
        this.fileName = fileName;
    }

    public String getContentUri() {
        return contentUri;
    }

    public void setContentUri(String contentUri) {
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

    @NonNull
    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(@NonNull String relativePath) {
        this.relativePath = relativePath;
    }

    public long getMostRecentAccessTime() {
        return mostRecentAccessTime;
    }

    public void setMostRecentAccessTime(long mostRecentAccessTime) {
        this.mostRecentAccessTime = mostRecentAccessTime;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @NonNull
    @Override
    public String toString() {
        return "ReadableFile{" +
                ", name='" + fileName + '\'' +
                ", contentUri='" + contentUri + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", fileType='" + fileType + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", mostRecentAccessTime='" + mostRecentAccessTime + '\'' +
                ", isFavorite=" + isFavorite +
                '}';
    }
}
