package com.example.dbaproject.activities.main_activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.dbaproject.R;
import com.example.dbaproject.activities.AuthenticatedActivity;
import com.example.dbaproject.activities.login_activity.LoginActivity;
import com.example.dbaproject.api.APIWrapper;
import com.example.dbaproject.databinding.MainActivityBinding;
import com.example.dbaproject.utils.PreferenceManager;
import com.example.dbaproject.utils.adapters.ProcessedDataListAdapter;
import com.example.dbaproject.utils.dialogs.CreateRequestDialog;
import com.example.dbaproject.utils.dialogs.FiltersDialog;
import com.example.dbaproject.view_models.ImageListViewModel;
import com.example.dbaproject.view_models.NewRequestsListViewModel;

/*
Contains logic of creating, viewing and deleting requests
* */
public class MainActivity extends AuthenticatedActivity {

    private MainActivityBinding binding;
    private ImageListViewModel imageListViewModel;
    private NewRequestsListViewModel newRequestsListViewModel;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = MainActivityBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(this);

        initPickMedia();
        initViewModels();
        addListeners();

        setContentView(binding.getRoot());
    }

    // The instrument to take images from gallery
    private void initPickMedia(){
        pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(), uriList -> {
                    if (uriList != null) {
                        newRequestsListViewModel.addItemsByUri(uriList, getString(R.string.request));
                    }
                    // It is called, because CreateRequestDialog is dismissed after
                    // attempt of taking photos, so we need to launch it one more time
                    showNewRequestsDialog();
                }
        );
    }

    private void addListeners() {
        // Updating list by swiping up by SwipeLayout
        binding.swipeLayoutMain.setOnRefreshListener(() -> {
            imageListViewModel.loadData();
            binding.swipeLayoutMain.setRefreshing(false);
        });
        // Showing CreateRequestDialog
        binding.addRequestBtn.setOnClickListener((view) -> {
            showNewRequestsDialog();
        });
        binding.filtersBtn.setOnClickListener((view) -> {
            new FiltersDialog(this, imageListViewModel).show();
        });
        binding.logoutBtn.setOnClickListener((view) -> {
            // Clear credentials
            preferenceManager.saveAccessToken("");
            preferenceManager.saveRefreshToken("");
            gotoLogin();
        });
    }

    private void showNewRequestsDialog(){
        new CreateRequestDialog(
                this,
                this::pickImages
        ).show();
    }

    private void pickImages(){
        pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        );
    }



    private void initViewModels() {
        ViewModelProvider provider = ViewModelProviders.of(this);

        newRequestsListViewModel = provider.get(NewRequestsListViewModel.class);
        imageListViewModel = provider.get(ImageListViewModel.class);

        // Initing ImageListViewModel
        imageListViewModel.setFailureCallback(new APIWrapper.FailureCallback() {
            @Override
            public void onFailure(Throwable t) {
                gotoLogin();
            }

            @Override
            public void onServerError(int code) {
                gotoLogin();
            }
        });
        // Viewing the list of processed data
        imageListViewModel.getData().observe(this, processedData -> {
            ProcessedDataListAdapter adapter = new ProcessedDataListAdapter(processedData, MainActivity.this, imageListViewModel);
            binding.processedImagesList.setAdapter(adapter);
        });
        imageListViewModel.setCurrentActivity(this);
    }

    private void gotoLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

}
