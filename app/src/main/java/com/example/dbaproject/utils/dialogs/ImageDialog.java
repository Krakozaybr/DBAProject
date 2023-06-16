package com.example.dbaproject.utils.dialogs;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dbaproject.databinding.ImageDialogBinding;

public class ImageDialog extends Dialog {

    private AppCompatActivity activity;
    private Bitmap bmp;
    private ImageDialogBinding binding;

    public ImageDialog(AppCompatActivity activity, Bitmap bmp) {
        super(activity);
        this.activity = activity;
        this.bmp = bmp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = ImageDialogBinding.inflate(LayoutInflater.from(activity));

        addListeners();
        binding.touchImageView.setImageDrawable(new BitmapDrawable(activity.getResources(), bmp));

        setContentView(binding.getRoot());
    }

    private void addListeners(){
        binding.closeButton.setOnClickListener((view) -> dismiss());
    }
}
