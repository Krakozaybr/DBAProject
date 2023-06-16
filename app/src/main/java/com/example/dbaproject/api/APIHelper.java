package com.example.dbaproject.api;

import android.util.Log;

import com.example.dbaproject.utils.DateTimeUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Singleton made for comfortable sing retrofit2
public class APIHelper {
    private static APIHelper instance = null;
    private static final String STORAGE_URL = "http://10.0.2.2:8000/";
    private static final String PROCESSING_URL = "https://side-test.dba.ooo/";
    private APIWrapper storageAPI;
    private ProcessingAPI processingAPI;

    private APIHelper(){
        Gson gson = new GsonBuilder()
                .setDateFormat(DateTimeUtils.DATE_TIME_FORMAT)
                .create();

        // It is important because we need to wait some time to get response from processing server
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(STORAGE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        storageAPI = new APIWrapper(retrofit.create(StorageAPI.class));

        retrofit = new Retrofit.Builder()
                .baseUrl(PROCESSING_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();

        processingAPI = retrofit.create(ProcessingAPI.class);
    }

    public static APIHelper getInstance(){
        if (instance == null){
            instance = new APIHelper();
        }
        return instance;
    }

    public APIWrapper getStorageAPI() {
        return storageAPI;
    }

    public ProcessingAPI getProcessingAPI() {
        return processingAPI;
    }
}
