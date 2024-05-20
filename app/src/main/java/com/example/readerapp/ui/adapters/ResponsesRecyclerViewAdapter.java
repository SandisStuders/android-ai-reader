package com.example.readerapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readerapp.R;
import com.example.readerapp.data.models.AiResponse;

import java.util.ArrayList;

public class ResponsesRecyclerViewAdapter extends RecyclerView.Adapter<ResponsesRecyclerViewAdapter.ViewHolder> {

    private ArrayList<AiResponse> aiRespons = new ArrayList<>();
    private ResponseOptionListener listener;

    public ResponsesRecyclerViewAdapter(ResponseOptionListener listener) {
        this.listener = listener;
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
        holder.textFileName.setText(aiRespons.get(position).getReadableFileName());
        holder.textSelectedText.setText(aiRespons.get(position).getSelectedText());
        holder.textGptResponse.setText(aiRespons.get(position).getResponse());

        holder.responseCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                AiResponse aiResponse = aiRespons.get(position);

                listener.openResponse(aiResponse);
            }
        });

        holder.responseCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();

                PopupMenu popup = new PopupMenu(v.getContext(), v);


                popup.inflate(R.menu.item_responses_list_long_click_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();

                        if (itemId == R.id.response_action_open) {
                            listener.openResponse(aiRespons.get(position));
                        } else if (itemId == R.id.response_action_delete) {
                            listener.deleteResponse(aiRespons.get(position));
                        }

                        return true;
                    }
                });

                popup.show();
                return true;
            }

            @Override
            public boolean onLongClickUseDefaultHapticFeedback(@NonNull View v) {
                return View.OnLongClickListener.super.onLongClickUseDefaultHapticFeedback(v);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (aiRespons == null) {
            return 0;
        } else {
            return aiRespons.size();
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
            responseCard = itemView.findViewById(R.id.response_card);
        }
    }

    public void setGptResponses(ArrayList<AiResponse> aiRespons) {
        this.aiRespons = aiRespons;
        notifyDataSetChanged();
    }

    public interface ResponseOptionListener {
        void openResponse(AiResponse aiResponse);
        void deleteResponse(AiResponse aiResponse);
    }
}
