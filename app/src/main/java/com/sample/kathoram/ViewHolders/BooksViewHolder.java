package com.sample.kathoram.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sample.kathoram.R;

public class BooksViewHolder extends RecyclerView.ViewHolder {

    public TextView bookNameTextView,bookTimeTextView;

    public BooksViewHolder(@NonNull View itemView) {
        super(itemView);

        bookNameTextView =  itemView.findViewById(R.id.book_name);
        bookTimeTextView = itemView.findViewById(R.id.book_time);
    }
}
