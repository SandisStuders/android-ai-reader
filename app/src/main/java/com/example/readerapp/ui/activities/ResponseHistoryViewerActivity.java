package com.example.readerapp.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readerapp.R;
import com.example.readerapp.data.models.gptResponse.GptResponse;
import com.example.readerapp.data.models.gptResponse.GptResponseViewModel;
import com.example.readerapp.data.models.readableFile.ReadableFile;
import com.example.readerapp.data.models.readableFile.ReadableFileViewModel;
import com.example.readerapp.databinding.ActivityResponseHistoryViewerBinding;
import com.example.readerapp.ui.adapters.FilesRecyclerViewAdapter;
import com.example.readerapp.ui.adapters.ResponsesRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResponseHistoryViewerActivity extends AppCompatActivity implements ResponsesRecyclerViewAdapter.ResponseOptionListener {

    ActivityResponseHistoryViewerBinding binding;
    private RecyclerView responsesRecyclerView;
    private GptResponseViewModel mGptResponseViewModel;
    private ArrayList<GptResponse> gptResponses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MyLogs", "Response history activity started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response_history_viewer);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_response_history_viewer);
        responsesRecyclerView = binding.responsesRecyclerView;

        mGptResponseViewModel = new ViewModelProvider(this).get(GptResponseViewModel.class);

        ResponsesRecyclerViewAdapter adapter = new ResponsesRecyclerViewAdapter(this);
        adapter.setGptResponses(gptResponses);

        responsesRecyclerView.setAdapter(adapter);
        responsesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mGptResponseViewModel.getAllGptResponses().observe(this, new Observer<List<GptResponse>>() {
            @Override
            public void onChanged(List<GptResponse> gptResponses) {
                Log.d("MyLogs", "New list of GPT Responses obtained from database");
                if (gptResponses != null) {
                    Log.d("MyLogs", "List is not null. Size: " + gptResponses.size());
                    adapter.setGptResponses((ArrayList<GptResponse>) gptResponses);
                }
            }
        });
    }

    @Override
    public void openResponse(GptResponse gptResponse) {
        Context context = this;
        Intent intent = new Intent(context, ResponseViewerActivity.class);
        intent.putExtra("SELECTION", gptResponse.getSelectedText());
        intent.putExtra("RESPONSE", gptResponse.getResponse());
        context.startActivity(intent);
    }

    @Override
    public void deleteResponse(GptResponse gptResponse) {
        mGptResponseViewModel.delete(gptResponse);
    }
}
