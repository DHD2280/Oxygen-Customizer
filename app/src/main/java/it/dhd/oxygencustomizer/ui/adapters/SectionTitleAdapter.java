package it.dhd.oxygencustomizer.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.dhd.oxygencustomizer.R;

public class SectionTitleAdapter extends RecyclerView.Adapter<SectionTitleAdapter.ViewHolder> {

    private final String title;

    public SectionTitleAdapter(String title) {
        this.title = title;
    }

    @NonNull
    @Override
    public SectionTitleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_list_option_section, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionTitleAdapter.ViewHolder holder, int position) {
        holder.bind(title);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
        }

        public void bind(String title) {
            mTitle.setText(title);
        }

    }

}
