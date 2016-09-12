package eu.alfred.chat.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class DebugUtils {
    public static void d(Context context, String tag, String text, boolean log, boolean toast) {
        if (log) {
            Log.d(tag, text);
        }
        if (toast && context != null) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }
}
