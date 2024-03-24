package com.example.readerapp.data.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "readableFiles")
public class ReadableFile {

    @PrimaryKey(autoGenerate = true)
    private int id;

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

    @ColumnInfo(name = "relativePath")
    private String relativePath;

    @ColumnInfo(name = "mostRecentAccessTime")
    private String mostRecentAccessTime;

    @ColumnInfo(name = "isFavorite")
    private boolean isFavorite;

    public ReadableFile(String fileName, String contentUri, String creationDate, String fileSize, String fileType, String relativePath, String mostRecentAccessTime, boolean isFavorite) {
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
    public ReadableFile(String fileName,
                        String contentUri,
                        String creationDate,
                        String fileSize,
                        String fileType,
                        String relativePath) {
        this.fileName = fileName;
        this.contentUri = contentUri;
        this.creationDate = creationDate;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.relativePath = relativePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
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

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getMostRecentAccessTime() {
        return mostRecentAccessTime;
    }

    public void setMostRecentAccessTime(String mostRecentAccessTime) {
        this.mostRecentAccessTime = mostRecentAccessTime;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

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
