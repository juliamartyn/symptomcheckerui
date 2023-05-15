package com.medcare.symptomchecker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.medcare.symptomchecker.R;

import java.util.List;

public class ChosenItemsAdapter extends RecyclerView.Adapter<ChosenItemsAdapter.ViewHolder> {

    private List<String> chosenItems;
    private OnCloseClickListener onCloseClickListener;

    public ChosenItemsAdapter(List<String> chosenItems) {
        this.chosenItems = chosenItems;
    }

    public void setOnCloseClickListener(OnCloseClickListener onCloseClickListener) {
        this.onCloseClickListener = onCloseClickListener;
    }

    public interface OnCloseClickListener {
        void onCloseClick(int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chosen_item_tile, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String item = chosenItems.get(position);
        holder.chosenItemTextView.setText(item);

        holder.closeButton.setOnClickListener(v -> {
            if (onCloseClickListener != null) {
                onCloseClickListener.onCloseClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chosenItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView chosenItemTextView;
        public ImageButton closeButton;

        public ViewHolder(View itemView) {
            super(itemView);
            chosenItemTextView = itemView.findViewById(R.id.chosenItemTextView);
            closeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}



