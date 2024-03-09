package com.example.readerapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.readerapp.databinding.FragmentReaderBinding;
import com.github.barteksc.pdfviewer.PDFView;

public class ReaderFragment extends Fragment {

    private FragmentReaderBinding binding;

    PDFView pdfViewer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReaderBinding.inflate(inflater, container, false);

        Intent intent = getActivity().getIntent();

        String pdfUriString = intent.getStringExtra("PDF_URI");
        Uri uri = Uri.parse(pdfUriString);;
        pdfViewer = binding.pdfView;
        pdfViewer.fromUri(uri).load();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
