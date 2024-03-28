package com.example.readerapp.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readerapp.R;
import com.example.readerapp.data.models.ReadableFile;
import com.example.readerapp.ui.activities.EpubViewerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class FilesRecyclerViewAdapter extends RecyclerView.Adapter<FilesRecyclerViewAdapter.ViewHolder> {

    private ArrayList<ReadableFile> readableFileDetails = new ArrayList<>();
    private String currentListType = "RECENT";
    private FileOptionListener listener;


    public FilesRecyclerViewAdapter(FileOptionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_files_list,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textFileName.setText(readableFileDetails.get(position).getFileName());
        holder.textFileType.setText(readableFileDetails.get(position).getFileType());
        holder.textFileCreationDate.setText(readableFileDetails.get(position).getCreationDate());
        holder.textFileSize.setText(readableFileDetails.get(position).getFileSize());
        holder.textFileRelativePath.setText(readableFileDetails.get(position).getRelativePath());

        String fileType = readableFileDetails.get(position).getFileType();
        if (Objects.equals(fileType, "EPUB")) {
            holder.thumbnail.setImageResource(R.drawable.epub_default_thumbnail);
        } else if (Objects.equals(fileType, "PDF")) {
            holder.thumbnail.setImageResource(R.drawable.pdf_default_thumbnail);
        } else {
            holder.thumbnail.setImageResource(R.drawable.default_thumbnail);
        }

        holder.fileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                ReadableFile readableFile = readableFileDetails.get(position);

                listener.fileOpened(readableFile, v);
            }
        });

        holder.fileCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();

                PopupMenu popup = new PopupMenu(v.getContext(), v);

                if (Objects.equals(currentListType, "RECENT")) {
                    popup.inflate(R.menu.item_files_list_long_click_menu_recent);
                } else if (Objects.equals(currentListType, "FAVORITE")) {
                    popup.inflate(R.menu.item_files_list_long_click_menu_favorite);
                } else {
                    popup.inflate(R.menu.item_files_list_long_click_menu_all);
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();

                        if (itemId == R.id.action_open) {
                            listener.fileOpened(readableFileDetails.get(position), v);
                        } else if (itemId == R.id.action_add_to_favorites) {
                            listener.addToFavorites(readableFileDetails.get(position));
                        } else if (itemId == R.id.action_remove_from_recent) {
                            listener.removeFromRecent(readableFileDetails.get(position));
                        } else if (itemId == R.id.action_remove_from_favorites) {
                            listener.removeFromFavorites(readableFileDetails.get(position));
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
        if (readableFileDetails == null) {
            return 0;
        } else {;
            return readableFileDetails.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textFileName, textFileType, textFileCreationDate, textFileSize,
                textFileRelativePath;

        private ImageView thumbnail;

        private CardView fileCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textFileName = itemView.findViewById(R.id.file_name);
            textFileType = itemView.findViewById(R.id.file_type);
            textFileCreationDate = itemView.findViewById(R.id.file_creation_date);
            textFileSize = itemView.findViewById(R.id.file_size);
            textFileRelativePath = itemView.findViewById(R.id.file_relative_path);
            fileCard = itemView.findViewById(R.id.file_card);
            thumbnail = itemView.findViewById(R.id.image_thumbnail);
        }
    }

    public void setReadableFileDetails(ArrayList<ReadableFile> readableFileDetails) {
        this.readableFileDetails = readableFileDetails;
        notifyDataSetChanged();
    }

    public String getCurrentListType() {
        return currentListType;
    }

    public void setCurrentListType(String currentListType) {
        this.currentListType = currentListType;
    }

    public interface FileOptionListener {
        void addToFavorites(ReadableFile readableFile);
        void removeFromRecent(ReadableFile readableFile);
        void removeFromFavorites(ReadableFile readableFile);
        void fileOpened(ReadableFile readableFile, View v);
    }
}
