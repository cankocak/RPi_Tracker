package com.cankocak.rpi_tracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class FeedRecyclerViewAdapter extends RecyclerView.Adapter<FeedRecyclerViewAdapter.ViewHolder> {
    private final Context context;
    private final List<RSS_Item> rssItems;

    private OnClickListener onClickListener;

    public FeedRecyclerViewAdapter(Context context,
                                   List<RSS_Item> rssItems,
                                   OnClickListener onClickListener) {
        this.context = context;
        this.rssItems = rssItems;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public FeedRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout, create the GUI
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.feed_recycler_view_row, parent, false);
        return new FeedRecyclerViewAdapter.ViewHolder(view, this.onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedRecyclerViewAdapter.ViewHolder holder, int position) {
        // Assign values to the GUI elements
        holder.textViewTitle.setText(rssItems.get(position).getTitle());
        holder.textViewDate.setText(rssItems.get(position).getDate().
                format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        holder.textViewDevice.setText(rssItems.get(position).getDevice());
        holder.textViewCountry.setText(rssItems.get(position).getCountry());
        holder.textViewVendor.setText(rssItems.get(position).getVendor());
    }

    @Override
    public int getItemCount() {
        // Return number of items in total
        return rssItems.size();
    }

    // Holds all the GUI elements of the row
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewTitle;
        TextView textViewDate;
        TextView textViewDevice;
        TextView textViewCountry;
        TextView textViewVendor;

        private FeedRecyclerViewAdapter.OnClickListener onClickListener;

        public ViewHolder(@NonNull View itemView,
                          FeedRecyclerViewAdapter.OnClickListener onClickListener) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewDevice = itemView.findViewById(R.id.textViewDevice);
            textViewCountry = itemView.findViewById(R.id.textViewCountry);
            textViewVendor = itemView.findViewById(R.id.textViewVendor);

            this.onClickListener = onClickListener;
            itemView.setOnClickListener(this);
        }

        // From View.OnClickListener
        @Override
        public void onClick(View view) {
            onClickListener.onClick(getAdapterPosition());
        }
    }

    public interface OnClickListener {
        public void onClick(int position);
    }
}
