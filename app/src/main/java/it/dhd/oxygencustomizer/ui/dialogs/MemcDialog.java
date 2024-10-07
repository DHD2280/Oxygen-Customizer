package it.dhd.oxygencustomizer.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.models.MemcAppModel;

public class MemcDialog extends AppCompatActivity {

    private final Context context;
    private Dialog dialog;
    private String mTitle = "";

    public MemcDialog(Context context) {
        this.context = context;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setTitle(@StringRes int title) {
        mTitle = context.getString(title);
    }

    public void show(
            MemcAppModel memcAppModel,
            OnApplyListener onApplyListener,
            OnDeleteListener onDeleteListener) {
        if (dialog != null) dialog.dismiss();

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_memc_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        ((TextView) dialog.findViewById(R.id.title)).setText(mTitle);

        MaterialButton mApply, mCancel, mDelete;
        mApply = dialog.findViewById(R.id.apply);
        mCancel = dialog.findViewById(R.id.cancel);
        mDelete = dialog.findViewById(R.id.delete);

        TextInputEditText mRefreshRate = dialog.findViewById(R.id.refresh_rate);
        TextInputEditText mMemcConfig = dialog.findViewById(R.id.memc_configuration);
        TextInputLayout mMemcConfigLayout = dialog.findViewById(R.id.memc_configuration_layout);

        ((TextView) dialog.findViewById(R.id.packageName)).setText(memcAppModel.getPackageName());
        if (memcAppModel.isActivity()) {
            ((TextView) dialog.findViewById(R.id.activityName)).setText(memcAppModel.getActivityName());
            dialog.findViewById(R.id.refresh_rate_layout).setVisibility(View.GONE);
            mMemcConfigLayout.setHint("258-40-0-0");
        } else {
            dialog.findViewById(R.id.activity_layout).setVisibility(View.GONE);
            if (memcAppModel.getRefreshRate() != -1) mRefreshRate.setText(String.valueOf(memcAppModel.getRefreshRate()));
        }

        InputFilter[] filters = new InputFilter[] {
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i)) && source.charAt(i) != '-') {
                            return "";
                        }
                    }
                    return null;
                }
        };
        mMemcConfig.setFilters(filters);
        mMemcConfig.setText(memcAppModel.getMemcConfig());

        mCancel.setOnClickListener(v -> dialog.dismiss());
        mApply.setOnClickListener(v -> {
            if (TextUtils.isEmpty(mMemcConfig.getText())) {
                mMemcConfig.setError(context.getString(R.string.memc_config_error));
            }
            if (TextUtils.isEmpty(mRefreshRate.getText())) {
                mRefreshRate.setError(context.getString(R.string.refresh_rate_error));
            }

            if (memcAppModel.isActivity() && TextUtils.isEmpty(mMemcConfig.getText())) {
                return;
            } else if (!memcAppModel.isActivity() && (TextUtils.isEmpty(mRefreshRate.getText()) || TextUtils.isEmpty(mMemcConfig.getText()))) {
                return;
            }
            if (!memcAppModel.isActivity()) memcAppModel.setRefreshRate(Integer.parseInt(mRefreshRate.getText().toString()));
            memcAppModel.setMemcConfig(mMemcConfig.getText().toString());
            onApplyListener.onApplyMemc(memcAppModel);
            dialog.dismiss();
        });
        if (onDeleteListener != null) {
            mDelete.setOnClickListener(v -> {
                onDeleteListener.onDelete();
                dialog.dismiss();
            });
        } else {
            mDelete.setVisibility(MaterialButton.GONE);
        }

        dialog.create();
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
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

    @Override
    public void onDestroy() {
        dismiss();
        super.onDestroy();
    }

    public interface OnApplyListener {
        void onApplyMemc(MemcAppModel memcAppModel);
    }

    public interface OnDeleteListener {
        void onDelete();
    }

}
