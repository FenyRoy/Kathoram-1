package com.sample.kathoram.ViewHolders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sample.kathoram.R;

public class PagesViewHolder extends RecyclerView.ViewHolder {

    public TextView pageNameTextView,pageStatusTextView;
    public ImageButton pageStartRecord,pagePlayRecord,pageUpload,pageDelete;
    public PagesViewHolder(@NonNull View itemView) {
        super(itemView);

        pageNameTextView = itemView.findViewById(R.id.page_name);
        pageStartRecord =  itemView.findViewById(R.id.page_start_recording);
        pagePlayRecord = itemView.findViewById(R.id.page_play_recording);
        pageUpload = itemView.findViewById(R.id.page_upload_recording);
        pageDelete = itemView.findViewById(R.id.page_delete_recording);
        pageStatusTextView = itemView.findViewById(R.id.page_status);
    }
}
