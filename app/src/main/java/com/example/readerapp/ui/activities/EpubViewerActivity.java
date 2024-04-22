package com.example.readerapp.ui.activities;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.readerapp.R;
import com.example.readerapp.data.models.gptResponse.GptResponse;
import com.example.readerapp.data.models.gptResponse.GptResponseViewModel;
import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.models.readableFile.ReadableFileViewModel;
import com.example.readerapp.data.services.ChatGptApiService;
import com.example.readerapp.databinding.ActivityEpubViewerBinding;
import com.example.readerapp.ui.customViews.ReaderView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.service.MediatypeService;

public class EpubViewerActivity extends AppCompatActivity implements ReaderView.ActionModeCallback {

    ActivityEpubViewerBinding binding;
    ReaderView epubViewer;
    BottomNavigationView bottomAppBar;
    int currentChapter;
    ArrayList<Chapter> chapters;
    ReadableFile sourceFile;
    private GptResponseViewModel mGptResponseViewModel;
    private ReadableFileViewModel mReadableFileViewModel;
    Context context;
    private String baseUrl;
    private String CACHE_DIR;

    private final Set<MediaType> cachedFileFormats = new HashSet<>(Arrays.asList(
            MediatypeService.CSS,
            MediatypeService.PNG,
            MediatypeService.GIF,
            MediatypeService.JPG,
            MediatypeService.SVG
    ));

