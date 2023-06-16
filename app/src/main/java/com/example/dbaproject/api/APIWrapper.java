package com.example.dbaproject.api;

import com.example.dbaproject.api.models.AccessToken;
import com.example.dbaproject.api.models.Source;
import com.example.dbaproject.api.models.processed_data.ProcessedDataCreate;
import com.example.dbaproject.api.models.processed_data.ProcessedDataResponse;
import com.example.dbaproject.api.models.UserAuthResp;
import com.example.dbaproject.utils.PreferenceManager;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Wraps interaction with backend by auto updating access tokens
public class APIWrapper {
    private StorageAPI storageApi;

    public APIWrapper(StorageAPI storageApi) {
        this.storageApi = storageApi;
    }

    // For now there is no big difference between authorization and registration
    // So general logic of them is presented here
    private static class UserAuthorizer implements Callback<UserAuthResp>{
        PreferenceManager preferenceManager;
        WrappedCallback<Void> callback;

        public UserAuthorizer(PreferenceManager preferenceManager, WrappedCallback<Void> callback) {
            this.preferenceManager = preferenceManager;
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<UserAuthResp> call, Response<UserAuthResp> response) {
            UserAuthResp resp = response.body();

            if (resp != null) {
                preferenceManager.saveAccessToken(resp.tokens.access);
                preferenceManager.saveRefreshToken(resp.tokens.refresh);
                callback.onServerSuccess(null);
            } else {
                callback.onServerError(response.code());
            }
        }

        @Override
        public void onFailure(Call<UserAuthResp> call, Throwable t) {
            callback.onFailure(t);
        }
    }

    // Next come wrapped methods of StorageAPI

    public Cancellable login(String username, String password, PreferenceManager preferenceManager, WrappedCallback<Void> callback) {
        Call<UserAuthResp> call = storageApi.login(username, password);
        call.enqueue(new UserAuthorizer(preferenceManager, callback));
        return call::cancel;
    }

    public Cancellable register(String username, String password, String email, PreferenceManager preferenceManager, WrappedCallback<Void> callback) {
        Call<UserAuthResp> call = storageApi.register(username, password, email);
        call.enqueue(new UserAuthorizer(preferenceManager, callback));
        return call::cancel;
    }

    public Cancellable addData(ProcessedDataCreate processedDataCreate,
                        PreferenceManager preferenceManager,
                        WrappedCallback<ProcessedDataResponse> callback
    ) {
        CallbackProxy<ProcessedDataResponse> callbackProxy = new CallbackProxy<>(callback,
                accessToken ->
                        storageApi.addData(
                                processedDataCreate,
                                accessToken
                        ),
                preferenceManager
        );
        callbackProxy.start();
        return callbackProxy;
    }

    public Cancellable checkToken(PreferenceManager preferenceManager, WrappedCallback<Void> callback){
        CallbackProxy<Void> callbackProxy = new CallbackProxy<>(
                callback,
                accessToken -> storageApi.check(accessToken),
                preferenceManager
        );
        callbackProxy.start();
        return callbackProxy;
    }

    public Cancellable getData(int dataId, PreferenceManager preferenceManager, WrappedCallback<ProcessedDataResponse> callback) {
        CallbackProxy<ProcessedDataResponse> callbackProxy = new CallbackProxy<>(
                callback,
                accessToken -> storageApi.getData("" + dataId, accessToken),
                preferenceManager
        );
        callbackProxy.start();
        return callbackProxy;
    }

    public Cancellable deleteData(int dataId, PreferenceManager preferenceManager, WrappedCallback<Void> callback) {
        CallbackProxy<Void> callbackProxy = new CallbackProxy<>(
                callback,
                accessToken -> storageApi.deleteData("" + dataId, accessToken),
                preferenceManager
        );
        callbackProxy.start();
        return callbackProxy;
    }

