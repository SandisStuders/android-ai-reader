package com.example.readerapp.data.dataSources;

import android.app.Application;
import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.stream.Stream;

public class InternalStorageDataSource {

    private final String CACHE_DIR;

    public InternalStorageDataSource(Application application) {
        Context context = application.getApplicationContext();
        CACHE_DIR = context.getCacheDir() + "/temp_files";
    }

    public boolean directoryExistsInCache(String directoryRelativePath) {
        String directoryFullPath = CACHE_DIR + File.separator + directoryRelativePath;
        File directory = new File(directoryFullPath);
        return directory.exists() && directory.isDirectory();
    }

    public void emptyCache() {
        if (CACHE_DIR != null) {
            Path cachePath = Paths.get(CACHE_DIR);
            try (Stream<Path> paths = Files.walk(cachePath)) {
                paths.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeByteArrayToCache(byte[] resource, String resourceRelativePath) {
        try {
            Path path = Paths.get(CACHE_DIR, resourceRelativePath);

            Path parentDir = path.getParent();
            if (!Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            Files.write(path, resource, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getCacheDirectoryPath() {
        return "file://" + CACHE_DIR + File.separator;
    }

    public String getCacheDirectoryPath(String directoryRelativePath) {
        return "file://" + CACHE_DIR + File.separator + directoryRelativePath + File.separator;
    }

}
