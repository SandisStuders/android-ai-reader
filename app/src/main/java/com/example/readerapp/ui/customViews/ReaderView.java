package com.example.readerapp.ui.customViews;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.example.readerapp.data.services.ChatGptApiService;
import com.example.readerapp.ui.activities.ResponseViewerActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ReaderView extends WebView {

    MenuItem explainItem;
    MenuItem copyItem;
    MenuItem personalPromptItem;
    private ActionModeCallback actionModeCallback;

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
        ActionMode.Callback customCallback = forwardingCallback(callback);

        return super.startActionMode(customCallback);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
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

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean personalPromptEnabled = sharedPreferences.getBoolean("personal_prompt_enable", false);
                if (personalPromptEnabled) {
                    String personalPromptName = sharedPreferences.getString("personal_prompt_name", "Personal Prompt");
                    personalPromptItem = menu.add(personalPromptName);
                }

                copyItem = menu.add("Copy");

                return result;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                evaluateJavascript("(function(){return window.getSelection().toString();})()", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if (item == explainItem) {
                            if (actionModeCallback != null) {
                                actionModeCallback.onTextSelected(value, true);
                            }
                        } else if (item == copyItem) {
                            actionCopy(value);
                        } else if (item == personalPromptItem) {
                            if (actionModeCallback != null) {
                                actionModeCallback.onTextSelected(value, false);
                            }
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

    private void actionCopy(String value) {
        String processedValue = value.replaceAll("^\"|\"$", "");
        //TODO: JavaScript copies string as JSON string therefore escape characters possible. These should be escaped

        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("default", processedValue);
        clipboardManager.setPrimaryClip(clip);
    }

    public void setActionModeCallback(ActionModeCallback callback) {
        this.actionModeCallback = callback;
    }

    public interface ActionModeCallback {
        void onTextSelected(String selectedText, boolean useDefaultSystemPrompt);
    }

}
