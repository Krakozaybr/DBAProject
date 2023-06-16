package com.example.dbaproject.utils;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

public class PreferenceManager {

    private static final String PREFERENCES = "PREFERENCES";
    private static final String EMAIL = "EMAIL";
    private static final String PASSWORD = "PASSWORD";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String REFRESH_TOKEN = "REFRESH_TOKEN";

    // TODO: We need to do something with this
    private static final String KEY = "77345D763FD65D8E54A278A87BC47";

    private SharedPreferences preferences;

    public PreferenceManager(AppCompatActivity context) {
        try {
            this.preferences = EncryptedSharedPreferences.create(
                    PREFERENCES,
                    KEY,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void putString(String key, String value){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void putStringSet(String key, Set<String> set){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(key, set);
        editor.apply();
    }

    private Set<String> getStringSet(String key){
        return preferences.getStringSet(key, new HashSet<>());
    }

    private void putIntegerSet(String key, Set<Integer> set){
        HashSet<String> stringSet = new HashSet<>();
        for (Integer i : set){
            stringSet.add(i + "");
        }
        putStringSet(key, stringSet);
    }

    private Set<Integer> getIntegerSet(String key){
        HashSet<Integer> set = new HashSet<>();
        for (String s : getStringSet(key)){
            set.add(Integer.parseInt(s));
        }
        return set;
    }

    private String getString(String key, String defaultValue){
        return preferences.getString(key, defaultValue);
    }

    private boolean containsKey(String key){
        return preferences.contains(key);
    }

    private void putBoolean(String key, boolean value){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defaultValue){
        return preferences.getBoolean(key, defaultValue);
    }

    public void saveAccessToken(String accessToken){
        putString(ACCESS_TOKEN, accessToken);
    }

    public String getAccessToken(){
        return getString(ACCESS_TOKEN, "");
    }

    public void saveRefreshToken(String refreshToken){
        putString(REFRESH_TOKEN, refreshToken);
    }

    public String getRefreshToken(){
        return getString(REFRESH_TOKEN, "");
    }

}