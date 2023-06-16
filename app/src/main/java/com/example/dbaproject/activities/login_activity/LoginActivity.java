package com.example.dbaproject.activities.login_activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.dbaproject.R;
import com.example.dbaproject.activities.BasicActivity;
import com.example.dbaproject.activities.main_activity.MainActivity;
import com.example.dbaproject.activities.registration_activity.RegistrationActivity;
import com.example.dbaproject.api.APIHelper;
import com.example.dbaproject.api.APIWrapper;
import com.example.dbaproject.databinding.LoginActivityBinding;
import com.example.dbaproject.utils.PreferenceManager;
/*
Start activity. Checks existing and expiring access and refresh tokens.
If they are valid redirects to MainActivity
* */
public class LoginActivity extends BasicActivity {

    private PreferenceManager preferenceManager;
    private LoginActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = new PreferenceManager(this);
        checkTokensExpired();

        binding = LoginActivityBinding.inflate(getLayoutInflater());

        addListeners();

        setContentView(binding.getRoot());
    }

    private void checkTokensExpired() {
        APIHelper.getInstance().getStorageAPI().checkToken(preferenceManager, new SimpleErrorHandler<Void>() {
            @Override
            public void onServerSuccess(Void data) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }

            @Override
            public void onServerError(int code) {}
        });
    }

    private void addListeners() {
        // Go to RegistrationActivity
        binding.gotoRegisterBtn.setOnClickListener((view) -> {
            startActivity(new Intent(this, RegistrationActivity.class));
        });
        // Try to log in
        binding.signinBtn.setOnClickListener((view) -> {
            APIWrapper api = APIHelper.getInstance().getStorageAPI();

            String username = binding.usernameInputLogin.getText().toString();
            String password = binding.passwordInputLogin.getText().toString();

            // Authorization isn`t covered by wrapper for now, because
            // it is not quite long and don`t need any data saving.
            // However, if we want to follow MVVM, it should be

            api.login(username, password, preferenceManager, new SimpleErrorHandler<Void>() {
                @Override
                public void onServerSuccess(Void data) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
        });
    }
}
