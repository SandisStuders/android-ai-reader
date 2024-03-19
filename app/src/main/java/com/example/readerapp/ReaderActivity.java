package com.example.readerapp;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.readerapp.databinding.ActivityReaderBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReaderActivity extends AppCompatActivity {

    ActivityReaderBinding binding;
    WebView viewerTester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_reader);
        viewerTester = binding.web;

        String htmlContent = loadHtmlFromFile("TestHtml.html");

        Log.d("MyLogs", htmlContent);

        viewerTester.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);

    }

    private String loadHtmlFromFile(String filename) {
        StringBuilder content = new StringBuilder();
        try {
            // Open an input stream to read the file
            InputStream inputStream = getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                Log.d("MyLogs", "Line isn't null");
                content.append(line);
                content.append('\n'); // Preserve line breaks
            }
            Log.d("MyLogs", "Line is null now");
            reader.close();
        } catch (IOException e) {
            e.printStackTrace(); // Handle exceptions or throw as needed
            Log.d("MyLogs", e.toString());
        }
        return content.toString();
    }

}
