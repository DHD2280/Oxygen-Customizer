package it.dhd.oxygencustomizer.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import it.dhd.oxygencustomizer.R;

public class PopupMenuAdapter extends ArrayAdapter<CharSequence> {

    private final int selectedIndex;

    public PopupMenuAdapter(Context context, int resource, CharSequence[] objects, int selectedIndex) {
        super(context, resource, objects);
        this.selectedIndex = selectedIndex;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Customize how the selected item is displayed in the ListPopupWindow
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view;
        if (position == selectedIndex) {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent)); // Customize selected color
        } else {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.textColorPrimary)); // Default color
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        // Customize the dropdown list
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = (TextView) view;
        if (position == selectedIndex) {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent)); // Customize selected color
        } else {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.textColorPrimary)); // Default color
        }
        return view;
    }
}