package com.PrimeAuthPro;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String APP_ID = "Your App ID";
    private static final String SECRET_KEY = "Your App Secret Key";
    private static final String VERSION = "App Version";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startApp();
    }

    private void startApp() {
        try {
            Runtime.getRuntime().exec("su");
        } catch (Exception e) {
            Toast.makeText(this, "Root not found!", Toast.LENGTH_SHORT).show();
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        new Login(this);

        AuthHelper.clearSession();

        AuthHelper.init(APP_ID, SECRET_KEY, VERSION, new AuthHelper.InitCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Authentication initialized successfully!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,  error, Toast.LENGTH_LONG).show());
            }
        });
    }
}
