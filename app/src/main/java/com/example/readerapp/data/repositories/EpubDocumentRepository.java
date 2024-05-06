package com.example.readerapp.data.repositories;

import android.app.Application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.service.MediatypeService;

public class EpubDocumentRepository {

    Application application;
    ExternalStorageRepository externalStorageRepository;
    InternalStorageRepository internalStorageRepository;
    Book epubBook;

    private final Set<MediaType> cachedFileFormats = new HashSet<>(Arrays.asList(
            MediatypeService.CSS,
            MediatypeService.PNG,
            MediatypeService.GIF,
            MediatypeService.JPG,
            MediatypeService.SVG
    ));

    public EpubDocumentRepository(Application application) {
        this.application = application;
        this.externalStorageRepository = new ExternalStorageRepository(application);
        this.internalStorageRepository = new InternalStorageRepository(application);

    }

    public ArrayList<Chapter> initializeBookWithUriString(String uriString) {
        epubBook = externalStorageRepository.getEpubWithUriString(uriString);
        downloadBookContentToCache();
        return getChapterContentAndTitles();
    }

    public void downloadBookContentToCache() {
        internalStorageRepository.emptyCache();

        if (epubBook == null) {
            return;
        }

        try {
            Resources resources = epubBook.getResources();
            Collection<Resource> resourceCollection = resources.getAll();
            for (Resource resource : resourceCollection) {
                if (cachedFileFormats.contains(resource.getMediaType())) {
                    byte[] resourceByteArray = resource.getData();
                    String resourceRelativePath = resource.getHref();

                    internalStorageRepository.writeByteArrayToCache(resourceByteArray, resourceRelativePath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Chapter> getChapterContentAndTitles() {
        ArrayList<TOCReference> tocReferences = (ArrayList<TOCReference>) epubBook.getTableOfContents().getTocReferences();
        ArrayList<Chapter> chapters;
        if (tocReferences.size() > 0) {
            chapters = getChapterContentAndTitlesViaToc(tocReferences);
        } else {
            Spine spine = epubBook.getSpine();
            chapters = getChapterContentAndTitlesViaSpine(spine);
        }
        return chapters;
    }

    private ArrayList<Chapter> getChapterContentAndTitlesViaToc(ArrayList<TOCReference> tocReferences) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        for (TOCReference TocReference : tocReferences) {
            String chapterTitle = TocReference.getTitle();
            String chapterContent = externalStorageRepository.getEpubResourceContent(TocReference.getResource());

            Chapter referenceChapter = new Chapter(chapterTitle, chapterContent);
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
            String chapterContent = externalStorageRepository.getEpubResourceContent(spine.getResource(i));

            Chapter spineChapter = new Chapter(chapterTitle, chapterContent);
            chapters.add(spineChapter);
        }
        return chapters;
    }

    public static class Chapter {
        public String title;
        public String content;

        Chapter(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }

    public String findBookContentBaseUrl() {
        String oebpsSubdirectory = "OEBPS";
        String opfSubdirectory = epubBook.getOpfResource().getHref().replace("content.opf", "").replace("/", "");

        if (internalStorageRepository.directoryExistsInCache(oebpsSubdirectory)) {
            return internalStorageRepository.getCacheDirectoryPath(oebpsSubdirectory);
        } else if (!opfSubdirectory.equals("") && internalStorageRepository.directoryExistsInCache(opfSubdirectory)) {
            return internalStorageRepository.getCacheDirectoryPath(opfSubdirectory);
        } else {
            return internalStorageRepository.getCacheDirectoryPath();
        }
    }
}
