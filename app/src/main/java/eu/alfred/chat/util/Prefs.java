package eu.alfred.chat.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;


public class Prefs {

    public static final String PREFS_NAME = "prefs";
    public static final int PREFS_MODE = Context.MODE_PRIVATE;
    private static Context ctx;

    public static void init(Context context) {
        if (context != null) {
            ctx = context;
        }
    }

    private static SharedPreferences getPrefs() {
        return ctx.getSharedPreferences(PREFS_NAME, PREFS_MODE);
    }

    public static String getString(String key, String defaultValue) {
        return getPrefs().getString(key, defaultValue);
    }

    public static boolean setString(String key, String value) {
        return getPrefs().edit().putString(key, value).commit();
    }

    public static int getInt(String key, int defaultValue) {
        return getPrefs().getInt(key, defaultValue);
    }

    public static boolean setInt(String key, int value) {
        return getPrefs().edit().putInt(key, value).commit();
    }

    public static float getFloat(String key, float defaultValue) {
        return getPrefs().getFloat(key, defaultValue);
    }

    public static boolean setFloat(String key, float value) {
        return getPrefs().edit().putFloat(key, value).commit();
    }

    public static long getLong(String key, long defaultValue) {
        return getPrefs().getLong(key, defaultValue);
    }

    public static boolean setLong(String key, long value) {
        return getPrefs().edit().putLong(key, value).commit();
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return getPrefs().getBoolean(key, defaultValue);
    }

    public static boolean setBoolean(String key, boolean value) {
        return getPrefs().edit().putBoolean(key, value).commit();
    }

    public static boolean remove(String key) {
        return !TextUtils.isEmpty(key) && getPrefs().edit().remove(key).commit();
    }

}