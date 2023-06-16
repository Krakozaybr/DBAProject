package com.example.dbaproject.api;

import com.example.dbaproject.api.models.AccessToken;
import com.example.dbaproject.api.models.Source;
import com.example.dbaproject.api.models.UserAuthResp;
import com.example.dbaproject.api.models.processed_data.ProcessedDataCreate;
import com.example.dbaproject.api.models.processed_data.ProcessedDataResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

// Interaction with backend
public interface StorageAPI {

    @POST("/api/users/login/")
    @FormUrlEncoded
    Call<UserAuthResp> login(@Field("username") String username,
                             @Field("password") String password
    );

    @POST("/api/users/signup/")
    @FormUrlEncoded
    Call<UserAuthResp> register(@Field("username") String username,
                                @Field("password") String password,
                                @Field("email") String email
    );
    
    @POST("/api/storage/create/")
    Call<ProcessedDataResponse> addData(@Body ProcessedDataCreate processedData,
                                        @Header("Authorization") String token
    );

    @DELETE("/api/storage/{data_id}/")
    Call<Void> deleteData(@Path(value = "data_id", encoded = true) String dataId,
                                        @Header("Authorization") String token
    );

    @GET("/api/storage/{data_id}/")
    Call<ProcessedDataResponse> getData(@Path(value = "data_id", encoded = true) String dataId,
                                        @Header("Authorization") String token
    );

    @GET("/api/storage/list/")
    Call<List<ProcessedDataResponse>> getAllData(@Header("Authorization") String token);

    @POST("/api/users/refresh/")
    @FormUrlEncoded
    Call<AccessToken> refresh(@Field("refresh") String refreshToken);

    @PUT("/api/storage/{data_id}/upload/")
    @Multipart
    Call<Source> upload(@Path(value = "data_id", encoded = true) String dataId,
                        @Part MultipartBody.Part source,
                        @Header("Authorization") String token
    );

    @PUT("/api/storage/{data_id}/select/")
    @FormUrlEncoded
    Call<Void> select(@Path(value = "data_id", encoded = true) String dataId,
                      @Field("selected") boolean selected,
                      @Header("Authorization") String token
    );

    @POST("/api/users/check/")
    Call<Void> check(@Header("Authorization") String token);

}
