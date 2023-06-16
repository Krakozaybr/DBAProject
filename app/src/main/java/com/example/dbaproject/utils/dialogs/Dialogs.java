package com.example.dbaproject.utils.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dbaproject.R;

public class Dialogs {
    public static void confirmationDialog(AppCompatActivity activity, int message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.confirmation))
                .setMessage(message)
                .setNegativeButton(R.string.no, (dialogInterface, i) -> {})
                .setPositiveButton(R.string.yes, listener)
                .show();
    }
}
