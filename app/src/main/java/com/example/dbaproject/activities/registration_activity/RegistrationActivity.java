package com.example.dbaproject.activities.registration_activity;

import android.content.Intent;
import android.os.Bundle;

import com.example.dbaproject.R;
import com.example.dbaproject.activities.BasicActivity;
import com.example.dbaproject.activities.login_activity.LoginActivity;
import com.example.dbaproject.activities.main_activity.MainActivity;
import com.example.dbaproject.api.APIHelper;
import com.example.dbaproject.api.APIWrapper;
import com.example.dbaproject.databinding.RegisterActivityBinding;
import com.example.dbaproject.utils.PreferenceManager;
import com.example.dbaproject.utils.Validation;

/*
As name presents, it is responsible for registration
* */
public class RegistrationActivity extends BasicActivity {

    private RegisterActivityBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = RegisterActivityBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(this);

        setContentView(binding.getRoot());

        addListeners();
    }

    private void addListeners() {
        // Return to LoginActivity
        binding.gotoLoginBtn.setOnClickListener((view -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
        }));
        // Try to register new user
        binding.registerBtn.setOnClickListener((view) -> {
            String username = binding.usernameInputRegister.getText().toString();
            String password = binding.passwordInputRegister.getText().toString();
            String email = binding.emailInputRegister.getText().toString();

            if (!Validation.isValidEmail(email)){
                showErrorMessage(getString(R.string.wrong_email));
                return;
            }

            // Registration isn`t covered by wrapper for now, because
            // it is not quite long and don`t need any data saving.
            // However, if we want to follow MVVM, it should be

            APIWrapper api = APIHelper.getInstance().getStorageAPI();

            api.register(username, password, email, preferenceManager, new SimpleErrorHandler<Void>() {
                @Override
                public void onServerSuccess(Void data) {
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
        });
    }
}
