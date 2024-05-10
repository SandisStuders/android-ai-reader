package com.example.readerapp.data.repositories;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.readerapp.data.models.aiResponse.AiResponse;
import com.example.readerapp.data.models.aiResponse.AiResponseRepository;
import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.services.ChatGptApiService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiConnectionRepository {

    private final String defaultSystemPrompt = "You are an AI assistant integrated into a mobile reading " +
    "application. The user has selected certain text from a document they are reading and sent to " +
    "you as a prompt because they want an explanation on their selection. Interpret the text and " +
    "try to provide factual knowledge surrounding it, avoid speculations and uncertainties if " +
    "possible. If the text includes only one term, provide definition for it. Prompt may include " +
    "the filename as additional context, use it, if it is beneficial. Try to keep your response " +
    "encompassing but reasonably concise.";

    private final String personalPromptSystemPrompt = "You are an AI assistant integrated into a " +
    "mobile reading application. The user has selected certain text from the document they are " +
    "reading and sent to you as a prompt. Their prompt also includes more specific instructions " +
    "on what they'd like to receive in the response. Prompt may include the filename as additional " +
    "context, use it, if it is beneficial. Try to keep your response encompassing but reasonably concise.";

    private final int MAX_GENERATED_TOKENS = 1000;
    private final int SELECTED_TEXT_MAX_CHARS = 2000;

    Application application;
    SharedPreferencesRepository sharedPreferencesRepository;
    AiResponseRepository aiResponseRepository;

    public AiConnectionRepository(Application application) {
        this.application = application;
        sharedPreferencesRepository = new SharedPreferencesRepository(application);
        aiResponseRepository = new AiResponseRepository(application);
    }

    public CompletableFuture<String> obtainAiResponse(String documentQuote, String documentTitle, boolean useDefaultPrompt, ReadableFile readableFile) {
        double temperature = (double) sharedPreferencesRepository.getTemperature() / 100;

        String prompt = constructAiPrompt(documentQuote, documentTitle, useDefaultPrompt);

        if (useDefaultPrompt) {
            return receiveAiResponse(prompt, defaultSystemPrompt, temperature, readableFile);
        } else {
            return receiveAiResponse(prompt, personalPromptSystemPrompt, temperature, readableFile);
        }
    }

    public String constructAiPrompt(String documentQuote, String documentTitle, boolean useDefaultPrompt) {
        String prompt = documentQuote.replaceAll("^\"|\"$", "");

        boolean includeFileName = sharedPreferencesRepository.fileNameIncluded();
        if (includeFileName) {
            prompt = "Document name: " + documentTitle + "; Selected text: " + prompt;
        }

        if (!useDefaultPrompt) {
            String userInstructions = sharedPreferencesRepository.getUsersPersonalPrompt();
            prompt = prompt + "; User's instructions: " + userInstructions;
        }

        return prompt;
    }

    public CompletableFuture<String> receiveAiResponse(String prompt, String systemPrompt, double temperature, ReadableFile readableFile) {
        ChatGptApiService chatGptApiService = new ChatGptApiService();

        CompletableFuture<String> returnableResponse = new CompletableFuture<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            String response = chatGptApiService.processPrompt(systemPrompt, prompt, MAX_GENERATED_TOKENS, temperature);
            returnableResponse.complete(response);

            handler.post(() -> {
                Log.d("MyLogs", "Response: " + response);

                AiResponse aiResponse = new AiResponse(readableFile.getFileName(),
                        readableFile.getRelativePath(),
                        prompt,
                        response,
                        readableFile.getLastOpenChapter());
                aiResponseRepository.insert(aiResponse);


            });
        });

        return returnableResponse;
    }

    public boolean selectedTextTooLong(String selectedText) {
        return selectedText.length() > SELECTED_TEXT_MAX_CHARS;
    }

    public int getSelectedTextMaxChars() {
        return SELECTED_TEXT_MAX_CHARS;
    }

}
