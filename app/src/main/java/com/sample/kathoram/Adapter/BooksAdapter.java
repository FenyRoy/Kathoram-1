package com.sample.kathoram.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sample.kathoram.Activities.BookPagesActivity;
import com.sample.kathoram.Models.Books;
import com.sample.kathoram.R;
import com.sample.kathoram.ViewHolders.BooksViewHolder;

import java.util.Date;
import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksViewHolder> {

    Context ctx;
    List<Books> booksList;

    public BooksAdapter(Context ctx, List<Books> booksList) {
        this.ctx = ctx;
        this.booksList = booksList;
    }

    @NonNull
    @Override
    public BooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BooksViewHolder(LayoutInflater.from(ctx).inflate(R.layout.layout_book_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull BooksViewHolder holder, final int position) {

        holder.bookNameTextView.setText(booksList.get(position).getName());
        try
        {
            long time = Long.parseLong(booksList.get(position).getTime());
            CharSequence Time = DateUtils.getRelativeDateTimeString(ctx, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
            String timesubstring = Time.toString().substring(Time.length() - 8);
            Date date = new Date(time);
            String dateformat = DateFormat.format("dd-MM-yyyy", date).toString();
            String dateandTime = dateformat + " @ " + timesubstring;
            holder.bookTimeTextView.setText(dateandTime);

        }
        catch (NumberFormatException e)
        {
            holder.bookTimeTextView.setText("Not available");

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ctx.startActivity(new Intent(ctx, BookPagesActivity.class).putExtra("id",booksList.get(position).getBookid()));

            }
        });

    }

    @Override
    public int getItemCount() {
        return booksList.size();
    }
}
