package com.example.readerapp.data.services;

import android.util.Log;

import com.example.readerapp.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import org.json.JSONException;
import org.json.JSONObject;


public class ChatGptApiService {

    private final String apiEndpoint = "https://api.openai.com/v1/chat/completions";
    private final String apiKey = BuildConfig.CHATGPT_API_KEY;
    private final String model = "gpt-3.5-turbo";

    public ChatGptApiService() {

    }

    public String processPrompt(String systemPrompt, String prompt, int maxTokens, Double temperature) {
        try {
            URI uri = new URI(apiEndpoint);
            URL urlObj = uri.toURL();

            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            // The request body
            String body = String.format(
                    "{\"model\": \"%s\", " +
                            "\"messages\": [{\"role\": \"system\", \"content\": \"%s\"}, {\"role\": \"user\", \"content\": \"%s\"}], " +
                            "\"max_tokens\": %s, " +
                            "\"temperature\": %s}",
                    model, systemPrompt, prompt, maxTokens, temperature
            );
            Log.d("MyLogs", "Raw request: " + body);

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

            Log.d("MyLogs", "Raw response: " + response);

            return extractMessageFromJSONResponse(response.toString());

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String extractMessageFromJSONResponse(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            String content = obj.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            Log.d("MyLogs", content);
            return content;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}
