package it.dhd.oxygencustomizer.ui.adapters.iconpack;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.LinkedList;
import java.util.List;

import it.dhd.oxygencustomizer.R;


public class IconDialogAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final List<ReplacementIcon> mReplacementIcons = new LinkedList<>();

    public IconDialogAdapter(Context context, List<ReplacementIcon> replacementIcons) {
        mReplacementIcons.addAll(replacementIcons);
        mInflater = LayoutInflater.from(context);
    }

    public static class ReplacementIcon implements Comparable<ReplacementIcon> {
        public final String packageName;
        public final String packName;
        public final String resName;
        public final String replacementName;
        public final Drawable icon;

        ReplacementIcon(String packageName, String pckName, String resDraw, String replDraw, Drawable icon) {
            this.packageName = packageName;
            this.packName = pckName;
            this.resName = resDraw;
            this.replacementName = replDraw;
            this.icon = icon;
        }

        @Override
        public int compareTo(ReplacementIcon other) {
            int result = replacementName.compareToIgnoreCase(other.replacementName);
            return result != 0 ? result : packName.compareTo(other.packName);
        }

        @NonNull
        @Override
        public String toString() {
            return "IconDialogAdapter.ReplacementIcon{" +
                    "packageName='" + packageName + '\'' +
                    ", packName='" + packName + '\'' +
                    ", resName='" + resName + '\'' +
                    ", replacementName='" + replacementName + '\'' +
                    '}';
        }

    }

    @Override
    public int getCount() {
        synchronized (mReplacementIcons) {
            return mReplacementIcons.size();
        }
    }

    @Override
    public ReplacementIcon getItem(int position) {
        synchronized (mReplacementIcons) {
            return mReplacementIcons.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        synchronized (mReplacementIcons) {
            return mReplacementIcons.get(position).replacementName.hashCode();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.applist_preference_icon, null, false);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.title = convertView.findViewById(R.id.title);
            holder.summary = convertView.findViewById(R.id.summary);
            holder.icon = convertView.findViewById(R.id.icon);
            holder.divider = convertView.findViewById(R.id.divider);
        }

        ReplacementIcon applicationInfo = getItem(position);
        holder.title.setText(applicationInfo.packName);
        holder.icon.setImageDrawable(applicationInfo.icon);
        TypedValue typedValue = new TypedValue();
        convertView.getContext().getTheme().resolveAttribute(R.attr.colorOnSurface, typedValue, true);
        int color = ContextCompat.getColor(convertView.getContext(), typedValue.resourceId);
        holder.icon.setImageTintList(ColorStateList.valueOf(color));
        holder.summary.setVisibility(View.GONE);

        holder.divider.setVisibility(position == getCount() - 1 ? View.GONE : View.VISIBLE);

        return convertView;
    }

    private static class ViewHolder {
        TextView title;
        TextView summary;
        ImageView icon;
        ImageView divider;
    }

}
