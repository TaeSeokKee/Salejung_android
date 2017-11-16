package com.salejung_android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import java.util.Arrays;

/**
 * Created by xotjr on 2017-11-09.
 */

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private String returnActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent myIntent = getIntent(); // gets the previously created intent
        returnActivity = myIntent.getStringExtra("returnActivity"); // will return "FirstKeyValue"

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build()
                                        // TODO : google login fail
                                        // Because It doesn't work in Samsung device, not use.
                                        //new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                        ))
                        .build(),
                RC_SIGN_IN);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in.
            // TODO : fasebook login test not yet. only twitter test completed.
            if (resultCode == RESULT_OK) {

                // If login triggered by LocationForUploadActivity.
                if(returnActivity.equals("LocationForUploadActivity")) {
                    Intent intent = new Intent(LoginActivity.this, LocationForUploadActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                // If login triggered by LocationForSearchActivity.
                else if(returnActivity.equals("LocationForSearchActivity")) {
                    Intent intent = new Intent(LoginActivity.this, LocationForSearchActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                // If login triggered by LocationForFCMActivity.
                else if(returnActivity.equals("LocationForFCMActivity")) {
                    Intent intent = new Intent(LoginActivity.this, LocationForFCMActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "no_internet_connection", Toast.LENGTH_LONG).show();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, "unknown_error", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            Toast.makeText(this, "unknown_sign_in_response", Toast.LENGTH_LONG).show();
        }
    }


}
