package com.example.readerapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.readerapp.R;
import com.example.readerapp.databinding.ActivityResponseViewerBinding;

public class ResponseViewerActivity extends AppCompatActivity {

    ActivityResponseViewerBinding binding;
    TextView fileNameTextView, selectionTextView, responseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epub_viewer);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_response_viewer);

        fileNameTextView = binding.fileNameTextView;
        selectionTextView = binding.selectionTextView;
        responseTextView = binding.responseTextView;

        Intent intent = getIntent();
        String fileName = intent.getStringExtra("FILENAME");
        String selection = intent.getStringExtra("SELECTION");
        String response = intent.getStringExtra("RESPONSE");

        if (fileName != null) {
            fileNameTextView.setText(fileName);
        }
        if (selection != null) {
            selectionTextView.setText(selection);
        }
        if (response != null) {
            responseTextView.setText(response);
        }
    }

}
