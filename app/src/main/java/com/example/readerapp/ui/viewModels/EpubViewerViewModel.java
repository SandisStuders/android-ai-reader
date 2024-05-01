package com.example.readerapp.ui.viewModels;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;

import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.models.readableFile.ReadableFileRepository;
import com.example.readerapp.data.repositories.ExternalFileRepository;
import com.example.readerapp.ui.activities.EpubViewerActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.service.MediatypeService;

public class EpubViewerViewModel extends AndroidViewModel {

    Context context;
    private final String CACHE_DIR;

    private final Set<MediaType> cachedFileFormats = new HashSet<>(Arrays.asList(
            MediatypeService.CSS,
            MediatypeService.PNG,
            MediatypeService.GIF,
            MediatypeService.JPG,
            MediatypeService.SVG
    ));

    private final int SELECTED_TEXT_MAX_CHARS = 600;
    ArrayList<Chapter> chapters = new ArrayList<>();
    int currentChapter = 0;
    boolean chapterChanged = false;

    public EpubViewerViewModel(Application application) {
        super(application);
        context = application.getApplicationContext();
        CACHE_DIR = context.getCacheDir() + "/temp_files";
    }

    public String findBaseUrl(Book book) {
        String baseUrl;

        String oebpsDirectory = CACHE_DIR + File.separator + "OEBPS";
        String opfSubdirectory = book.getOpfResource().getHref().replace("content.opf", "").replace("/", "");
        String opfDirectory = CACHE_DIR + File.separator + opfSubdirectory;

        File oebpsFile = new File(oebpsDirectory);
        File opfFile = new File(opfDirectory);

        if (oebpsFile.exists() && oebpsFile.isDirectory()) {
            Log.d("MyLogs", "Base URL crated to OEBPS");
            baseUrl = "file://" + oebpsDirectory + File.separator;
        } else if (opfFile.exists() && opfFile.isDirectory() && !opfSubdirectory.equals("")) {
            Log.d("MyLogs", "Base URL crated to resource folder");
            baseUrl = "file://" + opfDirectory + File.separator;
        } else {
            Log.d("MyLogs", "Base URL crated to head temp_files directory");
            baseUrl = "file://" + CACHE_DIR + File.separator;
        }
        Log.d("MyLogs", "BASE URL: " + baseUrl);
        return baseUrl;
    }

    public void downloadResources(Book book) {
        try {
            Resources resources = book.getResources();
            Collection<Resource> resourceCol = resources.getAll();
            Log.d("MyLogs", "Download files begin");
            for (Resource resource : resourceCol) {
                if (cachedFileFormats.contains(resource.getMediaType())) {
                    Path path = Paths.get(CACHE_DIR, resource.getHref());

                    Path parentDir = path.getParent();
                    if (!Files.exists(parentDir)) {
                        Files.createDirectories(parentDir);
                    }

                    Log.d("MyLogs", resource.getHref() + "\t" + path.toAbsolutePath() + "\t" + resource.getSize());

                    Files.write(path, resource.getData(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                }
            }
            Log.d("MyLogs", "Download files end");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("error", Objects.requireNonNull(e.getMessage()));
        }
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

    public ArrayList<Chapter> getChapterContentAndTitles(Book book) {
        ArrayList<TOCReference> tocReferences = (ArrayList<TOCReference>) book.getTableOfContents().getTocReferences();
        ArrayList<Chapter> chapters = new ArrayList<>();
        if (tocReferences.size() > 1) {
            chapters = getChapterContentAndTitlesViaToc(tocReferences);
        } else {
            Spine spine = book.getSpine();
            chapters = getChapterContentAndTitlesViaSpine(spine);
        }
        return chapters;
    }

    private ArrayList<Chapter> getChapterContentAndTitlesViaToc(ArrayList<TOCReference> tocReferences) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        for (TOCReference TocReference : tocReferences) {
            String referenceTitle = TocReference.getTitle();
            StringBuilder contentBuilder = new StringBuilder();
            try {
                InputStreamReader contentReader = new InputStreamReader(TocReference.getResource().getInputStream());
                BufferedReader r = new BufferedReader(contentReader);
                String aux = "";
                while ((aux = r.readLine()) != null) {
                    contentBuilder.append(aux);
                    contentBuilder.append('\n');
                }
            } catch (Exception e) {
                Log.d("MyLogs", e.toString());
            }
            String referenceContent = contentBuilder.toString();
            Chapter referenceChapter = new Chapter(referenceTitle, referenceContent);
            chapters.add(referenceChapter);

            ArrayList<TOCReference> referenceChildren = (ArrayList<TOCReference>) TocReference.getChildren();
            if (referenceChildren.size() > 0) {
                ArrayList<Chapter> childChapters = getChapterContentAndTitlesViaToc(referenceChildren);
                chapters.addAll(childChapters);
            }
        }
        return chapters;
    }

    private ArrayList<Chapter> getChapterContentAndTitlesViaSpine(Spine spine) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        for (int i = 0; i < spine.size(); i++) {
            String chapterTitle = spine.getResource(i).getTitle();
            if (chapterTitle == null) {
                chapterTitle = String.valueOf(i);
            }

            StringBuilder contentBuilder = new StringBuilder();
            try {
                InputStreamReader contentReader = new InputStreamReader(spine.getResource(i).getInputStream());
                BufferedReader r = new BufferedReader(contentReader);
                String aux = "";
                while ((aux = r.readLine()) != null) {
                    contentBuilder.append(aux);
                    contentBuilder.append('\n');
                }
            } catch (Exception e) {
                Log.d("MyLogs", e.toString());
            }
            String chapterContent = contentBuilder.toString();

            Chapter spineChapter = new Chapter(chapterTitle, chapterContent);
            chapters.add(spineChapter);
        }
        return chapters;
    }

    private class Chapter {
        public String title;
        public String content;

        Chapter(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }

    public String[] getChapterTitles() {
        String[] chapterTitles = new String[chapters.size()];
        for (int i = 0; i < chapters.size(); i++) {
            chapterTitles[i] = chapters.get(i).title;
        }
        return chapterTitles;
    }

    // TEMP METHODS

    public int getChapterAmount() {
        return chapters.size();
    }

    public String getChapterContent(int chapterNo) {
        return chapters.get(chapterNo).content;
    }

    public void setChapters(ArrayList<Chapter> chapters) {
        this.chapters = chapters;
    }

    public boolean selectedTextTooLong(String selectedText) {
        return selectedText.length() > SELECTED_TEXT_MAX_CHARS;
    }

    public int getSelectedTextMaxChars() {
        return SELECTED_TEXT_MAX_CHARS;
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
        if (currentChapter < getChapterAmount() - 1) {
            currentChapter++;
            chapterChanged = true;
        } else {
            chapterChanged = false;
        }
    }

    public void setCurrentChapter(int currentChapter) {
        if (currentChapter >= 0 && currentChapter < getChapterAmount()) {
            this.currentChapter = currentChapter;
            chapterChanged = true;
        } else {
            chapterChanged = false;
        }
    }

    public boolean chapterChanged() {
        return chapterChanged;
    }

}
