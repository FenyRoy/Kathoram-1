package com.sample.kathoram.Adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
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
import java.util.Timer;
import java.util.TimerTask;

public class PagesAdapter extends RecyclerView.Adapter<PagesViewHolder>{

    Context context;
    List<BookPages> bookPagesList;
    MediaPlayer mediaPlayer;
    FirebaseStorage storageReference;
    DatabaseReference pageRef;
    Dialog audioDialog;
    int length;
    final TextView playResumeTextView,pauseResumeTextView,audioProgressTextView,audioTotalProgressTextView;
    final ImageButton playBtn,pauseBtn,stopBtn;
    ProgressBar audioProgressbar;
    LinearLayout playLL,stopLL,pauseLL;
    Handler customHandler = new Handler();
    long startTime,timeInMillis,timeSwapBuff,updateTime;
    int elaspedSec;
    int elaspedMillis;

    final Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {

            timeInMillis = System.currentTimeMillis()-startTime;
            updateTime=timeSwapBuff+timeInMillis;
            int sec = (int) (updateTime/1000);
            double timeElapsed = updateTime/100;
            int progress = (int) (updateTime/100);
            elaspedSec= sec;
            elaspedMillis= (int)(updateTime%1000);
            if(mediaPlayer!=null && mediaPlayer.isPlaying() &&(mediaPlayer.getDuration())>=updateTime)
            {
                if(audioProgressTextView!=null)
                {

                    audioProgressTextView.setText(((double)timeElapsed/10)+"s");


                }
                if(audioProgressbar!=null)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        audioProgressbar.setProgress(progress,true);
                    }
                    else
                    {
                        audioProgressbar.setProgress(progress);
                    }
                }
            }
            customHandler.postDelayed(this,100);

        }
    };
    public PagesAdapter(Context context, List<BookPages> bookPagesList, FirebaseStorage reference, DatabaseReference bookPageRef) {
        this.context = context;
        this.bookPagesList = bookPagesList;
        mediaPlayer = new MediaPlayer();
        storageReference = reference;
        pageRef =  bookPageRef;
        length=0;

        startTime=0L;
        timeInMillis=0L;
        timeSwapBuff=0L;
        updateTime=0L;
        elaspedSec=0;
        elaspedMillis=0;

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
        pauseResumeTextView = audioDialog.findViewById(R.id.dialog_pause_textview);

        audioProgressTextView = audioDialog.findViewById(R.id.dialog_audio_progress_textview);
        audioTotalProgressTextView =  audioDialog.findViewById(R.id.dialog_audio_total_progress_textview);

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

        holder.pageNameTextView.setText("Page : "+bookPagesList.get(position).getPageNo());
        holder.pageDescTextView.setText("Desc : "+bookPagesList.get(position).getPageDesc());

        holder.pagePlayRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!bookPagesList.get(position).getUriPath().equals("empty"))
                {
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(bookPagesList.get(position).getUriPath());
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                playLL.setVisibility(View.GONE);
                                stopLL.setVisibility(View.VISIBLE);
                                mediaPlayer.start();
                                Toast.makeText(context, "Player started.", Toast.LENGTH_SHORT).show();
                                startTime = System.currentTimeMillis()-mediaPlayer.getCurrentPosition();
                                customHandler.postDelayed(updateTimerThread,0);

                            }
                        });
                        mediaPlayer.prepare();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.seekTo(0);
                    audioProgressTextView.setText("0s");
                    double totalTime =  (double)mediaPlayer.getDuration()/1000;
                    audioTotalProgressTextView.setText(totalTime+"s");

                    audioProgressbar.setProgress(0);
                    audioProgressbar.setMax(mediaPlayer.getDuration()/100);


                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {

                            playLL.setVisibility(View.VISIBLE);
                            stopLL.setVisibility(View.GONE);
                            pauseBtn.setImageResource(R.drawable.ic_pause);
                            mp.seekTo(0);
                            audioProgressbar.setProgress(0);
                            audioProgressTextView.setText("0s");
                            customHandler.removeCallbacks(updateTimerThread);
                        }
                    });



                    playBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            playLL.setVisibility(View.GONE);
                            stopLL.setVisibility(View.VISIBLE);
                            mediaPlayer.start();
                            startTime = System.currentTimeMillis()-mediaPlayer.getCurrentPosition();
                            customHandler.postDelayed(updateTimerThread,0);
                        }
                    });



                    pauseBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(mediaPlayer.isPlaying())
                            {   customHandler.removeCallbacks(updateTimerThread);
                                mediaPlayer.pause();
                                Toast.makeText(context, "Player paused.", Toast.LENGTH_SHORT).show();
                                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                                pauseBtn.setImageResource(R.drawable.ic_play);
                                pauseResumeTextView.setText("resume");


                            }
                            else
                            {
                                startTime = System.currentTimeMillis()-mediaPlayer.getCurrentPosition();
                                customHandler.postDelayed(updateTimerThread,0);
                                pauseResumeTextView.setText("pause");
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
