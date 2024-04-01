package com.example.readerapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readerapp.R;
import com.example.readerapp.data.models.gptResponse.GptResponse;
import com.example.readerapp.data.models.readableFile.ReadableFile;

import java.util.ArrayList;

public class ResponsesRecyclerViewAdapter extends RecyclerView.Adapter<ResponsesRecyclerViewAdapter.ViewHolder> {

    private ArrayList<GptResponse> gptResponses = new ArrayList<>();

    public ResponsesRecyclerViewAdapter() {

    }

    @NonNull
    @Override
    public ResponsesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_responses_list,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResponsesRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.textFileName.setText(gptResponses.get(position).getSourceReadableFile());
        holder.textSelectedText.setText(gptResponses.get(position).getSelectedText());
        holder.textGptResponse.setText(gptResponses.get(position).getResponse());
    }

    @Override
    public int getItemCount() {
        if (gptResponses == null) {
            return 0;
        } else {
            return gptResponses.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textFileName, textSelectedText, textGptResponse;

        private CardView responseCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textFileName = itemView.findViewById(R.id.response_file_name);
            textSelectedText = itemView.findViewById(R.id.response_selected_text);
            textGptResponse = itemView.findViewById(R.id.gpt_response);
        }
    }

    public void setGptResponses(ArrayList<GptResponse> gptResponses) {
        this.gptResponses = gptResponses;
        notifyDataSetChanged();
    }
}
