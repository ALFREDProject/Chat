package eu.alfred.chat.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.SmsManager;

import eu.alfred.chat.R;

public class PhoneCommunicationUtils {

    private static final String DEFAULT_SKYPE_PACKAGE = "com.skype.raider";
    public static String TAG = "PhoneCommunicationUtils";

    public static boolean call(Context context, String completePhoneNumber) {
        DebugUtils.d(context, TAG, "call #" + completePhoneNumber + "#", true, false);
        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + completePhoneNumber));
            context.startActivity(callIntent);
            return true;
        } catch (ActivityNotFoundException activityException) {
            DebugUtils.d(context, TAG, context.getString(R.string.error), true, true);
        } catch (Exception exception) {
            DebugUtils.d(context, TAG, context.getString(R.string.error), true, true);
        }
        return false;
    }

    public static boolean videoCall(Context context, String name, String completePhoneNumber) {
        DebugUtils.d(context, TAG, "videoCall #" + completePhoneNumber + "#", true, false);
        // TODO videocall via skype or install the skype application
        if (isSkypeInstalled(context)) {
            try {
                DebugUtils.d(context, TAG, context.getString(R.string.videocalling_skype), true, true);
                Intent skype = new Intent("android.intent.action.VIEW");
                skype.setData(Uri.parse("skype:" + name + "?call&video=true"));
                context.startActivity(skype);
            } catch (Exception e) {
                e.printStackTrace();
                DebugUtils.d(context, TAG, context.getString(R.string.error), true, true);
            }
        } else {
            installSkype(context);
        }

        return true;
    }

    private static boolean isSkypeInstalled(Context context) {
        return isPackageInstalled(context, DEFAULT_SKYPE_PACKAGE);
    }

    private static boolean isPackageInstalled(Context context, String packagename) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static void installSkype(Context context) {
        DebugUtils.d(context, TAG, context.getString(R.string.install_skype), true, true);
        Intent sky = new Intent("android.intent.action.VIEW");
        sky.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.skype.raider"));
        context.startActivity(sky);
    }

    public static boolean sendMessage(Context context, String completePhoneNumber, String message) {
        DebugUtils.d(context, TAG, "sendMessage #" + completePhoneNumber + "##" + message + "#", true, false);
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(completePhoneNumber, null, message, null, null);
            DebugUtils.d(context, TAG, context.getString(R.string.sms_sent), true, true);
            return true;
        } catch (Exception e) {
            DebugUtils.d(context, TAG, context.getString(R.string.error), true, true);
            e.printStackTrace();
        }
        return false;
    }
}
