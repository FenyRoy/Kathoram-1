package com.sample.kathoram.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.sample.kathoram.Adapter.PagesAdapter;
import com.sample.kathoram.MainActivity;
import com.sample.kathoram.Models.BookPages;
import com.sample.kathoram.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookPagesActivity extends AppCompatActivity {


    String bookId;
    List<BookPages> bookPagesList=new ArrayList<>();
    DatabaseReference bookPageRef;

    FloatingActionButton bookPagesFab;
    RecyclerView bookPagesRecyclerview;

    Dialog bookPagesDialog;
    PagesAdapter pagesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_pages);

        bookId= getIntent().getStringExtra("id");
        if(bookId!=null)
        {

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

                    if(!dataSnapshot.hasChild("0"))
                    {
                        bookPagesList.add(new BookPages("0",true,"empty","preface"));

                    }
                    else
                    {   String uri  = dataSnapshot.child("0").getValue().toString();
                        bookPagesList.add(new BookPages("0",true,uri,"preface"));
                    }
                    if(!dataSnapshot.hasChild("1"))
                    {
                        bookPagesList.add(new BookPages("1",true,"empty","index"));

                    }
                    else
                    {   String uri  = dataSnapshot.child("1").getValue().toString();
                        bookPagesList.add(new BookPages("1",true,uri,"index"));
                    }

                    for(DataSnapshot snapshot:dataSnapshot.getChildren())
                    {

                        bookPagesList.add(new BookPages(snapshot.getKey(),false,snapshot.child(snapshot.getKey()).getValue().toString(),"page"));
                    }

                    pagesAdapter =  new PagesAdapter(BookPagesActivity.this,bookPagesList);
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
                    bookPagesDialog.setContentView(R.layout.layout_new_book);
                    bookPagesDialog.setCancelable(false);
                    bookPagesDialog.setCanceledOnTouchOutside(true);
                    bookPagesDialog.getWindow().getAttributes().windowAnimations = R.style.UpBottomSlideDialogAnimation;

                    Window window = bookPagesDialog.getWindow();
                    window.setGravity(Gravity.TOP);
                    window.setLayout(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    window.setDimAmount(0.75f);
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                    final EditText bookNameEditText = bookPagesDialog.findViewById(R.id.dialog_new_book_name_edittext);
                    Button Done, Cancel;
                    Done = bookPagesDialog.findViewById(R.id.dialog_new_book_done_btn);
                    Cancel = bookPagesDialog.findViewById(R.id.dialog_new_book_cancel_btn);

                    TextView bookMessage =  bookPagesDialog.findViewById(R.id.dialog_new_book_message_textview);
                    bookMessage.setText("Enter the new page number.");

                    Done.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (!TextUtils.isEmpty(bookNameEditText.getText().toString())) {

                                bookPageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.hasChild(bookNameEditText.getText().toString()))
                                        {
                                            Toast.makeText(BookPagesActivity.this, "Already has this page.", Toast.LENGTH_SHORT).show();

                                        }
                                        else
                                        {
                                            if(bookNameEditText.getText().toString().matches("[0-9]+") && bookNameEditText.getText().toString().length() > 1)
                                            {
                                                bookPageRef.setValue(bookNameEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {
                                                            bookPagesDialog.dismiss();
                                                            Toast.makeText(BookPagesActivity.this, "Created new page.", Toast.LENGTH_SHORT).show();

                                                        } else {
                                                            bookPagesDialog.dismiss();
                                                            Toast.makeText(BookPagesActivity.this, "Failed to create new page.", Toast.LENGTH_SHORT).show();

                                                        }

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                        Toast.makeText(BookPagesActivity.this, "Failed to create new page.", Toast.LENGTH_SHORT).show();
                                                        bookPagesDialog.dismiss();


                                                    }
                                                });
                                            }
                                            else
                                            {
                                                Toast.makeText(BookPagesActivity.this, "Enter only book page number.", Toast.LENGTH_SHORT).show();

                                            }


                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                        Toast.makeText(BookPagesActivity.this, "database error.", Toast.LENGTH_SHORT).show();


                                    }
                                });


                            } else {

                                Toast.makeText(BookPagesActivity.this, "fill in book page number.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                    Cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            bookPagesDialog.dismiss();

                        }
                    });

                    bookPagesDialog.show();

                }
            });
        }
    }
}
