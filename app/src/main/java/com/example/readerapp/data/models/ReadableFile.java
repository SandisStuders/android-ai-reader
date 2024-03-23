package com.example.readerapp.data.models;

import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "readableFiles")
public class ReadableFile {
    @PrimaryKey(autoGenerate = true)
    private int fileId;
    @ColumnInfo(name = "fileName")
    private String name;
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


    public ReadableFile(String name,
                        String contentUri,
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

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "ReadableFileDetails{" +
                "fileId=" + fileId +
                ", name='" + name + '\'' +
                ", contentUri=" + contentUri +
                ", creationDate='" + creationDate + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", fileType='" + fileType + '\'' +
                ", relativePath='" + relativePath + '\'' +
                '}';
    }
}
