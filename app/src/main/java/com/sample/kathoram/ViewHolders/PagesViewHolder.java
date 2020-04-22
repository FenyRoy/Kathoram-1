package com.sample.kathoram.ViewHolders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sample.kathoram.R;

public class PagesViewHolder extends RecyclerView.ViewHolder {

    public TextView pageNameTextView,pageStatusTextView;
    public ImageButton pagePlayRecord,pageDelete;
    public ProgressBar pageProgressbar;

    public PagesViewHolder(@NonNull View itemView) {
        super(itemView);

        pageNameTextView = itemView.findViewById(R.id.page_name);
        pagePlayRecord = itemView.findViewById(R.id.page_play_recording);
        pageDelete = itemView.findViewById(R.id.page_delete_recording);
        pageStatusTextView = itemView.findViewById(R.id.page_status);
        pageProgressbar = itemView.findViewById(R.id.page_progressbar);
    }
}
