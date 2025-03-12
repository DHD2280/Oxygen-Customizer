package it.dhd.oxygencustomizer.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.widgets.FooterWidget;

public class FooterWidgetAdapter extends RecyclerView.Adapter<FooterWidgetAdapter.ViewHolder> {

    private final String title;
    private final View.OnClickListener mLearnMoreListener;

    public FooterWidgetAdapter(String title, View.OnClickListener listener) {
        this.title = title;
        this.mLearnMoreListener = listener;
    }

    @NonNull
    @Override
    public FooterWidgetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_widget_footer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FooterWidgetAdapter.ViewHolder holder, int position) {
        holder.bind(title, mLearnMoreListener);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements OplusRecyclerView.IOplusDividerDecorationInterface{

        private FooterWidget mFooterWidget;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mFooterWidget = itemView.findViewById(R.id.footer_widget);
        }

        public void bind(String title, View.OnClickListener listener) {
            mFooterWidget.setTitle(title);
            mFooterWidget.setLearnMoreAction(listener);
        }

    }

}
