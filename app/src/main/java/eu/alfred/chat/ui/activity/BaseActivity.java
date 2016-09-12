package eu.alfred.chat.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import eu.alfred.chat.R;
import eu.alfred.chat.util.DialogUtils;
import eu.alfred.chat.util.PermissionUtils;
import eu.alfred.chat.util.Prefs;
import eu.alfred.ui.AppActivity;

public abstract class BaseActivity extends AppActivity {

    private final String[] permissionsRequired = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.SEND_SMS};
    private boolean askingPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!askingPermissions) {
            checkPermissions();
        }
    }

    private void init() {
        Prefs.init(getApplicationContext());
        askingPermissions = false;
    }

    private void checkPermissions() {
        askingPermissions = true;
        PermissionUtils.requestPermissions(this, permissionsRequired, PermissionUtils.DEFAULT_PERMISSION_REQUEST_CODE);
    }

    private void askCheckPermissionsOrClose() {
        DialogUtils.showConfirmDialog(this,
                getString(R.string.permissions_needed),
                getString(R.string.permissions_needed_descr),
                getString(R.string.permissions_needed_check),
                getString(R.string.permissions_needed_close),
                false,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkPermissions();
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BaseActivity.this.finish();
                    }
                });
    }

    private void askOpenPermissionSettingsOrClose() {
        DialogUtils.showConfirmDialog(this,
                getString(R.string.permissions_needed),
                getString(R.string.permissions_needed_descr),
                getString(R.string.permissions_needed_open),
                getString(R.string.permissions_needed_close),
                false,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPermissionSettings();
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BaseActivity.this.finish();
                    }
                });
    }

    private void openPermissionSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BaseActivity.this.getPackageName(), null);
        intent.setData(uri);
        BaseActivity.this.startActivityForResult(intent, PermissionUtils.DEFAULT_PERMISSION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionUtils.DEFAULT_PERMISSION_REQUEST_CODE) {
            checkPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.DEFAULT_PERMISSION_REQUEST_CODE) {
            int status = 0; // 0 ok, 1 denied, 2 never ask again
            int i = 0;
            for (int granted : grantResults) {
                if (granted != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(BaseActivity.this, permissions[i])) {
                        status = 1;
                    } else {
                        status = 2;
                        break;
                    }
                }
                i++;
            }
            if (status == 0) {
                askingPermissions = false;
            } else if (status == 1) {
                askCheckPermissionsOrClose();
            } else {
                askOpenPermissionSettingsOrClose();
            }
        }
    }
}
