package com.example.dbaproject.view_models;

import com.example.dbaproject.api.models.processed_data.ProcessedData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Filters {
    private Date startDate, endDate;
    private boolean onlySelected;
    private String search;

    public Filters() {
        startDate = null;
        endDate = null;
        onlySelected = false;
        search = null;
    }

    public Filters(Date startDate, Date endDate, boolean onlySelected, String search) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.onlySelected = onlySelected;
        this.search = search;
    }

    public List<ProcessedData> filter(List<ProcessedData> processedDataList) {
        List<ProcessedData> result = new ArrayList<>();
        for (ProcessedData processedData : processedDataList) {

            if (startDate != null && processedData.dateCreation.before(startDate)) {
                continue;
            }

            if (endDate != null && processedData.dateCreation.after(endDate)) {
                continue;
            }

            if (onlySelected && !processedData.selected) {
                continue;
            }

            if (search != null && !search.isEmpty() && !processedData.name.contains(search)) {
                continue;
            }

            result.add(processedData);
        }
        return result;
    }

    public boolean isValid() {
        return startDate == null || endDate == null || startDate.before(endDate);
    }

    public Filters copy() {
        return new Filters(
                startDate,
                endDate,
                onlySelected,
                search
        );
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isOnlySelected() {
        return onlySelected;
    }

    public void setOnlySelected(boolean onlySelected) {
        this.onlySelected = onlySelected;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
