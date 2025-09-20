package it.dhd.oxygencustomizer.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.AppUtils;

public class ReoptimizeDialog extends AppCompatActivity {

    private final Context context;
    private Dialog dialog;
    private final String mPkgName;

    public ReoptimizeDialog(Context context, String pkgName) {
        this.context = context;
        this.mPkgName = pkgName;

    }

    public void show() {
        if (dialog != null) dialog.dismiss();

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_reoptimize_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setOnCancelListener(null);
        dialog.setCanceledOnTouchOutside(false);

        LinearProgressIndicator progress = dialog.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        progress.setIndeterminate(true);
        TextView logs = dialog.findViewById(R.id.logs);
        MaterialButton mOk, mRestart;
        mOk = dialog.findViewById(R.id.apply);
        mRestart = dialog.findViewById(R.id.restart);
        mOk.setOnClickListener(v -> dismiss());
        mRestart.setOnClickListener(v -> {
            dismiss();
            AppUtils.restartAllScope(new String[]{mPkgName});
        });
        String[] commands = {
                "pm compile -r bg-dexopt " + mPkgName,
                "cmd package compile -m speed -f " + mPkgName
        };
        new DexOptTask(commands, logs, () -> {
            progress.setIndeterminate(false);
            progress.setMin(0);
            progress.setMax(100);
            progress.setProgress(100, true);
            mOk.setEnabled(true);
            mRestart.setEnabled(true);
        }).execute();
        dialog.create();
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void hide() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void setLogs(String text) {
        TextView l = dialog.findViewById(R.id.logs);
        if (l.getText() == null)
            l.setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY));
        else l.append(HtmlCompat.fromHtml("<br>" + text, HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    @Override
    public void onDestroy() {
        dismiss();
        super.onDestroy();
    }

    public interface OnSuccessListener {
        void onSuccess();
    }

    @SuppressLint("StaticFieldLeak")
    private class DexOptTask extends AsyncTask<Void, String, Boolean> {

        private final String[] commands;
        private final TextView logView;
        private final OnSuccessListener mSuccessListener;

        public DexOptTask(String[] commands, TextView logView, OnSuccessListener successListener) {
            this.commands = commands;
            this.logView = logView;
            this.mSuccessListener = successListener;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            for (String command : commands) {
                Shell.Result result = Shell.cmd(command).exec();
                publishProgress(command);
                if (result.getOut() != null) {
                    for (String outLine : result.getOut()) {
                        publishProgress(outLine);
                    }
                }
                if (result.getCode() != 0) {
                    publishProgress("Error " + result.getCode());
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            for (String line : values) {
                if (logView != null) {
                    logView.append(HtmlCompat.fromHtml("<br>" + line, HtmlCompat.FROM_HTML_MODE_LEGACY));
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mSuccessListener.onSuccess();

            if (!success) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
