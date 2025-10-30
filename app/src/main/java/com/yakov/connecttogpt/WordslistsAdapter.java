package com.yakov.connecttogpt;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WordslistsAdapter extends RecyclerView.Adapter<WordslistsAdapter.ViewHolder> {
    Context mContext;
    List<String[]> mList;

    public WordslistsAdapter(Context context, List<String[]> list){
        mContext = context;
        mList = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView numberTextView;
        public TextView titleTextView;
        public TextView descriptionTextView;
        public Button actionButton;
        private ItemClickListener itemClickListener;

        public ViewHolder(View itemView) {
            super(itemView);
            numberTextView = itemView.findViewById(R.id.numberTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            actionButton = itemView.findViewById(R.id.actionButton);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            itemClickListener.onItemClick(position);
                        }
                    }
                }
            });
        }
        public void setItemClickListener(ItemClickListener listener) {
            itemClickListener = listener;
        }


    }
    public interface ItemClickListener {
        void onItemClick(int position);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view and inflate the layout for each item
        // Example:
        // View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        // ViewHolder viewHolder = new ViewHolder(itemView);
        // return viewHolder;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wordslists, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Bind data to the views in each item
        // Example:
        // String item = itemList.get(position);
        // holder.textView.setText(item);
        // Set an OnClickListener for the button if needed
        holder.numberTextView.setText(String.valueOf(position));
        holder.titleTextView.setText(mList.get(position)[0]);
        holder.descriptionTextView.setText(mList.get(position)[1]);

        // Set the item click listener
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Handle item click
            }
        });
        holder.actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle button click
            }
        });
    }

    @Override
    public int getItemCount() {
        // Return the number of items in the data source
        return mList.size();
    }

}
