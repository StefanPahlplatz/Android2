package com.app.eu.proximitymap.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Wrapper class to manage permission calls and checks.
 */

public class PermissionHelper {

    private Activity activity;

    public PermissionHelper(Activity activity) {
        this.activity = activity;
    }

    /**
     * Requests the user's permission. The result of this request will be received in
     * onRequestPermissionsResult, override this in the calling class.
     *
     * @param permission  to request.
     * @param requestCode code to handle the result response.
     */
    public void requestPermission(String permission, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            }
        }
    }

    /**
     * Checks whether the application has external storage read permissions.
     *
     * @return permission or not.
     */
    public boolean hasExternalStoragePermission() {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks if the application has a certain permission.
     *
     * @return whether the application has location permissions.
     */
    public boolean hasLocationPermission() {
        return !(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED);
    }
}
