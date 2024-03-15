package com.example.readerapp;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class ReaderView extends WebView {

    public ReaderView(@NonNull Context context) {
        super(context);
    }

    public ReaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReaderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ReaderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        Log.d("MyLogs", "Action mode started! No type");
        ActionMode actionMode = super.startActionMode(callback);


        return actionMode;
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        Log.d("MyLogs", "Action mode started! With type");
        ActionMode actionMode = super.startActionMode(callback, type);
        Menu menu = actionMode.getMenu();
        Log.d("MyLogs", "Menu item 0: " + menu.getItem(0));

        return actionMode;
    }


}
