package com.example.readerapp.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readerapp.R;
import com.example.readerapp.data.models.ReadableFileDetails;
import com.example.readerapp.ui.activities.EpubViewerActivity;

import java.util.ArrayList;

public class FilesRecyclerViewAdapter extends RecyclerView.Adapter<FilesRecyclerViewAdapter.ViewHolder> {

    private ArrayList<ReadableFileDetails> readableFileDetails = new ArrayList<>();

    public FilesRecyclerViewAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_files_list,
                parent,
                false);

        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textFileName.setText(readableFileDetails.get(position).getName());
        holder.textFileType.setText(readableFileDetails.get(position).getFileType());
        holder.textFileCreationDate.setText(readableFileDetails.get(position).getCreationDate());
        holder.textFileSize.setText(readableFileDetails.get(position).getFileSize());
        holder.textFileRelativePath.setText(readableFileDetails.get(position).getRelativePath());

        holder.fileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                int position = holder.getAdapterPosition();

                Log.d("MyLogs", "BOOK TITLE: " + readableFileDetails.get(position).getName());
                Uri fileUri = readableFileDetails.get(position).getContentUri();
                String uriString = fileUri.toString();

                Intent intent = new Intent(context, EpubViewerActivity.class);
                intent.putExtra("URI_STRING", uriString);
                intent.putExtra("TEST", "This my testen strinen");
                context.startActivity(intent);

            }
        });

        holder.fileCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                Toast toast = Toast.makeText(v.getContext() , "Long click!", Toast.LENGTH_SHORT);
//                toast.show();
                int position = holder.getAdapterPosition();
                // Initialize the PopupMenu
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                // Inflate the menu from xml
                popup.inflate(R.menu.item_files_list_long_click_menu);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast toastEn = Toast.makeText(v.getContext() , "HERE!!!", Toast.LENGTH_SHORT);
                        toastEn.show();
                        int itemId = item.getItemId();

                        if (itemId == R.id.action_open) {
                            Toast toast = Toast.makeText(v.getContext() , "Open", Toast.LENGTH_SHORT);
                            toast.show();
                        } else if (itemId == R.id.action_add_to_favorites) {
                            Toast toast = Toast.makeText(v.getContext() , "Fav", Toast.LENGTH_SHORT);
                            toast.show();
                        } else if (itemId == R.id.action_remove_from_recent) {
                            Toast toast = Toast.makeText(v.getContext() , "Recent", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        return true;
                    }
                });

                popup.show();
                // launch menu
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
        if (readableFileDetails == null) {
            Log.d("MyLogs", "For some reason readableFileDetails ArrayList is null");
            return 0;
        } else {
            Log.d("MyLogs", "readableFileDetails ArrayList is not null");
            return readableFileDetails.size();
        }
    }

    public void setReadableFileDetails(ArrayList<ReadableFileDetails> readableFileDetails) {
        Log.d("MyLogs", "READABLE FILE DETAILS SET!!");
        this.readableFileDetails = readableFileDetails;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textFileName, textFileType, textFileCreationDate, textFileSize,
                textFileRelativePath;

        private CardView fileCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textFileName = itemView.findViewById(R.id.file_name);
            textFileType = itemView.findViewById(R.id.file_type);
            textFileCreationDate = itemView.findViewById(R.id.file_creation_date);
            textFileSize = itemView.findViewById(R.id.file_size);
            textFileRelativePath = itemView.findViewById(R.id.file_relative_path);
            fileCard = itemView.findViewById(R.id.file_card);
        }
    }

}
