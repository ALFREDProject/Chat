package eu.alfred.chat.util;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import eu.alfred.chat.R;

/**
 * Created by a593419 on 02/12/2015.
 */
public class DialogUtils {
    public static void showConfirmDialog(Context context, String title, String msg, String yesTxt, String noTxt, boolean cancelable, final View.OnClickListener onClickListenerPositive, final View.OnClickListener onClickListenerNegative) {

        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(cancelable);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before setContentView(...)
        dialog.setContentView(R.layout.dialog_confirm);

        // set the custom dialog components - text, image and button
        TextView titleView = (TextView) dialog.findViewById(R.id.title);
        TextView descriptionView = (TextView) dialog.findViewById(R.id.description);
        Button buttonYesView = (Button) dialog.findViewById(R.id.buttonYes);
        Button buttonNoView = (Button) dialog.findViewById(R.id.buttonNo);
        titleView.setText(!TextUtils.isEmpty(title) ? title : "");
        descriptionView.setText(!TextUtils.isEmpty(msg) ? msg : "");
        buttonYesView.setText(!TextUtils.isEmpty(yesTxt) ? yesTxt : context.getString(android.R.string.ok));
        buttonNoView.setText(!TextUtils.isEmpty(noTxt) ? noTxt : context.getString(android.R.string.no));

        dialog.show();

        buttonYesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListenerPositive != null) {
                    onClickListenerPositive.onClick(v);
                }
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        buttonNoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListenerNegative != null) {
                    onClickListenerNegative.onClick(v);
                }
                try {
                    dialog.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /*new AlertDialog.Builder(context)
                .setCancelable(cancelable)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(yesTxt, onClickListenerPositive)
                .setNegativeButton(noTxt, onClickListenerNegative)
                .show();*/
    }
}
