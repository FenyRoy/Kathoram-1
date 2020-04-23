package com.sample.kathoram.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sample.kathoram.Adapter.PagesAdapter;
import com.sample.kathoram.MainActivity;
import com.sample.kathoram.Models.BookPages;
import com.sample.kathoram.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookPagesActivity extends AppCompatActivity {


    String bookId;
    List<BookPages> bookPagesList = new ArrayList<>();
    DatabaseReference bookPageRef;

    FloatingActionButton bookPagesFab;
    RecyclerView bookPagesRecyclerview;

    Dialog bookPagesDialog;
    PagesAdapter pagesAdapter;


    MediaRecorder mediaRecorder;

    private static String fileName = null;
    private StorageReference storageReference;

    Handler customHandler = new Handler();
    long startTime=0L,timeInMillis=0L,timeSwapBuff=0L,updateTime=0L;
    int elaspedSec=0,elaspedMin=0,elaspedMillis=0;

    TextView timeElapsedTextView,recordStatusTexview,startRecordingTextView;

    final Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {

            timeInMillis = System.currentTimeMillis()-startTime;
            updateTime=timeSwapBuff+timeInMillis;
            int sec = (int) (updateTime/1000);
            elaspedSec=sec;
            int mins = (int) (sec/60);
            elaspedMin=mins;
            sec%=60;
            int milliseconds = (int)(updateTime%1000);
            elaspedMillis=milliseconds;
            if(timeElapsedTextView!=null)
            {
                timeElapsedTextView.setText(String.format("Time Elapsed : %d m : %2d s ", mins, sec));

            }
            customHandler.postDelayed(this,100);

        }
    };

    CountDownTimer countDownTimer;

    static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO=101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_pages);

        bookId = getIntent().getStringExtra("id");
        if (bookId != null) {


            storageReference = FirebaseStorage.getInstance().getReference();
            fileName = this.getExternalFilesDir(null).getAbsolutePath() + System.currentTimeMillis() + "_raw_audio.3gp";
            Log.i("Maintag", fileName);

            bookPagesFab = findViewById(R.id.book_pages_add_pages_fab);
            bookPagesRecyclerview = findViewById(R.id.book_pages_recyclerview);
            bookPagesRecyclerview.setHasFixedSize(true);
            bookPagesRecyclerview.setLayoutManager(new LinearLayoutManager(this));


            bookPageRef = FirebaseDatabase.getInstance().getReference().child("Books").child(bookId).child("pages");
            bookPageRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    bookPagesList.clear();
                    bookPagesRecyclerview.removeAllViews();

                    /*
                    if (!dataSnapshot.hasChild("0")) {
                        bookPagesList.add(new BookPages("0", true, "empty", "preface"));

                    } else {
                        String uri = dataSnapshot.child("0").getValue().toString();
                        bookPagesList.add(new BookPages("0", true, uri, "preface"));
                    }
                    if (!dataSnapshot.hasChild("1")) {
                        bookPagesList.add(new BookPages("1", true, "empty", "index"));

                    } else {
                        String uri = dataSnapshot.child("1").getValue().toString();
                        bookPagesList.add(new BookPages("1", true, uri, "index"));
                    }
                     */

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        String key = snapshot.getKey();
                        String pageno="empty",uri="empty";
                        if (snapshot.hasChild("pageno")) {
                             pageno = snapshot.child("pageno").getValue().toString();
                        }
                        if(snapshot.hasChild("uriPath"))
                        {
                            uri = snapshot.child("uriPath").getValue().toString();
                        }

                         bookPagesList.add(new BookPages(key,pageno,uri));

                    }

                    pagesAdapter = new PagesAdapter(BookPagesActivity.this, bookPagesList,FirebaseStorage.getInstance(),bookPageRef);
                    bookPagesRecyclerview.setAdapter(pagesAdapter);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            bookPagesDialog = new Dialog(this);
            bookPagesFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    bookPagesDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    bookPagesDialog.setContentView(R.layout.layout_new_page);
                    bookPagesDialog.setCancelable(false);
                    bookPagesDialog.setCanceledOnTouchOutside(false);
                    bookPagesDialog.getWindow().getAttributes().windowAnimations = R.style.UpBottomSlideDialogAnimation;

                    Window window = bookPagesDialog.getWindow();
                    window.setGravity(Gravity.TOP);
                    window.setLayout(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    window.setDimAmount(0.75f);
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                    final EditText pageNoEditText = bookPagesDialog.findViewById(R.id.dialog_new_page_no_edittext);
                    final ImageButton start, stop;
                    start = bookPagesDialog.findViewById(R.id.dialog_start_recording);
                    stop = bookPagesDialog.findViewById(R.id.dialog_stop_recording);
                    final ProgressBar progressBar = bookPagesDialog.findViewById(R.id.new_page_progressbar);
                    progressBar.setVisibility(View.INVISIBLE);

                    timeElapsedTextView = bookPagesDialog.findViewById(R.id.dialog_new_page_time_elapsed_textview);
                    recordStatusTexview = bookPagesDialog.findViewById(R.id.dialog_new_page_record_status_textview);
                    startRecordingTextView = bookPagesDialog.findViewById(R.id.dialog_start_recording_textview);

                    recordStatusTexview.setText("Record Status : Not Started");
                    timeElapsedTextView.setText("Time Elapsed : 0m: 00s : 00ms");

                    Button cancel = bookPagesDialog.findViewById(R.id.dialog_new_page_cancel_btn);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            bookPagesDialog.dismiss();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(pageNoEditText.getWindowToken(), 0);

                        }
                    });

                    Button done = bookPagesDialog.findViewById(R.id.dialog_new_page_done_btn);


                    countDownTimer = new CountDownTimer(5000,1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                            recordStatusTexview.setText("Recording Status : Starting in "+millisUntilFinished/1000+"s");

                        }

                        @Override
                        public void onFinish() {

                            startRecordingTextView.setText("Restart");
                            startTime = System.currentTimeMillis();
                            customHandler.postDelayed(updateTimerThread,500);
                            mediaRecorder = new MediaRecorder();
                            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                            mediaRecorder.setOutputFile(fileName);
                            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


                            try {
                                mediaRecorder.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            mediaRecorder.start();
                            //Toast.makeText(BookPagesActivity.this, "started recording.", Toast.LENGTH_SHORT).show();
                            recordStatusTexview.setText("Record Status : Recording");

                        }
                    };

                    done.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            if(!TextUtils.isEmpty(pageNoEditText.getText().toString()))
                            {
                                UploadFile(fileName, pageNoEditText.getText().toString());
                                progressBar.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                Toast.makeText(BookPagesActivity.this, "give a page number.", Toast.LENGTH_SHORT).show();
                            }

                            if(mediaRecorder !=null)
                            {
                                recordStatusTexview.setText("Record Status : Recording Stopped");
                                mediaRecorder.stop();
                                mediaRecorder.release();
                                mediaRecorder = null;
                                customHandler.removeCallbacks(updateTimerThread);
                            }

                            if(countDownTimer!=null)
                            {
                                countDownTimer.cancel();
                            }
                        }
                    });

                    start.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (ContextCompat.checkSelfPermission(BookPagesActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                                if (ActivityCompat.shouldShowRequestPermissionRationale(BookPagesActivity.this,
                                        Manifest.permission.RECORD_AUDIO)) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(BookPagesActivity.this)
                                            .setTitle("Permission Required")
                                            .setCancelable(true)
                                            .setMessage("App need audio recording permission to record audio. Please allow the permission to continue further.")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    ActivityCompat.requestPermissions(BookPagesActivity.this,
                                                            new String[]{Manifest.permission.RECORD_AUDIO},
                                                            MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    dialog.dismiss();

                                                }
                                            });

                                    builder.show();

                                } else {
                                    ActivityCompat.requestPermissions(BookPagesActivity.this,
                                            new String[]{Manifest.permission.RECORD_AUDIO},
                                            MY_PERMISSIONS_REQUEST_RECORD_AUDIO);


                                }
                            }
                            else {
                                recordStatusTexview.setText("Record Status : Not Started");
                                timeElapsedTextView.setText("Time Elapsed : 0m: 00s : 00ms");
                                customHandler.removeCallbacks(updateTimerThread);
                                countDownTimer.cancel();
                                countDownTimer.start();
                            }



                        }
                    });


                    stop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (mediaRecorder != null) {
                                recordStatusTexview.setText("Record Status : Recording Stopped");
                                mediaRecorder.stop();
                                mediaRecorder.release();
                                mediaRecorder = null;
                                customHandler.removeCallbacks(updateTimerThread);
                            }
                            if(countDownTimer!=null)
                            {
                                countDownTimer.cancel();
                            }

                        }
                    });


                    bookPagesDialog.show();

                }
            });
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if(countDownTimer!=null)
                    {
                        countDownTimer.cancel();
                        countDownTimer.start();

                    }

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void UploadFile(final String fileName, final String pageno) {

        final StorageReference filepath = storageReference.child(bookId).child(System.currentTimeMillis() + "_audio.3gp");
        Uri uri = Uri.fromFile(new File(fileName));
        filepath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            bookPagesDialog.dismiss();
                            String pushid = bookPageRef.push().getKey();
                            bookPageRef.child(pushid).child("uriPath").setValue(String.valueOf(uri));
                            bookPageRef.child(pushid).child("pageno").setValue(pageno);
                            Toast.makeText(BookPagesActivity.this, "Uploaded file", Toast.LENGTH_SHORT).show();


                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {


                                    bookPagesDialog.dismiss();
                                    Toast.makeText(BookPagesActivity.this, "Failed to upload file.", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(BookPagesActivity.this, "File saved at : " + fileName, Toast.LENGTH_SHORT).show();
                                    Log.i("mainTag",e.toString());

                                }
                            });



                } else {
                    bookPagesDialog.dismiss();
                    Toast.makeText(BookPagesActivity.this, "Failed to upload file.", Toast.LENGTH_SHORT).show();
                    Toast.makeText(BookPagesActivity.this, "File saved at : " + fileName, Toast.LENGTH_SHORT).show();
                    Log.i("mainTag",task.getException().toString());
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                bookPagesDialog.dismiss();
                Toast.makeText(BookPagesActivity.this, "Failed to upload file.", Toast.LENGTH_SHORT).show();
                Toast.makeText(BookPagesActivity.this, "File saved at : " + fileName, Toast.LENGTH_SHORT).show();
                Log.i("mainTag",e.toString());
            }
        });
    }


}