    public Cancellable getAllData(PreferenceManager preferenceManager, WrappedCallback<List<ProcessedDataResponse>> callback) {
        CallbackProxy<List<ProcessedDataResponse>> callbackProxy = new CallbackProxy<>(
                callback,
                accessToken -> storageApi.getAllData(accessToken),
                preferenceManager
        );
        callbackProxy.start();
        return callbackProxy;
    }

    public Cancellable upload(int dataId, File sourceFile, PreferenceManager preferenceManager, WrappedCallback<Source> callback){
        RequestBody fbody = RequestBody.create(
                MediaType.parse("multipart/form-data"),
                sourceFile
        );
        MultipartBody.Part source = MultipartBody.Part.createFormData(
                "source", sourceFile.getName(), fbody
        );
        CallbackProxy<Source> callbackProxy = new CallbackProxy<>(
                callback,
                accessToken -> storageApi.upload(dataId + "", source, accessToken),
                preferenceManager
        );
        callbackProxy.start();
        return callbackProxy;
    }

    public Cancellable select(int dataId, boolean selected, PreferenceManager preferenceManager, WrappedCallback<Void> callback){
        CallbackProxy<Void> callbackProxy = new CallbackProxy<>(
                callback,
                accessToken -> storageApi.select(dataId + "", selected, accessToken),
                preferenceManager
        );
        callbackProxy.start();
        return callbackProxy;
    }

    public Call<AccessToken> refresh(String refreshToken) {
        return storageApi.refresh(refreshToken);
    }

    private static interface CallRunner<T> {
        Call<T> runCall(String accessToken);
    }

    // Delegates cancel() to current Call<> object
    public static interface Cancellable{
        void cancel();
    }

    // Proxy for api that contains logic of updating access token
    class CallbackProxy<T> extends FailureWrappedCallback<T> implements Cancellable {

        public CallbackProxy(WrappedCallback<T> callback, CallRunner<T> callRunner, PreferenceManager preferenceManager) {
            super(callback);
            this.callback = callback;
            this.callRunner = callRunner;
            this.preferenceManager = preferenceManager;
            this.accessUpdated = false;
        }

        WrappedCallback<T> callback;
        CallRunner<T> callRunner;
        PreferenceManager preferenceManager;
        boolean accessUpdated;
        private Call currentCall;

        private static final String tokenPrefix = "Bearer ";

        public void start() {
            currentCall = callRunner.runCall(tokenPrefix + preferenceManager.getAccessToken());
            currentCall.enqueue(this);
        }

        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            int code = response.code();
            T body = response.body();
            if (code >= 200 && code < 300) {
                callback.onServerSuccess(body);
            } else if (code == 401 && !accessUpdated) {
                refreshAccess();
            } else {
                callback.onServerError(code);
            }
        }

        // Attempt to refresh access token, if everything ok, tries to do request one more time
        // If opposite case happened, notifies WrappedCallback
        private void refreshAccess() {
            String refresh = preferenceManager.getRefreshToken();

            currentCall = refresh(refresh);
            currentCall.enqueue(new FailureWrappedCallback<AccessToken>(callback) {
                @Override
                public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                    AccessToken body = response.body();
                    if (response.code() == 200) {
                        preferenceManager.saveAccessToken(body.access);
                        accessUpdated = true;
                        start();
                    } else {
                        callback.onServerError(response.code());
                    }
                }
            });
        }

        @Override
        public void cancel() {
            if (currentCall != null){
                currentCall.cancel();
            }
        }
    }

    public static interface FailureCallback {
        void onFailure(Throwable t);

        void onServerError(int code);
    }

    public static interface WrappedCallback<F> extends FailureCallback {
        void onServerSuccess(F data);
    }

    // Delegates onFailure to object of FailureCallback which is presented
    private static abstract class FailureWrappedCallback<F> implements Callback<F> {
        private final FailureCallback fail;

        public FailureWrappedCallback(FailureCallback fail) {
            this.fail = fail;
        }

        @Override
        public void onFailure(Call<F> call, Throwable t) {
            fail.onFailure(t);
        }
    }
}
