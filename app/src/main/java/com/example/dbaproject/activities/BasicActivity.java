package com.example.dbaproject.activities;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dbaproject.R;
import com.example.dbaproject.api.APIWrapper;

// The ancestor of LoginActivity and RegistrationActivity
public class BasicActivity extends AppCompatActivity {

    protected void showErrorMessage(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    // Just convinient solution for network errors` catching
    // Can be improved
    protected abstract class SimpleErrorHandler<T> implements APIWrapper.WrappedCallback<T> {
        @Override
        public void onFailure(Throwable t) {
            showErrorMessage(getString(R.string.problems_with_connection));
        }

        @Override
        public void onServerError(int code) {
            String text;
            if (code == 401) {
                text = getString(R.string.wrong_username_or_password);
            } else {
                text = getString(R.string.problems_with_server);;
            }
            showErrorMessage(text);
        }
    }

    // That`s done to prevent user`s coming back to LoginActivity from MainActivity
    // and on the contrary
    @Override
    public void onBackPressed() {}
}
