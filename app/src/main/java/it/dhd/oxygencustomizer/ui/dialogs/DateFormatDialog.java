package it.dhd.oxygencustomizer.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.StringFormatter;

public class DateFormatDialog extends AppCompatActivity {

    Context context;
    Dialog dialog;
    StringFormatter formatter = new StringFormatter();

    public DateFormatDialog(Context context) {
        this.context = context;
    }

    public void show(String title, String text, OnApplyListener onApplyListener) {
        if (dialog != null) dialog.dismiss();

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_date_format_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        ((TextView) dialog.findViewById(R.id.title)).setText(title);

        MaterialButton mApply, mCancel;
        mApply = dialog.findViewById(R.id.apply);
        mCancel = dialog.findViewById(R.id.cancel);
        TextView mPreview = dialog.findViewById(R.id.preview);

        TextInputEditText formatText = dialog.findViewById(R.id.edit_text);
        formatText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SpannableStringBuilder formatted = new SpannableStringBuilder();
                formatted.append(formatter.formatString(s.toString()));
                mPreview.setText(formatted);
            }
        });

        formatText.setText(text);

        mCancel.setOnClickListener(v -> dialog.dismiss());
        mApply.setOnClickListener(v -> {
            onApplyListener.onApplyText(formatText.getText());
            dialog.dismiss();
        });

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
        void onApplyText(CharSequence value);
    }

}
