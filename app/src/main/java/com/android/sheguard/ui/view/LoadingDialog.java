package com.android.sheguard.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.sheguard.R;

import java.util.Objects;

public class LoadingDialog extends AppCompatActivity {

    Context context;
    Dialog dialog;

    public LoadingDialog(Context context) {
        this.context = context;
    }

    public void show(@Nullable String title) {
        if (dialog != null)
            dialog.dismiss();

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_loading_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setOnCancelListener(null);
        dialog.setCanceledOnTouchOutside(false);

        if (title != null) {
            TextView text = dialog.findViewById(R.id.title);
            text.setText(title);
        }

        dialog.create();
        dialog.show();
    }

    public void show(@Nullable String title, boolean cancellable) {
        if (dialog != null)
            dialog.dismiss();

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_loading_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(cancellable);
        dialog.setOnCancelListener(null);
        dialog.setCanceledOnTouchOutside(cancellable);

        if (title != null) {
            TextView text = dialog.findViewById(R.id.title);
            text.setText(title);
        }

        dialog.create();
        dialog.show();
    }

    public void hide() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        hide();
        super.onDestroy();
    }
}
