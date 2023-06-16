package com.example.dbaproject.utils.dialogs;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.dbaproject.R;
import com.example.dbaproject.databinding.FiltersDialogBinding;
import com.example.dbaproject.utils.DateTimeUtils;
import com.example.dbaproject.view_models.Filtered;
import com.example.dbaproject.view_models.Filters;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;
import java.util.Date;

public class FiltersDialog extends BottomSheetDialog {

    private FiltersDialogBinding binding;
    private AppCompatActivity activity;
    private Filtered filtered;
    private Filters filters;

    public FiltersDialog(AppCompatActivity activity, Filtered filtered) {
        super(activity);

        binding = FiltersDialogBinding.inflate(getLayoutInflater());
        this.filtered = filtered;
        this.filters = filtered.getFilters();
        this.activity = activity;

        updateData();
        addListeners();

        setContentView(binding.getRoot());
    }

    private Drawable getDrawable(int id) {
        return AppCompatResources.getDrawable(activity, id);
    }

    private void updateData() {
        binding.onlySelectedCheckbox.setChecked(filters.isOnlySelected());

        String startDate = "";
        if (filters.getStartDate() != null) {
            startDate = DateTimeUtils.format(filters.getStartDate());
        }
        binding.fromDateOut.setText(startDate);

        String endDate = "";
        if (filters.getEndDate() != null) {
            endDate = DateTimeUtils.format(filters.getEndDate());
        }
        binding.toDateOut.setText(endDate);

        String search = "";
        if (filters.getSearch() != null) {
            search = filters.getSearch();
        }
        binding.searchInput.setText(search);
    }

    private void addListeners() {
        binding.clearFromDateBtn.setOnClickListener((view) -> {
            filters.setStartDate(null);
            updateData();
        });
        binding.clearFromDateBtn.setOnClickListener((view) -> {
            filters.setEndDate(null);
            updateData();
        });
        binding.clearSearchInputBtn.setOnClickListener((view) -> {
            filters.setSearch(null);
            updateData();
        });
        binding.changeFromDateBtn.setOnClickListener((view) -> {
            requestDateTime((date) -> {
                filters.setStartDate(date);
                updateData();
            });
        });
        binding.changeToDateBtn.setOnClickListener((view) -> {
            requestDateTime((date) -> {
                filters.setEndDate(date);
                updateData();
            });
        });
        binding.filtersClearBtn.setOnClickListener((view) -> {
            filters = new Filters();
            updateData();
        });
        binding.filtersApplyBtn.setOnClickListener((view) -> {
            if (filters.isValid()){
                dismiss();
                filtered.updateFilters(filters);
            } else {
                new ErrorDialog(
                        activity.getString(R.string.wrong_input),
                        activity.getString(R.string.date_input_error)
                ).show(activity.getSupportFragmentManager(), "error");
            }
        });
        binding.onlySelectedCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            filters.setOnlySelected(b);
        });
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filters.setSearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private static interface DateTimeResp {
        void onResp(Date date);
    }

    private void requestDateTime(DateTimeResp r) {
        Calendar dateAndTime = Calendar.getInstance();

        new DatePickerDialog(activity,
                (datePicker, y, m, d) -> {
                    new TimePickerDialog(activity,
                            (timePicker, h, min) -> {
                                r.onResp(new Date(
                                        y - 1900, m, d, h, min
                                ));
                            },
                            dateAndTime.get(Calendar.HOUR_OF_DAY),
                            dateAndTime.get(Calendar.MINUTE), true)
                            .show();
                },
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
}
