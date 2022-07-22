package com.example.cpastone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cpastone.adapters.AdapterBook;
import com.example.cpastone.databinding.ActivityDonateBinding;
import com.example.cpastone.models.ModelBook;
import com.example.cpastone.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DonateActivity extends AppCompatActivity {

    // view binding
    private ActivityDonateBinding binding;

    // firebase Auth
    private FirebaseAuth firebaseAuth;

    // arrayList to store books
    private ArrayList<ModelBook> bookArrayList;

    // adapter
    private AdapterBook adapterBook;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityDonateBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    // init firebase auth
    firebaseAuth = FirebaseAuth.getInstance();
    checkUser();
    localBooks();


    binding.searchEt.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        // called when user is typing each letter
            try {
                adapterBook.getFilter().filter(s);
            }
            catch (Exception e) {

            }
            adapterBook.getFilter().filter(s);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    });

    // handles click, logout
    binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            firebaseAuth.signOut();
            checkUser();
        }
    });

    // handles click starts books add screen
    binding.addBookBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("Sarthak", "Sarthak");
            startActivity(new Intent(DonateActivity.this, BookAddActivity.class));
        }
    });

    // handles adding user details screen
    binding.addDonerBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(DonateActivity.this, DonerAddActivity.class));
        }
    });

    // handles click, takes back
    binding.backBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    });


}

private void localBooks() {
    // inti arrayList
    bookArrayList = new ArrayList<>();
    // getting all categories from firebase to Books
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Doner");
    ref.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            // clearing Array List before adding data into it
            bookArrayList.clear();
            for (DataSnapshot ds: snapshot.getChildren()) {
                // getting data
                ModelBook model = ds.getValue(ModelBook.class);

                // adding to array List
                bookArrayList.add(model);
            }

            // setting up adapter
            adapterBook = new AdapterBook(DonateActivity.this, bookArrayList);
            // setting up adapter to recycler view
            binding.bookRv.setAdapter(adapterBook);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
}

private void checkUser() {
    // getting current user
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    if (firebaseUser == null)
    {
        // not logged in, go to main screen
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
    else {
        // logged in, get User info
        String email = firebaseUser.getEmail();
        // set in textview of toolbar
        binding.subtitleTv.setText(email);
    }
}


}
