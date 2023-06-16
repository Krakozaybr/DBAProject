package com.example.dbaproject.api.models.processed_data;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.dbaproject.view_models.NewRequestItem;

// Main container of requests` data
public class ProcessedData extends ProcessedDataResponse{
    public static final int PROCESSING = 0;
    public static final int CANCELLED = 1;
    public static final int UPLOADING = 2;
    public static final int DONE = 3;

    // ProcessedData can contain intermedite results like only bitmap
    // that was sent to server, so we need to now the state of current ProcessedData
    // to view it correctly
    private MutableLiveData<Integer> state;

    // Name can be changed
    private MutableLiveData<String> nameData;

    // Usually ProcessedData doesn`t contain bitmaps, because they could be weighty
    // However, it can do that if the state is PROCESSING, and we need to show the image of request to user
    private Bitmap bitmap;

    // Constructor for creating ProcessedData in PROCESSING state
    public ProcessedData(String name, Bitmap bitmap) {
        this.state = new MutableLiveData<>();
        setState(PROCESSING);
        this.name = name;
        this.bitmap = bitmap;
    }

    // Constructor for creating ProcessedData in DONE state
    // ProcessedData takes only ProcessedDataResponse object,
    // because it contains everything necessary for viewing
    public ProcessedData(ProcessedDataResponse data){
        this.id = data.id;
        this.source = data.source;
        this.state = new MutableLiveData<>();
        setState(DONE);
        this.dateCreation = data.dateCreation;
        this.shapes = data.shapes;
        this.name = data.name;
        this.rotated = data.rotated;
        this.selected = data.selected;
    }

    // Convenient if we don`t want to call state.getState().getValue()
    public int getState() {
        return state.getValue();
    }

    public LiveData<Integer> getStateLiveData(){
        return state;
    }

    // Make ProcessedData DONE without loading whole list of data
    public void done(ProcessedDataResponse data){
        setState(DONE);
        bitmap.recycle();
        id = data.id;
        name = data.name;
        dateCreation = data.dateCreation;
        shapes = data.shapes;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setState(int state) {
        this.state.postValue(state);
    }

}
