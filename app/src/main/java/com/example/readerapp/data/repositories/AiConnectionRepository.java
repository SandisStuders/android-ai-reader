package com.example.readerapp.data.repositories;

import android.app.Application;

import com.example.readerapp.data.services.ChatGptApiService;

import java.util.concurrent.CompletableFuture;

public class AiConnectionRepository {

    private final String defaultSystemPrompt = "You are an assistant designed to help users understand books and documents. When the user sends you a passage, provide an explanation based on the following potential needs:\\n" +
            "1. Define or explain complex terms and concepts.\\n" +
            "2. Provide historical, cultural or geographical background information.\\n" +
            "3. Offer literary analysis, if the passage is from a fictional work and seems rich in themes.\\n" +
            "4. Elaborate on technical details or arguments.\\n" +
            "5. Interpret the author's intent or viewpoint.\\n" +
            "6. Clarify the context in simpler terms, if the passage is of particularly difficult vocabulary and structure.\\n" +
            "Use your judgment to determine the most relevant type of explanation based on the selected passage.\\n" +
            "Don't be too verbose, but get to the point. Avoid over-explaining straightforward parts and focus on meaningful insights or clarifications that could be truly helpful. Avoid uncertain speculations.";

    private final String personalPromptSystemPrompt = "You are an assistant designed to help users understand books and documents. When the user sends you a passage, provide an explanation.\\n" +
            "The user message includes the passage and user's personal instructions on what they wish to receive in answer. Interpret the passage and offer an explanation according to those instructions\\n" +
            "Don't be too verbose, but get to the point. Avoid over-explaining straightforward parts and focus on meaningful insights or clarifications that could be truly helpful. Avoid uncertain speculations.";

    private final String systemPromptFileNameInfo = "\\n User message also includes the document's name. You can utilize it, if it provides useful information.";

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

    public CompletableFuture<String> obtainAiResponse(String documentQuote, String documentTitle, boolean useDefaultPrompt) {
        double temperature = (double) sharedPreferencesRepository.getTemperature() / 100;

        String systemPrompt;
        if (useDefaultPrompt) {
            systemPrompt = defaultSystemPrompt;
        } else {
            systemPrompt = personalPromptSystemPrompt;
        }
        boolean includeFileName = sharedPreferencesRepository.fileNameIncluded();
        if (includeFileName) {
            systemPrompt += systemPromptFileNameInfo;
        }

        String prompt = constructAiPrompt(documentQuote, documentTitle, useDefaultPrompt, includeFileName);

        if (useDefaultPrompt) {
            return receiveAiResponse(prompt, systemPrompt, temperature);
        } else {
            return receiveAiResponse(prompt, systemPrompt, temperature);
        }
    }

    public String constructAiPrompt(String documentQuote, String documentTitle, boolean useDefaultPrompt, boolean includeFileName) {
        String prompt = documentQuote.replaceAll("^\"|\"$", "");

        if (includeFileName) {
            prompt = "Document name: " + documentTitle +
                    "\\nSelected text: " + prompt;
        }

        if (!useDefaultPrompt) {
            String userInstructions = sharedPreferencesRepository.getUsersPersonalPrompt();
            prompt = prompt + "\\nUser's instructions: " + userInstructions;
        }

        return prompt;
    }

    public CompletableFuture<String> receiveAiResponse(String prompt, String systemPrompt, double temperature) {
        ChatGptApiService chatGptApiService = new ChatGptApiService();

        CompletableFuture<String> returnableResponse = new CompletableFuture<>();

        String response = chatGptApiService.processPrompt(systemPrompt, prompt, MAX_GENERATED_TOKENS, temperature);
        returnableResponse.complete(response);

        return returnableResponse;
    }

    public boolean selectedTextTooLong(String selectedText) {
        return selectedText.length() > SELECTED_TEXT_MAX_CHARS;
    }

    public int getSelectedTextMaxChars() {
        return SELECTED_TEXT_MAX_CHARS;
    }

}
