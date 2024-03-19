package com.example.readerapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ReaderView extends WebView {

    MenuItem explainItem;
    MenuItem copyItem;

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
        Log.d("MyLogs", "Action mode started!");
        ActionMode.Callback customCallback = forwardingCallback(callback);

        return super.startActionMode(customCallback);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        Log.d("MyLogs", "Action mode started!");
        ActionMode.Callback customCallback = forwardingCallback(callback);

        return super.startActionMode(customCallback, type);
    }

    private ActionMode.Callback forwardingCallback(final ActionMode.Callback callback) {
        return new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                boolean result = callback.onCreateActionMode(mode, menu);

                return result;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                boolean result = callback.onPrepareActionMode(mode, menu);

                menu.clear();
                explainItem = menu.add("Explain");
                copyItem = menu.add("Copy");

                return result;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                evaluateJavascript("(function(){return window.getSelection().toString();})()", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if (item == explainItem) {
                            actionExplain(value);
                        } else if (item == copyItem) {
                            actionCopy(value);
                        }
                    }
                });

                return callback.onActionItemClicked(mode, item);
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                callback.onDestroyActionMode(mode);
            }
        };
    }

    private void actionExplain(String value) {
        String processedValue = value.replaceAll("^\"|\"$", "");
        ChatGptApi chatGptApi = new ChatGptApi();
        String prompt = "Please provide a concise explanation of the below excerpt from a book: " + processedValue;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Log.d("MyLogs", prompt);
            String response = chatGptApi.processPrompt(prompt);

            handler.post(() -> {
                Context context = getContext();
                Intent intent = new Intent(context, ResponseViewerActivity.class);
                intent.putExtra("SELECTION", processedValue);
                intent.putExtra("RESPONSE", response);
                context.startActivity(intent);
            });
        });

    }

    private void actionCopy(String value) {
        String processedValue = value.replaceAll("^\"|\"$", "");
        //TODO: JavaScript copies string as JSON string therefore escape characters possible. These should be escaped

        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("default", processedValue);
        clipboardManager.setPrimaryClip(clip);
    }

}
