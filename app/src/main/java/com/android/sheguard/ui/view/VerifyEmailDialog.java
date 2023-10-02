package com.android.sheguard.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.sheguard.R;

import java.util.Objects;

public class VerifyEmailDialog extends AppCompatActivity {

    Context context;
    Dialog dialog;

    public VerifyEmailDialog(Context context) {
        this.context = context;
    }

    public void show(@Nullable Integer icon, Object title, Object description, View.OnClickListener onClickListener) {
        if (dialog != null) dialog.dismiss();

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_verify_email_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setOnCancelListener(null);
        dialog.setCanceledOnTouchOutside(true);

        if (icon != null) {
            ImageView iconView = dialog.findViewById(R.id.icon);
            iconView.setImageDrawable(ContextCompat.getDrawable(context, icon));
        }

        TextView text = dialog.findViewById(R.id.title);
        if (title instanceof Integer) {
            text.setText(Html.fromHtml((String) context.getResources().getText((Integer) title), Html.FROM_HTML_MODE_COMPACT));
        } else if (title instanceof String) {
            text.setText(Html.fromHtml((String) title, Html.FROM_HTML_MODE_COMPACT));
        }

        TextView desc = dialog.findViewById(R.id.description);
        if (description instanceof Integer) {
            desc.setText(Html.fromHtml((String) context.getResources().getText((Integer) description), Html.FROM_HTML_MODE_COMPACT));
        } else if (description instanceof String) {
            desc.setText(Html.fromHtml((String) description, Html.FROM_HTML_MODE_COMPACT));
        }

        Button resend_email = dialog.findViewById(R.id.resend_email);
        resend_email.setOnClickListener(onClickListener);

        Button close = dialog.findViewById(R.id.close);
        close.setOnClickListener(view -> hide());

        dialog.create();
        dialog.show();

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
    }

    public void hide() {
        if ((dialog != null) && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        hide();
        super.onDestroy();
    }
}
