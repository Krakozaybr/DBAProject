package com.example.dbaproject.activities;

import android.content.Intent;

import com.example.dbaproject.R;
import com.example.dbaproject.activities.login_activity.LoginActivity;
import com.example.dbaproject.api.APIWrapper;

// Just one more level of abstraction.
// It is made for the case when we will have more than one activity of content
// For example SettingsActivity or something like that
public class AuthenticatedActivity extends BasicActivity{

    protected abstract class AuthenticatedErrorHandler<T> extends SimpleErrorHandler<T> {

        @Override
        public void onServerError(int code) {
            String text;
            if (code == 401) {
                text = getString(R.string.wrong_username_or_password);
            } else {
                text = getString(R.string.problems_with_server);;
            }
            showErrorMessage(text);
            startActivity(new Intent(AuthenticatedActivity.this, LoginActivity.class));
        }
    }
}
