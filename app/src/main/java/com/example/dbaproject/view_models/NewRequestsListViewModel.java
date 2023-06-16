package com.example.dbaproject.view_models;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dbaproject.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewRequestsListViewModel extends ViewModel {
    private List<NewRequestItem> items;
    private MutableLiveData<List<NewRequestItem>> data;

    public NewRequestsListViewModel() {
        data = new MutableLiveData<>();
        data.setValue(new ArrayList<>());
        items = new ArrayList<>();
    }

    public void removeItem(NewRequestItem item){
        items.remove(item);
        updateValue();
    }

    public void addItems(List<NewRequestItem> items){
        this.items.addAll(items);
        updateValue();
    }

    public void addItemsByUri(List<Uri> uriList, String defaultName){
        List<NewRequestItem> newItems = new ArrayList<>();

        for (int i = 0; i < uriList.size(); i++) {
            newItems.add(new NewRequestItem(
                    uriList.get(i),
                    defaultName + " #" + (i + 1 + items.size())
            ));
        }

        addItems(newItems);
    }

    public void clear(){
        items.clear();
        updateValue();
    }

    private void updateValue(){
        data.postValue(Collections.unmodifiableList(items));
    }

    public MutableLiveData<List<NewRequestItem>> getData() {
        return data;
    }
}
