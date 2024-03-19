package com.example.readerapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;

public class ChatGptApi {

    private final String apiKey;
    private final String apiEndpoint;
    private String model;

    ChatGptApi() {
        apiEndpoint = "https://api.openai.com/v1/chat/completions";
        apiKey = "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";
        model = "gpt-3.5-turbo";
    }

    public String processPrompt(String prompt) {
        try {
            URI uri = new URI(apiEndpoint);
            URL urlObj = uri.toURL();

            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            // The request body
            String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}";
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(body);
            writer.flush();
            writer.close();

            // Response from ChatGPT
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;

            StringBuilder response = new StringBuilder();

            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            return extractMessageFromJSONResponse(response.toString());

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static String extractMessageFromJSONResponse(String response) {
        int start = response.indexOf("content")+ 11;

        int end = response.indexOf("\"", start);

        return response.substring(start, end);

    }

}
