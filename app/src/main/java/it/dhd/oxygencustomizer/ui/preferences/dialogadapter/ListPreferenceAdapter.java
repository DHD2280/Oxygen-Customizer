package it.dhd.oxygencustomizer.ui.preferences.dialogadapter;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.databinding.BatteryIconOptionsBinding;
import it.dhd.oxygencustomizer.databinding.PreferenceListItemBinding;
import it.dhd.oxygencustomizer.databinding.QsHeaderImageOptionsBinding;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;

public class ListPreferenceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int DEFAULT_TYPE = 0;
    public static final int TYPE_QS_IMAGE = 1;
    public static final int TYPE_BATTERY_ICONS = 2;
    private List<String> mResImages = new ArrayList<>();
    private final CharSequence[] mEntries;
    private final CharSequence[] mEntryValues;
    private final int[] mEntryIcons;
    private final Drawable[] mEntryDrawables;
    private final String mKey;
    private final boolean mHasImage;
    private onItemClickListener onItemClickListener;
    private String mValue;
    private int mType = DEFAULT_TYPE;
    private static final int HEADER_COUNT = 24;
    int prevPos = -1;


    public ListPreferenceAdapter(CharSequence[] entries,
                                 CharSequence[] entryValues,
                                 int[] entryIcons,
                                 String key,
                                 boolean hasImage,
                                 onItemClickListener onItemClickListener) {
        this.mEntries = entries;
        this.mEntryValues = entryValues;
        this.mEntryIcons = entryIcons;
        this.mEntryDrawables = null;
        this.mKey = key;
        this.mHasImage = hasImage;
        this.onItemClickListener = onItemClickListener;
        this.mType = DEFAULT_TYPE;
    }

    public ListPreferenceAdapter(CharSequence[] entries,
                                 CharSequence[] entryValues,
                                 Drawable[] entryDrawables,
                                 String key,
                                 boolean hasImage,
                                 onItemClickListener onItemClickListener) {
        this.mEntries = entries;
        this.mEntryValues = entryValues;
        this.mEntryDrawables = entryDrawables;
        this.mEntryIcons = null;
        this.mKey = key;
        this.mHasImage = hasImage;
        this.onItemClickListener = onItemClickListener;
        this.mType = DEFAULT_TYPE;
    }

    public void setListener(onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mValue = PreferenceHelper.instance.mPreferences.getString(mKey, "");
        PreferenceListItemBinding binding = PreferenceListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        QsHeaderImageOptionsBinding qsHeaderImageOptionsBinding = QsHeaderImageOptionsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        BatteryIconOptionsBinding batteryIconOptionsBinding = BatteryIconOptionsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        if (mType == TYPE_QS_IMAGE) {
            return new QsImageViewHolder(qsHeaderImageOptionsBinding);
        } else if (mType == TYPE_BATTERY_ICONS) {
            return new BatteryIconsViewHolder(batteryIconOptionsBinding);
        } else {
            return new ViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (mType == TYPE_QS_IMAGE) {
            int currentHeaderNumber = getCurrentHeaderNumber();

            String loadedImage = mResImages.get(position);

            Glide.with(((QsImageViewHolder) holder).binding.qsHeaderImage)
                    .load(getDrawable(((QsImageViewHolder) holder).binding.qsHeaderImage.getContext(), loadedImage))
                    .transform(new RoundedCorners(20))
                    .into(((QsImageViewHolder) holder).binding.qsHeaderImage);

            if (PreferenceHelper.instance.mPreferences.getInt("qs_header_image_number", -1) == (holder.getBindingAdapterPosition() + 1)) {
                ((QsImageViewHolder) holder).binding.rootLayout.setStrokeColor(getAppContext().getColor(android.R.color.system_accent1_400));
            } else {
                ((QsImageViewHolder) holder).binding.rootLayout.setStrokeColor(Color.TRANSPARENT);
            }

            holder.itemView.setOnClickListener(v -> {
                PreferenceHelper.instance.mPreferences.edit().putInt("qs_header_image_number", (holder.getBindingAdapterPosition() + 1)).apply();
                onItemClickListener.onItemClick(v, holder.getBindingAdapterPosition());
                notifyItemChanged(currentHeaderNumber);
                notifyItemChanged(holder.getBindingAdapterPosition());
            });
        } else if (mType == TYPE_BATTERY_ICONS) {
            ((BatteryIconsViewHolder)holder).binding.typeTitle.setText(mEntries[position]);

            if (mHasImage) {
                if (mEntryDrawables != null)
                    ((BatteryIconsViewHolder) holder).binding.batteryIcon.setImageDrawable(mEntryDrawables[position]);
                else
                    ((BatteryIconsViewHolder) holder).binding.batteryIcon.setImageDrawable(ContextCompat.getDrawable(((BatteryIconsViewHolder)holder).binding.getRoot().getContext(), mEntryIcons[position]));
            } else
                ((BatteryIconsViewHolder)holder).binding.batteryIcon.setVisibility(View.GONE);

            if (TextUtils.equals(mEntryValues[position].toString(), mValue)) {
                prevPos = position;
                ((BatteryIconsViewHolder)holder).binding.rootLayout.setStrokeColor(getAppContext().getColor(android.R.color.system_accent1_400));
            } else {
                ((BatteryIconsViewHolder)holder).binding.rootLayout.setStrokeColor(Color.TRANSPARENT);
            }

            ((BatteryIconsViewHolder)holder).binding.rootLayout.setOnClickListener(v -> {
                onItemClickListener.onItemClick(v, position);
                mValue = String.valueOf(mEntryValues[position]);
                notifyItemChanged(prevPos);
                notifyItemChanged(position);
            });
        } else {
            ((ViewHolder)holder).binding.text.setText(mEntries[position]);
            if (mHasImage) {
                if (mEntryIcons != null && mEntryIcons.length > 0)
                    ((ViewHolder)holder).binding.image.setImageDrawable(ContextCompat.getDrawable(((ViewHolder)holder).binding.getRoot().getContext(), mEntryIcons[position]));
                else if (mEntryDrawables != null && mEntryDrawables.length > 0)
                    ((ViewHolder)holder).binding.image.setImageDrawable(mEntryDrawables[position]);
            } else
                ((ViewHolder)holder).binding.image.setVisibility(View.GONE);


            if (TextUtils.equals(mEntryValues[position].toString(), mValue)) {
                prevPos = position;
                ((ViewHolder)holder).binding.rootLayout.setStrokeColor(getAppContext().getColor(android.R.color.system_accent1_400));
            } else {
                ((ViewHolder)holder).binding.rootLayout.setStrokeColor(Color.TRANSPARENT);
            }

            ((ViewHolder)holder).binding.rootLayout.setOnClickListener(v -> {
                onItemClickListener.onItemClick(v, position);
                mValue = String.valueOf(mEntryValues[position]);
                notifyItemChanged(prevPos);
                notifyItemChanged(position);
            });
        }

    }

    private int getCurrentHeaderNumber() {
        return PreferenceHelper.instance.mPreferences.getInt("qs_header_image_number", -1);
    }

    @Override
    public int getItemCount() {
        if (mType == TYPE_QS_IMAGE)
            return mResImages.size();
        else
            return mEntries.length;
    }


    public void setType(int type) {
        mType = type;
    }

    private Bitmap getBitmap(Context context, String drawableName) {
        return DrawableConverter.drawableToBitmap(getDrawable(context, drawableName));
    }

    public Drawable getDrawable(Context context, String drawableName) {
        Resources res = context.getResources();
        int resId = res.getIdentifier(drawableName, "drawable", BuildConfig.APPLICATION_ID);
        return ContextCompat.getDrawable(context, resId);
    }

    public int getType() {
        return mType;
    }

    public void setImages(List<String> images) {
        mResImages = images;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final PreferenceListItemBinding binding;

        ViewHolder(@NonNull PreferenceListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public static class QsImageViewHolder extends RecyclerView.ViewHolder {

        private final QsHeaderImageOptionsBinding binding;

        QsImageViewHolder(@NonNull QsHeaderImageOptionsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public static class BatteryIconsViewHolder extends RecyclerView.ViewHolder {

        private final BatteryIconOptionsBinding binding;

        BatteryIconsViewHolder(@NonNull BatteryIconOptionsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    /**
     * Interface for the click on the item
     */
    public interface onItemClickListener {
        void onItemClick(View view, int position);
    }

}
