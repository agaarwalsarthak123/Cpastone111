package com.example.cpastone.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.cpastone.MyApplication;
import com.example.cpastone.databinding.ActivityDonerDetailsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DonerDetailActivity extends AppCompatActivity {

    // view binding
    private ActivityDonerDetailsBinding binding;

    String donerId, name, url;
    public static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDonerDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // getting data from intent
        Intent intent = getIntent();
        donerId = intent.getStringExtra("bookId");
        Log.d("Ch"+donerId+"eck", "@@@ ");

        // start download button
        binding.downloadDonerBtn.setVisibility(View.GONE);

        loadDonerDetails();

        // incrementing view count whenever this page loads up
        MyApplication.incrementDonerViewCount(donerId);
        Log.d("Sarthak1", donerId);

        // handles click go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // handles click download picture of the book
        binding.downloadDonerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_DOWNLOAD, "onClick: Checking permission");
                if(ContextCompat.checkSelfPermission(DonerDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG_DOWNLOAD, "onClick: Permission already granted, can download book");
                    MyApplication.downloadPicture(DonerDetailActivity.this, ""+donerId, ""+name, ""+url);
                }
                else {
                    Log.d(TAG_DOWNLOAD, "onClick: Permission was not granted, request cancellation");
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        binding.googleMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DonerDetailActivity.this, MapsActivity.class));
            }
        });
    }



    // requesting storage permission
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if(isGranted) {
                    Log.d(TAG_DOWNLOAD, ": Permission Granted");
                    MyApplication.downloadPicture(this, ""+donerId, ""+name, ""+url);
                }
                else {
                    Log.d(TAG_DOWNLOAD, ": Permission was denied");
                    Toast.makeText(this, "Permission was denied", Toast.LENGTH_SHORT).show();
                }
                });

                private void loadDonerDetails() {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Details");
                    ref.child(donerId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    // getting data
                                    name = ""+snapshot.child("name").getValue();
                                    String condition = ""+snapshot.child("condition").getValue();
                                    String latitude = ""+snapshot.child("latitude").getValue();
                                    String longitude = ""+snapshot.child("longitude").getValue();
                                    String viewsCount = ""+snapshot.child("viewsCount").getValue();
                                    String pYear = ""+snapshot.child("year").getValue();
                                    String eAddress = ""+snapshot.child("email").getValue();
                                    url = ""+snapshot.child("url").getValue();
                                    String timestamp = ""+snapshot.child("timestamp").getValue();

                                    // required data is loaded, show downloaded button
                                    binding.downloadDonerBtn.setVisibility(View.VISIBLE);

                                    // foratting data
                                    String date = MyApplication.formatTimestamp(timestamp);

                                    MyApplication.loadDonerFromUrlSinglePage(
                                            ""+url,
                                            ""+name
                                    );

                                    binding.titleTv.setText(name);
                                    binding.conditionTv.setText(condition);
                                    binding.latitudeTv.setText(latitude);
                                    binding.longitudeTv.setText(longitude);
                                    binding.viewsTv.setText(viewsCount);
                                    binding.publicationTv.setText(pYear);
                                    binding.emailTv.setText(eAddress);



                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }


}