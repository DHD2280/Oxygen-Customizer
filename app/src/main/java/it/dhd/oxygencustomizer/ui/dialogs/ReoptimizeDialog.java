package it.dhd.oxygencustomizer.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
    private final ObservableArrayList<String> list = new ObservableArrayList<>();
    private Shell.Result shellResult;

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
        mRestart.setOnClickListener(v -> AppUtils.restartAllScope(new String[]{mPkgName}));
        Shell.Job job = Shell.cmd("pm compile -r bg-dexopt " + mPkgName);
        list.addOnListChangedCallback(new MyCallback(logs, job, () -> {
            progress.setIndeterminate(false);
            progress.setMin(0);
            progress.setMax(100);
            progress.setProgress(100, true);
            mOk.setEnabled(true);
            mRestart.setEnabled(true);
        }));
        list.add("pm compile -r bg-dexopt " + mPkgName);
        Shell.cmd("pm compile -r bg-dexopt " + mPkgName).to(list, null).exec();
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

    public class MyCallback extends ObservableList.OnListChangedCallback<ObservableList<?>> {

        private final TextView mTextView;
        private final OnSuccessListener mOnSuccessListener;
        private final Shell.Job mShellJob;
        private final Shell.Result mShellResult;

        public MyCallback(TextView tv, Shell.Job shellJob, OnSuccessListener onSuccessListener) {
            this.mTextView = tv;
            this.mOnSuccessListener = onSuccessListener;
            this.mShellJob = shellJob;
            this.mShellResult = shellJob.exec();
        }

        public void onChanged(ObservableList sender) {}

        public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
        }

        public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
            Log.d("MyCallback", "onItemRangeInserted: " + sender);
            StringBuilder textBuilder = new StringBuilder();
            for (String s : ((List<String>) sender)) {
                textBuilder.append("<br>").append(s);
            }
            if (mShellResult.getCode() == 0 && mOnSuccessListener != null) {
                mOnSuccessListener.onSuccess();
            } else {
                Toast.makeText(ReoptimizeDialog.this.context, "Error: " + mShellResult.getCode(), Toast.LENGTH_SHORT).show();
            }
            if (mTextView == null) return;
            mTextView.setText(
                    HtmlCompat.fromHtml(
                            textBuilder.toString(),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
            );
        }

        public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount)  {
        }

        public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount)  {
        }
    }

}
