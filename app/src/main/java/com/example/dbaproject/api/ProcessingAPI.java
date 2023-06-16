package com.example.dbaproject.api;

import com.example.dbaproject.api.models.processed_data.ProcessingResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// Interaction with processing server
public interface ProcessingAPI {

    @POST("/predictions/detection")
    Call<ProcessingResponse> detect(@Body RequestBody requestBody);
}
