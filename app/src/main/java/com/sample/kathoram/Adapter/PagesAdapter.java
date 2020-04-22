package com.sample.kathoram.Adapter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sample.kathoram.Models.BookPages;
import com.sample.kathoram.R;
import com.sample.kathoram.ViewHolders.PagesViewHolder;

import java.util.List;

public class PagesAdapter extends RecyclerView.Adapter<PagesViewHolder>{

    Context context;
    List<BookPages> bookPagesList;

    public PagesAdapter(Context context, List<BookPages> bookPagesList) {
        this.context = context;
        this.bookPagesList = bookPagesList;
    }

    @NonNull
    @Override
    public PagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PagesViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_page_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull PagesViewHolder holder, int position) {

        holder.pageNameTextView.setText(bookPagesList.get(position).getPageType()+" "+bookPagesList.get(position).getPageNo());
        if(bookPagesList.get(position).getUriPath().equals("empty"))
        {
         holder.pageStatusTextView.setText("Status : Book has no recording for this page.");
        }
        else
        {
            holder.pageStatusTextView.setVisibility(View.GONE);
        }


        holder.pageStartRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.getPackageName()));
                        context.startActivity(intent);
                    }
                    Toast.makeText(context, "enable permissions", Toast.LENGTH_SHORT).show();

                }
                else
                {
                        
                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return bookPagesList.size();
    }
}
