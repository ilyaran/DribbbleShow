package mysite.com.dribbbleshow.other;

import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;
import android.app.Activity;

/**
 * Utility to manage Android-M permission model
 */
public class AppPermission {

    private Activity context;
    private int permissionCode;

    public AppPermission(Activity _context, int _permissionCode) {
        context = _context;
        permissionCode = _permissionCode;

    }
    /**
     * Handles permissions for Android M (SDK level 23)
     */
    private final String STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    private final String STORAGE_READ = "android.permission.READ_EXTERNAL_STORAGE";
    private final String INTERNET = "android.permission.INTERNET";
    private final String WIFI = "android.permission.ACCESS_WIFI_STATE";

    /**
     * @brief Classes to synchronize Android M permissions.
     * Unfortunately the requesPermission() system call is asynchronous so we have to do some
     * threading synchonization to turn it into a synchronous call to ensure
     * opening of camera and audio before permission is granted.
     *
     * IMPORTANT: Please follow these steps when compiling for SDK level 23
     * 1) Comment out checkSelfPermission() stub method below
     * 2) Comment out requestPermissions() stub method below
     *
     * Alternatively, you can also install the the Android M support libraries
     */
    private boolean pendingPermissionRequest = false;

    private int checkSelfPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(permission);
        }else return 0;
    }

    /**
     * NOTE: Comment this method when compiling for SDK level 23
     * This is just a stub so it can compile with SDK below level 23
     * short of installing the android support libraries
     */
    private void requestPermissions(String [] permissions, int code) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.requestPermissions(permissions, code);
        }
    }


    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (requestCode == permissionCode) {
            for (int i= 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "AppPermission " + permissions[i] + " is required", Toast.LENGTH_SHORT).show();
                }
            }
            pendingPermissionRequest = false;
        }
    }

    class PermissionThread extends Thread {

        private void queryPermissions() {
            if (Build.VERSION.SDK_INT >= 23) {
                List<String> permissions = new ArrayList<String>();
                final int granted = PackageManager.PERMISSION_GRANTED;

                // ask 10 times
                for (int i = 0 ; i < 9 ; i++) {
                    permissions.clear();

                    if (granted != checkSelfPermission(STORAGE_READ)) {
                        permissions.add(STORAGE_READ);
                    }
                    if (granted != checkSelfPermission(STORAGE)) {
                        permissions.add(STORAGE);
                    }
                    if (granted != checkSelfPermission(INTERNET)) {
                        permissions.add(INTERNET);
                    }
                    if (granted != checkSelfPermission(WIFI)) {
                        permissions.add(WIFI);
                    }

                    if (!permissions.isEmpty()) {
                        if (!pendingPermissionRequest) {
                            pendingPermissionRequest = true;
                            requestPermissions(permissions.toArray(new String[permissions.size()]), permissionCode);
                        }
                    } else {
                        break;
                    }
                    try {

                        Thread.sleep(2000);

                    } catch (InterruptedException e) {

                    }

                }
            }
        }

        @Override
        public void run() {
            queryPermissions();
        }
    }


    /**
     * a blocking permission query
     */
    public void askPermissions() {
        PermissionThread permission = new PermissionThread();
        permission.start();
        try {
            permission.join();
        } catch (InterruptedException e) {

        }
    }

}
