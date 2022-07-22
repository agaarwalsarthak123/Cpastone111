package com.example.cpastone.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cpastone.R;
import com.example.cpastone.databinding.ActivityDonerAddBinding;
import com.example.cpastone.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class DonerAddActivity extends AppCompatActivity {


    String item;
    // drop down menu
    String[] condition = {"New","Used", "Old"};
    AutoCompleteTextView conditionEt;
    ArrayAdapter<String> adapterItems;


    // setup view binding
    private ActivityDonerAddBinding binding;
    // firebase auth
    private FirebaseAuth firebaseAuth;

    // progress dialog
    private ProgressDialog progressDialog;

    // arrayList to hold user details for different books
    private ArrayList<String> bookTitileArrayList, bookIdArrayList;

    private Uri donerUri = null;

    private static final int PDF_PICK_CODE = 1000;

    private static final String TAG = "ADD_DONER_TAG";   // debugging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDonerAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // inti firebase
        firebaseAuth = FirebaseAuth.getInstance();
        loadDonerCategories();

        // setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // handles click go to previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // handles click to pick book
        binding.bookTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookPickDialog();
            }
        });

        // handles click to attach pdf
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });

        // handles click to submit the post
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        conditionEt = findViewById(R.id.conditionEt);
        adapterItems = new ArrayAdapter<String>(this, R.layout.list_item, condition);
        conditionEt.setAdapter(adapterItems);

        conditionEt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 item = parent.getItemAtPosition(position).toString();
            }
        });
    }



    private String name="", year="", email="", latitude="",longitude="" ;
         //   condition="",

    private void validateData() {
        // 1. validating data
        Log.d(TAG, "validateData: validating data....");
        // getting data
        name = binding.donerEt.getText().toString().trim();
        year = binding.publicationEt.getText().toString().trim();
     //   conditionEt = binding.conditionEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        latitude = binding.latEt.getText().toString().trim();
        longitude = binding.latEt.getText().toString().trim();

        // validating data
        if(TextUtils.isEmpty(name)) {
            Toast.makeText(this,"Enter Your Name....", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(year)) {
            Toast.makeText(this,"Enter Publication Year....", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(email)) {
            Toast.makeText(this,"Enter Your email address....", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(latitude)) {
            Toast.makeText(this,"Enter Your Latitude....", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(longitude)) {
            Toast.makeText(this,"Enter Your Longitude....", Toast.LENGTH_SHORT).show();
        }
        else if(donerUri == null) {
            Toast.makeText(this, "Please post picture of the book", Toast.LENGTH_SHORT).show();
        }
        else{
            uploadDonerToStorage();
        }
    }

    private void uploadDonerToStorage() {
        // Step 2: Uploading Doner details to firebase
        Log.d(TAG, "uploadDonerToStorage: Uploading it to storage");

        // show progress
        progressDialog.setMessage("Uploading picture...");
        progressDialog.show();
        // timestamps
        long timestamp = System.currentTimeMillis();

        // path to doner details in firebase storage
        String filePathName = "Details/"+timestamp;
        // storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathName);
        storageReference.putFile(donerUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Post published");
                        Log.d(TAG, "onSuccess: getting picture url");

                        // getting pdf url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPictureUrl = ""+uriTask.getResult();

                        // uploading to firebase db
                        uploadedPictureUrl(uploadedPictureUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: PAD upload failed due to "+e.getMessage());
                        Toast.makeText(DonerAddActivity.this, "Picture upload failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadedPictureUrl(String uploadedPictureUrl, long timestamp) {
        // Step 3: Uploading doner Details to firebase db
        Log.d(TAG, "uploadedPictureUrl: Uploading Doner Details to firebase db....");

        progressDialog.setTitle("Uploading Doner details....");

        String uid = firebaseAuth.getUid();
        // setting up data to upload
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("name", ""+name);
        hashMap.put("year", ""+year);
        hashMap.put("condition", ""+item);
        hashMap.put("email", ""+email);
        hashMap.put("latitude", ""+latitude);
        hashMap.put("longitude", ""+longitude);
        hashMap.put("bookId", ""+selectedBookId);
        hashMap.put("url", ""+uploadedPictureUrl);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("viewsCount", 0);

        // db reference; DB -> Details
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Details");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Successfully uploaded the picture");
                        startActivity(new Intent(DonerAddActivity.this, DonateActivity.class));
                        Toast.makeText(DonerAddActivity.this, "Posted Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload db due to "+e.getMessage());
                        Toast.makeText(DonerAddActivity.this, "Failed t upload db due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // getting data


    private void loadDonerCategories() {
        Log.d(TAG, "loadDonerCategories: Loading Doner lists");
        bookTitileArrayList = new ArrayList<>();
        bookIdArrayList = new ArrayList<>();

        // db reference to load doner  db->Doner
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Doner");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookTitileArrayList.clear();
                bookIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {

                    // getting id and book
                    String bookId = ""+ds.child("id").getValue();
                    String bookTitle = ""+ds.child("book").getValue();

                    // adding to arrayLists
                    bookTitileArrayList.add(bookTitle);
                    bookIdArrayList.add(bookId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

private String selectedBookId, selectedBookTitle;
    private void bookPickDialog() {
        // getting books from firebase
        Log.d(TAG, "bookPickDialog: showing books pick dialog ");

        // getting String array of books from arrayList
        String[] bookArray = new String[bookTitileArrayList.size()];

        for (int i = 0; i< bookTitileArrayList.size();i++) {
            bookArray[i] = bookTitileArrayList.get(i);
        }

        // alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Book")
                .setItems(bookArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // handles item click
                        // get clicked item from list
                        selectedBookTitle = bookTitileArrayList.get(which);
                        selectedBookId = bookIdArrayList.get(which);
                        // setting to book text view
                        binding.bookTv.setText(selectedBookTitle);

                        Log.d(TAG, "onClick: Selected book: "+selectedBookId+" "+selectedBookTitle);
                    }
                })
                .show();
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: Starting doner details pick intent");
        Intent intent = new Intent();
        intent.setType("image/jpeg");
        intent.setType("image/png");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Deleted Doner Details"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: Book Picture Picked");
        donerUri = data.getData();
        Log.d(TAG, "onActivityResult: URI" + donerUri);

    }

}
