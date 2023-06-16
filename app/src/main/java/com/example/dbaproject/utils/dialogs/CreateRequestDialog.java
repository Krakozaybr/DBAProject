package com.example.dbaproject.utils.dialogs;

import android.app.AlertDialog;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.dbaproject.R;
import com.example.dbaproject.databinding.CreateRequestDialogBinding;
import com.example.dbaproject.utils.adapters.RequestListAdapter;
import com.example.dbaproject.view_models.ImageListViewModel;
import com.example.dbaproject.view_models.NewRequestItem;
import com.example.dbaproject.view_models.NewRequestsListViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class CreateRequestDialog extends BottomSheetDialog {

    private CreateRequestDialogBinding binding;
    private AppCompatActivity activity;
    private PickImages imagesPicker;
    private NewRequestsListViewModel newRequestsListViewModel;
    private ImageListViewModel imageListViewModel;

    public CreateRequestDialog(AppCompatActivity activity,
                               PickImages imagesPicker
    ) {
        super(activity);
        this.activity = activity;
        this.imagesPicker = imagesPicker;
        initViewModels();

        binding = CreateRequestDialogBinding.inflate(getLayoutInflater());
        addListeners();

        setContentView(binding.getRoot());
    }

    private void initViewModels(){
        ViewModelProvider provider = new ViewModelProvider(activity);
        this.newRequestsListViewModel = provider.get(NewRequestsListViewModel.class);
        this.imageListViewModel = provider.get(ImageListViewModel.class);
    }

    private void addListeners() {
        newRequestsListViewModel.getData().observe(activity, items -> {
            RequestListAdapter adapter = new RequestListAdapter(activity, items, item -> {
                newRequestsListViewModel.removeItem(item);
            });
            binding.requestsList.setAdapter(adapter);

            int buttonsVisibility = items.isEmpty() ? View.GONE : View.VISIBLE;
            binding.clearAllBtn.setVisibility(buttonsVisibility);
            binding.sendRequestsBtn.setVisibility(buttonsVisibility);
        });
        binding.clearAllBtn.setOnClickListener((view) -> {
            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.confirmation))
                    .setMessage(R.string.delete_new_requests_suppress)
                    .setNegativeButton(R.string.no, (dialogInterface, i) -> {})
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        newRequestsListViewModel.clear();
                    }).show();
        });
        binding.addImagesBtn.setOnClickListener((view) -> {
            dismiss();
            imagesPicker.pickImages();
        });
        binding.sendRequestsBtn.setOnClickListener((view) -> {
            for (NewRequestItem item : newRequestsListViewModel.getData().getValue()){
                imageListViewModel.addData(item);
            }
            newRequestsListViewModel.clear();
            dismiss();
        });
    }



    public static interface PickImages {
        void pickImages();
    }
}
