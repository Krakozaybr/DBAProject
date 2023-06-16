package com.example.dbaproject.view_models;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dbaproject.R;
import com.example.dbaproject.api.APIHelper;
import com.example.dbaproject.api.APIWrapper;
import com.example.dbaproject.api.models.Source;
import com.example.dbaproject.api.models.processed_data.ProcessedData;
import com.example.dbaproject.api.models.processed_data.ProcessedDataCreate;
import com.example.dbaproject.api.models.processed_data.ProcessedDataResponse;
import com.example.dbaproject.api.models.processed_data.ProcessingResponse;
import com.example.dbaproject.utils.FileUtil;
import com.example.dbaproject.utils.ImageUtils;
import com.example.dbaproject.utils.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageListViewModel extends ViewModel implements DeleteStopSelectData, AddData, Filtered {

    private List<ProcessedData> allData;
    private MutableLiveData<List<ProcessedData>> data;
    private APIWrapper.FailureCallback failureCallback = null;
    private AppCompatActivity currentActivity = null;
    private HashMap<ProcessedData, Thread> processingPool;
    private Filters currentFilters;

    public ImageListViewModel() {
        this.data = new MutableLiveData<>();
        this.processingPool = new HashMap<>();
        this.currentFilters = new Filters();
    }

    private abstract class ErrorDelegator<T> implements APIWrapper.WrappedCallback<T> {
        @Override
        public void onFailure(Throwable t) {
            failureCallback.onFailure(t);
        }

        @Override
        public void onServerError(int code) {
            failureCallback.onServerError(code);
        }
    }

    public void loadData() {
        APIWrapper apiWrapper = APIHelper.getInstance().getStorageAPI();
        apiWrapper.getAllData(new PreferenceManager(currentActivity), new ErrorDelegator<List<ProcessedDataResponse>>() {
            @Override
            public void onServerSuccess(List<ProcessedDataResponse> data) {

                ArrayList<ProcessedData> newData = new ArrayList<>(data.size());

                if (allData != null) {
                    for (ProcessedData processedData : allData) {
                        if (processedData.getState() == ProcessedData.PROCESSING) {
                            newData.add(processedData);
                        }
                    }
                }

                for (ProcessedDataResponse response : data) {
                    newData.add(new ProcessedData(response));
                }

                allData = newData;
                ImageListViewModel.this.data.postValue(newData);
            }
        });
    }

    private void updateData() {
        data.postValue(currentFilters.filter(allData));
    }

    public void setFailureCallback(APIWrapper.FailureCallback failureCallback) {
        this.failureCallback = failureCallback;
    }

    public void setCurrentActivity(AppCompatActivity currentActivity) {
        boolean notInited = this.currentActivity == null;
        this.currentActivity = currentActivity;
        if (notInited) {
            loadData();
        }
    }

    public LiveData<List<ProcessedData>> getData() {
        return data;
    }

    public void postValue(List<ProcessedData> data) {
        this.data.postValue(data);
    }

    @Override
    public void delete(ProcessedData data) {
        APIWrapper apiWrapper = APIHelper.getInstance().getStorageAPI();

        apiWrapper.deleteData(data.id, new PreferenceManager(currentActivity), new ErrorDelegator<Void>() {
            @Override
            public void onServerSuccess(Void smth) {
                allData.remove(data);
                postValue(allData);
            }
        });
    }

    @Override
    public void stop(ProcessedData data) {
        Thread thread = processingPool.getOrDefault(data, null);
        if (thread != null) {
            thread.interrupt();
            allData.remove(data);
            postValue(allData);
        }
    }

    @Override
    public void select(ProcessedData data) {
        data.selected = !data.selected;
        APIHelper.getInstance().getStorageAPI().select(
                data.id,
                data.selected,
                new PreferenceManager(currentActivity),
                new ErrorDelegator<Void>() {
                    @Override
                    public void onServerSuccess(Void data) {}
                }
        );
    }

    @Override
    public void updateFilters(Filters filters) {
        this.currentFilters = filters.copy();
        updateData();
    }

    @Override
    public Filters getFilters() {
        return currentFilters.copy();
    }

    @Override
    public void addData(NewRequestItem item) {

        String name = item.getRealName();
        Bitmap bitmap = ImageUtils.loadBitmap(item.getUri(), currentActivity, R.drawable.placeholder);
        ProcessedData processedData = new ProcessedData(name, bitmap);

        allData.add(processedData);
        updateData();

        byte[] bytes = FileUtil.bytesFromUri(item.getUri(), currentActivity);
        File file = FileUtil.from(currentActivity, item.getUri());

        RequestBody fbody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/octet-stream");
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.write(bytes);
            }
        };
        Call<ProcessingResponse> call = APIHelper.getInstance()
                .getProcessingAPI()
                .detect(fbody);
        call.enqueue(
                new Callback<ProcessingResponse>() {
                    @Override
                    public void onResponse(Call<ProcessingResponse> call, Response<ProcessingResponse> response) {
                        if (response.code() == 200) {

                            if (processedData.getState() == ProcessedData.CANCELLED) {
                                return;
                            }

                            processedData.setState(ProcessedData.UPLOADING);
                            processedData.rotated = response.body().imageHeight != bitmap.getHeight();

                            uploadProcessingResponse(response.body(), file, processedData);
                        } else {
                            failureCallback.onServerError(response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ProcessingResponse> call, Throwable t) {
                        failureCallback.onFailure(t);
                    }
                }
        );
        processedData.getStateLiveData().observe(currentActivity, integer -> {
            if (integer == ProcessedData.CANCELLED && !call.isCanceled()){
                call.cancel();
                allData.remove(processedData);
                updateData();
            }
        });
    }


    private void uploadProcessingResponse(ProcessingResponse response, File file, ProcessedData processedData) {
        APIWrapper api = APIHelper.getInstance().getStorageAPI();
        PreferenceManager preferenceManager = new PreferenceManager(currentActivity);

        api.addData(new ProcessedDataCreate(response, processedData.name, processedData.rotated),
                preferenceManager,
                new ErrorDelegator<ProcessedDataResponse>() {
                    @Override
                    public void onServerSuccess(ProcessedDataResponse processedDataResponse) {

                        api.upload(
                                processedDataResponse.id,
                                file,
                                preferenceManager,
                                new ErrorDelegator<Source>() {
                                    @Override
                                    public void onServerSuccess(Source data) {
                                        processedData.source = data.source;
                                        processedData.done(processedDataResponse);
                                        updateData();
                                    }
                                }
                        );
                    }
                }
        );
    }
}