    private final int SELECTED_TEXT_MAX_CHARS = 600;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epub_viewer);
        context = this;

        CACHE_DIR = context.getCacheDir() + "/temp_files";

        mGptResponseViewModel = new ViewModelProvider(this).get(GptResponseViewModel.class);
        mReadableFileViewModel = new ViewModelProvider(this).get(ReadableFileViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_epub_viewer);
        epubViewer = binding.epubViewer;
        bottomAppBar = binding.bottomAppBar;

        epubViewer.setActionModeCallback(this);

        if (savedInstanceState == null) {
            currentChapter = 0;
        } else {
            currentChapter = savedInstanceState.getInt("CURRENT_CHAPTER", 0);
        }

        WebSettings webSettings = epubViewer.getSettings();
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptEnabled(true);

        Intent intent = getIntent();

        String fileName = intent.getStringExtra("FILE_NAME");
        String fileRelativePath = intent.getStringExtra("FILE_RELATIVE_PATH");
        Log.d("MyLogs", "GOT INTENT. FILE NAME: " + fileName + " ; RELATIVE PATH: " + fileRelativePath);

        AtomicReference<Boolean> firstFileObservation = new AtomicReference<>(true);
        mReadableFileViewModel.getReadableFileByPrimaryKey(fileName, fileRelativePath).observe(this, readableFile -> {
            if (readableFile != null && firstFileObservation.get()) {
                currentChapter = readableFile.getLastOpenChapter();
                sourceFile = readableFile;
                loadCurrentChapter("primary");
                firstFileObservation.set(false);
            }
        });

        String uriString = intent.getStringExtra("URI_STRING");
        Uri uri = Uri.parse(uriString);

        ContentResolver contentResolver = getContentResolver();
        try {
            InputStream fileStream = contentResolver.openInputStream(uri);
            Book book = (new EpubReader()).readEpub(fileStream);

            emptyCache();
            downloadResources(book);
            this.baseUrl = findBaseUrl(book);

            this.chapters = getChapterContentAndTitles(book);
            loadCurrentChapter("onCreate content resolver");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        bottomAppBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                Log.d("MyLogs", "ITEM PRESSED!");

                if (itemId == R.id.prevChapter) {
                    if (currentChapter > 0) {
                        currentChapter--;
                        loadCurrentChapter("navigation prev chapt");
                    }
                    return true;
                } else if (itemId == R.id.selectChapter) {
                    String[] chapterTitles = new String[chapters.size()];
                    for (int i = 0; i < chapters.size(); i++) {
                        chapterTitles[i] = chapters.get(i).title;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Select a Chapter");

                    builder.setItems(chapterTitles, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currentChapter = which;
                            loadCurrentChapter("navigation select chapt");
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return true;
                } else if (itemId == R.id.nextChapter) {
                    Log.d("MyLogs", "Next item button pressed!");
                    if (currentChapter < chapters.size() - 1) {
                        currentChapter++;
                        loadCurrentChapter("navigation next chapt");
                    }
                    return true;
                }

                return false;
            }
        });

    }

    private String findBaseUrl(Book book) {
        String baseUrl;

        String oebpsDirectory = CACHE_DIR + File.separator + "OEBPS";
        String opfSubdirectory = book.getOpfResource().getHref().replace("content.opf","").replace("/","");
        String opfDirectory = CACHE_DIR + File.separator + opfSubdirectory;

        File oebpsFile = new File(oebpsDirectory);
        File opfFile = new File(opfDirectory);

        if (oebpsFile.exists() && oebpsFile.isDirectory()) {
            Log.d("MyLogs", "Base URL crated to OEBPS");
            baseUrl = "file://" + oebpsDirectory + File.separator;
        } else if(opfFile.exists() && opfFile.isDirectory() && !opfSubdirectory.equals("")){
            Log.d("MyLogs", "Base URL crated to resource folder");
            baseUrl = "file://" + opfDirectory + File.separator;
        } else {
            Log.d("MyLogs", "Base URL crated to head temp_files directory");
            baseUrl = "file://" + CACHE_DIR + File.separator;
        }
        Log.d("MyLogs", "BASE URL: " + baseUrl);
        return baseUrl;
    }

    private void downloadResources(Book book) {
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
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e("error", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void emptyCache (){
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

    private ArrayList<Chapter> getChapterContentAndTitles(Book book) {
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
            }
            catch(Exception e) {
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
        for (int i = 0; i < spine.size(); i++){
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
            } catch(Exception e) {
                Log.d("MyLogs", e.toString());
            }
            String chapterContent = contentBuilder.toString();

            Chapter spineChapter = new Chapter(chapterTitle, chapterContent);
            chapters.add(spineChapter);
        }
        return chapters;
    }

    private void loadCurrentChapter(String source) {
        Log.d("MyLogs", "Loading current chapter! Source: " + source);
        if (sourceFile != null) {
            ReadableFile readableFile = sourceFile;
            readableFile.setLastOpenChapter(currentChapter);
            mReadableFileViewModel.update(readableFile);
            Log.d("MyLogs", "UPDATED THAT FILE BRAH CHAPTER: " + currentChapter);
        }
        String dataPiece = chapters.get(currentChapter).content;
        dataPiece = dataPiece.replaceAll("href=\"http", "hreflink=\"http").replaceAll("<a href=\"[^\"]*", "<a ").replaceAll("hreflink=\"http", "href=\"http");

        Log.d("MyLogs", dataPiece);

        epubViewer.loadDataWithBaseURL(baseUrl,
                dataPiece,
                "text/html",
                "UTF-8",
                null);
    }

    @Override
    public void onTextSelected(String selectedText, boolean useDefaultSystemPrompt) {
        if (selectedText.length() > SELECTED_TEXT_MAX_CHARS) {
            String alertTitle = getString(R.string.document_selection_alert_title);
            String alertText1 = getString(R.string.document_selection_alert_text_1);
            String alertText2 = getString(R.string.document_selection_alert_text_2);
            String alertButtonOk = getString(R.string.alert_button_ok);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(alertText1 + SELECTED_TEXT_MAX_CHARS + alertText2)
                    .setTitle(alertTitle);

            builder.setPositiveButton(alertButtonOk, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this); // 'context' refers to the Activity or Context object
        int temperaturePercentage = sharedPreferences.getInt("temperature", 40);

        String prompt = selectedText.replaceAll("^\"|\"$", "");
        boolean includeFileName = sharedPreferences.getBoolean("send_file_name", false);
        if (includeFileName && sourceFile != null) {
            prompt = "File name: " + sourceFile.getFileName() + "; Selected text: " + prompt;
        }

        String systemPrompt = "";
        if (useDefaultSystemPrompt) {
            systemPrompt = "You are an AI assistant integrated into a mobile reading application. The user has selected certain text from the document they are reading and sent to you as a prompt because they want an explanation on their selection. Interpret the text and try to provide factual knowledge surrounding it, avoid speculations and uncertainties if possible. If the text includes only one term, provide definition for it. Prompt may include the filename as additional context, use it, if it is beneficial. Try to keep your response encompassing but reasonably concise.";
        } else {
            systemPrompt = "You are an AI assistant integrated into a mobile reading application. The user has selected certain text from the document they are reading and sent to you as a prompt. Their prompt also includes more specific instructions on what they'd like to receive in the response. Prompt may include the filename as additional context, use it, if it is beneficial. Try to keep your response encompassing but reasonably concise.";
            String userInstructions = sharedPreferences.getString("personal_prompt_define", "");
            prompt = prompt + "; User's instructions: " + userInstructions;
        }

        double temperature = ((double) temperaturePercentage) / 100;

        ChatGptApiService chatGptApiService = new ChatGptApiService();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        String finalSystemPrompt = systemPrompt;
        String finalPrompt = prompt;
        executor.execute(() -> {
            Log.d("MyLogs", finalPrompt);
            String response = chatGptApiService.processPrompt(finalSystemPrompt, finalPrompt, temperature);
            String fileName;
            String fileRelativePath;
            if (sourceFile != null) {
                fileName = sourceFile.getFileName();
                fileRelativePath = sourceFile.getRelativePath();
            } else {
                fileRelativePath = "";
                fileName = "";
            }

            handler.post(() -> {
                Log.d("MyLogs", "Response: " + response);

                GptResponse gptResponse = new GptResponse(fileName,
                        fileRelativePath,
                        selectedText,
                        response,
                        currentChapter);
                mGptResponseViewModel.insert(gptResponse);

                Context context = this;
                Intent intent = new Intent(context, ResponseViewerActivity.class);
                intent.putExtra("SELECTION", selectedText);
                intent.putExtra("RESPONSE", response);
                intent.putExtra("FILENAME", fileName);
                context.startActivity(intent);
            });
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CURRENT_CHAPTER", currentChapter);
    }

    private class Chapter {
        public String title;
        public String content;

        Chapter(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }
}
