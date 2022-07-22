package com.example.cpastone.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cpastone.databinding.ActivityBookAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class BookAddActivity extends AppCompatActivity {

    // view binding
    private ActivityBookAddBinding binding;

    // firebase Auth
    private FirebaseAuth firebaseAuth;

    // progress dialog

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // inti firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // handles click, will go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // handles click begins to upload book
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }
    private String book = "";

    private void validateData() {
        // getting data
        book = binding.bookEt.getText().toString().trim();
        // validate if not empty
        if(TextUtils.isEmpty(book)) {
            Toast.makeText(this, "Please Enter book Title", Toast.LENGTH_SHORT).show();
        }
        else {
            addBookFirebase();
        }
    }

    private void addBookFirebase() {
        // showing progress
        progressDialog.setMessage("Adding Book");
        progressDialog.show();

        // getting timestamp
        long timestamp = System.currentTimeMillis();

        // setting up info to add in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("book", ""+book);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", ""+firebaseAuth.getUid());

// add to firebase db..... Database Root -> Doner -> bookId -> book info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Doner");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // book add success
                        progressDialog.dismiss();
                        Toast.makeText(BookAddActivity.this, "Book Added Successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(BookAddActivity.this, DonateActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // book adding failed
                        progressDialog.dismiss();
                        Toast.makeText(BookAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}