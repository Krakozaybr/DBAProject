package com.example.dbaproject.api.models.processed_data;

// Used for making requests to create ProcessedData on backend
public class ProcessedDataCreate extends AbstractProcessedData {
    public String name;
    public boolean rotated;
    public boolean selected = false;

    public ProcessedDataCreate() {
    }

    public ProcessedDataCreate(ProcessingResponse response, String name, boolean rotated){
        this.dateCreation = response.dateCreation;
        this.shapes = response.shapes;
        this.name = name;
        this.rotated = rotated;
    }
}
