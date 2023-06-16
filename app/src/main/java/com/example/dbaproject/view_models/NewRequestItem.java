package com.example.dbaproject.view_models;

import android.net.Uri;

public class NewRequestItem {
    private final String defaultName;
    private final Uri imageUri;
    private String name;

    public NewRequestItem(Uri imageUri, String defaultName) {
        this.imageUri = imageUri;
        this.defaultName = defaultName;
        this.name = "";
    }

    public Uri getUri() {
        return imageUri;
    }

    public String getPath() {
        return imageUri.getPath();
    }

    public String getDefaultName() {
        return defaultName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRealName(){
        return name.isEmpty() ? defaultName : name;
    }
}
