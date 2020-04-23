package com.sample.kathoram.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.sample.kathoram.Activities.BookPagesActivity;
import com.sample.kathoram.Models.Books;
import com.sample.kathoram.R;
import com.sample.kathoram.ViewHolders.BooksViewHolder;

import java.util.Date;
import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksViewHolder> {

    Context ctx;
    List<Books> booksList;
    AlertDialog.Builder deleteBookDialog;
    DatabaseReference bookRef;

    public BooksAdapter(Context ctx, List<Books> booksList, DatabaseReference booksRef) {
        this.ctx = ctx;
        this.booksList = booksList;
        deleteBookDialog =  new AlertDialog.Builder(ctx)
        .setCancelable(true)
        .setTitle("Delete Book")
        .setMessage("Are you sure you want to delete this book? This process cannot be undone.");

        bookRef = booksRef;
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

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                deleteBookDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        bookRef.child(booksList.get(position).getBookid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful())
                                {
                                    Toast.makeText(ctx, "Deleted book successfully.", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(ctx, "Failed to deleted book.", Toast.LENGTH_SHORT).show();

                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(ctx, "Failed to deleted book.", Toast.LENGTH_SHORT).show();


                            }
                        });

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                deleteBookDialog.show();

                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return booksList.size();
    }
}
