package com.example.readerapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.epub.EpubReader;

public class FilesRecyclerViewAdapter extends RecyclerView.Adapter<FilesRecyclerViewAdapter.ViewHolder> {

    private ArrayList<ReadableFileDetails> readableFileDetails = new ArrayList<>();

    public FilesRecyclerViewAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.files_list_item,
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

//                ContentResolver contentResolver = context.getContentResolver();

//                try {
//                    InputStream fileStream = contentResolver.openInputStream(fileUri);
//                    Book book = (new EpubReader()).readEpub(fileStream);
//                    Log.d("MyLogs", "author(s): " + book.getMetadata().getAuthors());
//                    Log.d("MyLogs", "title: " + book.getTitle());
//                    List<Resource> contents = book.getContents();
//                    Log.d("MyLogs", "OBTAINED CONTENTS SIZE: " + contents.size());
//                    String contentStringExample = contents.get(0).toString();
//                    Log.d("MyLogs", "RESOURCE CONTENTS: " + contentStringExample);
//
//                    Resources resources = book.getResources();
//                    Collection<Resource> resourceCollection = resources.getAll();
//
//
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }


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
