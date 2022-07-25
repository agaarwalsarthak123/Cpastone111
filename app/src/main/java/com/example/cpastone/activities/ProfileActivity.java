package com.example.cpastone.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.cpastone.MyApplication;
import com.example.cpastone.R;
import com.example.cpastone.adapters.AdapterPdfFavourite;
import com.example.cpastone.databinding.ActivityProfileBinding;
import com.example.cpastone.models.ModelPdf;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {


    // view binding
    private ActivityProfileBinding binding;

// firebase for loading user data using user uid
    private FirebaseAuth firebaseAuth;
    public static final String TAG = "PROFILE_TAG";

    // array list to hold the books
    private ArrayList<ModelPdf>pdfArrayList;

    //    adapter to set in recycler view
    private AdapterPdfFavourite adapterPdfFavourite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // setup firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();
        loadFavouriteBooks();

        // handles click start profile editing
        binding.profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
            }
        });

        // handles click go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



    }

    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Loading user info of user "+firebaseAuth.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // getting all info of user from snapshot
                        String email = ""+snapshot.child("email").getValue();

                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();

                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String userType = ""+snapshot.child("userType").getValue();
                        userType = userType.toUpperCase();

                        String formattedDate = MyApplication.formatTimestamp(timestamp);

                        // setting data to ui
                        binding.emailTv.setText(email);
                        binding.nameTv.setText(name);
                        binding.memberDateTv.setText(formattedDate);
                        binding.accountTypeTv.setText(userType);

                        // setting image by using glide
                        Glide.with(getApplicationContext())    // before it was ProfileActivity.this replaced it with getApplicationContext()
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_gray)
                                .into(binding.profileIv);
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void loadFavouriteBooks() {
        // init list
        pdfArrayList = new ArrayList<>();
        Log.d("Favourite1", "Favourite1");
//         load favourite books from databse
//        User -> user id -> Favourites
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
        ref.child(firebaseAuth.getUid()).child("Favourites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // clearing list starting add data
                        Log.d("Favourite2", "Favourite2");
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            String bookId = ""+ds.child("bookId").getValue();
                            Log.d("Favourite3", "Favourite3");
                            // setting id to model
                            ModelPdf modelPdf = new ModelPdf();
                            modelPdf.setId(bookId);

                            // add model to list
                            pdfArrayList.add(modelPdf);
                            Log.d("Favourite4", "Favourite4");
                        }

                        // setting number of favourite books
                        binding.favouriteBookCountTv.setText(""+pdfArrayList.size());
                        Log.d("Favourite5", "Favourite5");
                        // setup adapter
                        adapterPdfFavourite = new AdapterPdfFavourite(ProfileActivity.this, pdfArrayList);
                        Log.d("Favourite6", "Favourite6");
                        // setting adapter to recycler view
                        binding.booksRv.setAdapter(adapterPdfFavourite);
                        Log.d("Favourite7", "Favourite7");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}