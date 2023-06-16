package com.example.dbaproject.utils.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ErrorDialog extends DialogFragment {

    private String message = null;
    private final String title;

    public ErrorDialog(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public ErrorDialog(String title) {
        this.title = title;
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (message != null){
            builder.setMessage(message);
        }
        return builder.setTitle(title).setPositiveButton("Ok", null).create();
    }
}