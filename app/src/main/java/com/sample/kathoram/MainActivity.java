package com.sample.kathoram;

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
import com.sample.kathoram.Adapter.BooksAdapter;
import com.sample.kathoram.Models.Books;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    DatabaseReference booksRef;
    List<Books> bookList = new ArrayList<>();
    public static final String NOT_AVAILABLE = "-N-A-";
    BooksAdapter booksAdapter;

    FloatingActionButton mainFab;
    RecyclerView mainRecyclerview;

    Dialog bookDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainFab = findViewById(R.id.main_add_books_fab);
        mainRecyclerview = findViewById(R.id.main_recyclerview);
        mainRecyclerview.setHasFixedSize(true);
        mainRecyclerview.setLayoutManager(new LinearLayoutManager(this));

        booksRef = FirebaseDatabase.getInstance().getReference().child("Books");
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mainRecyclerview.removeAllViews();
                bookList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String name = NOT_AVAILABLE, time = NOT_AVAILABLE;
                    if (snapshot.hasChild("name")) {
                        name = snapshot.child("name").getValue().toString();
                    }
                    if (snapshot.hasChild("time")) {
                        time = snapshot.child("time").getValue().toString();
                    }

                    bookList.add(new Books(snapshot.getKey(),name, time));

                }

                Collections.reverse(bookList);
                booksAdapter = new BooksAdapter(MainActivity.this, bookList);
                mainRecyclerview.setAdapter(booksAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        bookDialog = new Dialog(this);
        mainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bookDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                bookDialog.setContentView(R.layout.layout_new_book);
                bookDialog.setCancelable(false);
                bookDialog.setCanceledOnTouchOutside(true);
                bookDialog.getWindow().getAttributes().windowAnimations = R.style.UpBottomSlideDialogAnimation;

                Window window = bookDialog.getWindow();
                window.setGravity(Gravity.TOP);
                window.setLayout(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setDimAmount(0.75f);
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                final EditText bookNameEditText = bookDialog.findViewById(R.id.dialog_new_book_name_edittext);
                Button Done, Cancel;
                Done = bookDialog.findViewById(R.id.dialog_new_book_done_btn);
                Cancel = bookDialog.findViewById(R.id.dialog_new_book_cancel_btn);

                Done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!TextUtils.isEmpty(bookNameEditText.getText().toString())) {
                            String pushid = booksRef.push().getKey();
                            Map bookMap = new HashMap();
                            bookMap.put("name", bookNameEditText.getText().toString());
                            bookMap.put("time", ServerValue.TIMESTAMP);

                            booksRef.child(pushid).setValue(bookMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        bookDialog.dismiss();
                                        Toast.makeText(MainActivity.this, "Created new book.", Toast.LENGTH_SHORT).show();

                                    } else {
                                        bookDialog.dismiss();
                                        Toast.makeText(MainActivity.this, "Failed to create new book.", Toast.LENGTH_SHORT).show();

                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(MainActivity.this, "Failed to create new book.", Toast.LENGTH_SHORT).show();
                                    bookDialog.dismiss();


                                }
                            });
                        } else {

                            Toast.makeText(MainActivity.this, "fill in book name.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        bookDialog.dismiss();

                    }
                });

                bookDialog.show();

            }
        });

    }
}
