package com.sample.kathoram.Adapter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sample.kathoram.Models.BookPages;
import com.sample.kathoram.R;
import com.sample.kathoram.ViewHolders.PagesViewHolder;

import java.io.IOException;
import java.util.List;

public class PagesAdapter extends RecyclerView.Adapter<PagesViewHolder>{

    Context context;
    List<BookPages> bookPagesList;
    MediaPlayer mediaPlayer;
    FirebaseStorage storageReference;
    DatabaseReference pageRef;
    Dialog audioDialog;
    int length;
    final TextView playResumeTextView;
    final ImageButton playBtn,pauseBtn,stopBtn;
    ProgressBar audioProgressbar;
    LinearLayout playLL,stopLL,pauseLL;


    public PagesAdapter(Context context, List<BookPages> bookPagesList, FirebaseStorage reference, DatabaseReference bookPageRef) {
        this.context = context;
        this.bookPagesList = bookPagesList;
        mediaPlayer = new MediaPlayer();
        storageReference = reference;
        pageRef =  bookPageRef;
        length=0;
        audioDialog = new Dialog(context);
        audioDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        audioDialog.setContentView(R.layout.layout_audio_dialog);
        audioDialog.setCancelable(false);
        audioDialog.setCanceledOnTouchOutside(true);
        audioDialog.getWindow().getAttributes().windowAnimations = R.style.UpBottomSlideDialogAnimation;

        Window window = audioDialog.getWindow();
        window.setGravity(Gravity.TOP);
        window.setLayout(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0.75f);

        playLL = audioDialog.findViewById(R.id.playWrapper);
        stopLL = audioDialog.findViewById(R.id.stopWrapper);
        pauseLL = audioDialog.findViewById(R.id.pauseWrapper);

        playResumeTextView = audioDialog.findViewById(R.id.dialog_play_textview);

        playBtn =  audioDialog.findViewById(R.id.dialog_play);
        pauseBtn = audioDialog.findViewById(R.id.dialog_pause);
        stopBtn =  audioDialog.findViewById(R.id.dialog_stop);
        stopLL.setVisibility(View.GONE);
        audioProgressbar = audioDialog.findViewById(R.id.dialog_audio_progressbar);


    }

    @NonNull
    @Override
    public PagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PagesViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_page_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final PagesViewHolder holder, final int position) {

        holder.pageNameTextView.setText("Page "+bookPagesList.get(position).getPageNo());
        if(bookPagesList.get(position).getUriPath().equals("empty"))
        {
         holder.pageStatusTextView.setText("Status : Book has no recording for this page.");
        }
        else
        {
            holder.pageStatusTextView.setVisibility(View.GONE);
        }

        holder.pagePlayRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!bookPagesList.get(position).getUriPath().equals("empty"))
                {

                    playBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            try {
                                playLL.setVisibility(View.GONE);
                                stopLL.setVisibility(View.VISIBLE);
                                mediaPlayer = new MediaPlayer();
                                mediaPlayer.setDataSource(bookPagesList.get(position).getUriPath());
                                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        mediaPlayer.seekTo(0);
                                        mp.start();
                                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mp) {

                                                playLL.setVisibility(View.VISIBLE);
                                                stopLL.setVisibility(View.GONE);
                                                pauseBtn.setImageResource(R.drawable.ic_pause);
                                                mp.seekTo(0);
                                            }
                                        });
                                    }
                                });
                                Toast.makeText(context, "Player started.", Toast.LENGTH_SHORT).show();
                                mediaPlayer.prepare();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });


                    pauseBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(mediaPlayer.isPlaying())
                            {
                                mediaPlayer.pause();
                                Toast.makeText(context, "Player paused.", Toast.LENGTH_SHORT).show();
                                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                                pauseBtn.setImageResource(R.drawable.ic_play);
                            }
                            else
                            {
                                if(mediaPlayer.getCurrentPosition()!=mediaPlayer.getDuration())
                                {
                                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                                    mediaPlayer.start();
                                    Toast.makeText(context, "Player resumed.", Toast.LENGTH_SHORT).show();
                                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                                    pauseBtn.setImageResource(R.drawable.ic_pause);
                                }
                                else if(mediaPlayer.getCurrentPosition()==mediaPlayer.getDuration())
                                {
                                    pauseBtn.setImageResource(R.drawable.ic_play);
                                    mediaPlayer.seekTo(0);

                                }

                                else
                                {
                                    mediaPlayer.seekTo(0);
                                    mediaPlayer.start();
                                    Toast.makeText(context, "Player starting again.", Toast.LENGTH_SHORT).show();
                                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                                    pauseBtn.setImageResource(R.drawable.ic_pause);
                                }

                            }



                        }
                    });

                    stopBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pauseBtn.setImageResource(R.drawable.ic_pause);
                            playLL.setVisibility(View.VISIBLE);
                            stopLL.setVisibility(View.GONE);
                            Toast.makeText(context, "Player stopped.", Toast.LENGTH_SHORT).show();
                            mediaPlayer.stop();
                            mediaPlayer.release();
                        }
                    });

                    audioDialog.show();

                }
                else
                {
                    Toast.makeText(context, "Cannot play this audio.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        holder.pageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!bookPagesList.get(position).getUriPath().equals("empty"))
                {
                    final StorageReference audioRef = storageReference.getReferenceFromUrl(bookPagesList.get(position).getUriPath());
                    AlertDialog.Builder builder = new AlertDialog.Builder(context)
                            .setTitle("Delete page audio")
                            .setMessage("Are you sure you want to delete this audio. This process cannot be undone.")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    audioRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            pageRef.child(bookPagesList.get(position).getKey()).removeValue();
                                            holder.pageProgressbar.setVisibility(View.VISIBLE);


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {

                                            Toast.makeText(context, "Unable to delete this audio.", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                }
                else
                {
                    Toast.makeText(context, "Cannot delete this audio.", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return bookPagesList.size();
    }
}
