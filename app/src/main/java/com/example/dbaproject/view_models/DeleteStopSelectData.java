package com.example.dbaproject.view_models;

import com.example.dbaproject.api.models.processed_data.ProcessedData;

public interface DeleteStopSelectData{
    void delete(ProcessedData data);
    void stop(ProcessedData data);
    void select(ProcessedData data);
}
