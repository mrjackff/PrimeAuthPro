package com.chad;

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

    private static final int REQUEST_OVERLAY_PERMISSION = 5469;
    private static final int REQUEST_STORAGE_PERMISSION = 5470;

    private static final String APP_ID = "c379560000";
    private static final String SECRET_KEY = "8ad92d0a650a178cb642";
    private static final String VERSION = "2.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            return;
        }

        // Storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_STORAGE_PERMISSION);
            return;
        }

        startApp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
                recreate(); // re-check storage permission
            } else {
                Toast.makeText(this, "Overlay permission not granted!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startApp();
            } else {
                Toast.makeText(this, "Storage permission not granted!", Toast.LENGTH_SHORT).show();
            }
        }
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
