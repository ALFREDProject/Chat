package eu.alfred.chat.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PermissionUtils {

    public static final int DEFAULT_PERMISSION_REQUEST_CODE = 5;    //

    public static List<String> getDeniedPermissions(Context context, List<String> permissions) {
        if (permissions != null) {
            ArrayList<String> deniedPermissions = new ArrayList<>();
            int[] granted = new int[permissions.size()];
            for (int i = 0; i < permissions.size(); i++) {
                granted[i] = PackageManager.PERMISSION_DENIED;
            }
            int j = 0;
            for (String permission : permissions) {
                try {
                    int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        granted[j] = PackageManager.PERMISSION_GRANTED;
                    }
                } catch (Exception ignored) {
                }
                j++;
            }
            for (int i = 0; i < granted.length; i++) {
                if (granted[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions.get(i));
                }
            }
            return deniedPermissions;
        }
        return null;
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        List<String> permissns = new ArrayList<>();
        if (permissions != null) {
            Collections.addAll(permissns, permissions);
        }
        requestPermissions(activity, permissns, requestCode);
    }

    public static void requestPermissions(Activity activity, List<String> permissions, int requestCode) {
        if (activity == null || permissions == null) {
            throw new RuntimeException("Parameters must be not null. activity:" + activity + ", permissions:" + permissions);
        }
        if (!permissions.isEmpty()) {
            permissions = getDeniedPermissions(activity, permissions);
            if (!permissions.isEmpty()) {
                String[] permssins = new String[permissions.size()];
                for (int i = 0; i < permissions.size(); i++) {
                    permssins[i] = permissions.get(i);
                }
                ActivityCompat.requestPermissions(activity, permssins, requestCode);
            }
        }
    }


}
