package com.example.cpastone.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.cpastone.R;
import com.example.cpastone.adapters.AdapterDonerUser;
import com.example.cpastone.databinding.ActivityDonerAddBinding;
import com.example.cpastone.databinding.ActivityDonerListUserBinding;
import com.example.cpastone.models.ModelDoner;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class DonerListUserActivity extends AppCompatActivity {

    // view binding
    private ActivityDonerListUserBinding binding;

    private ArrayList<ModelDoner> donerArrayList;

    // adapter
    private AdapterDonerUser adapterDonerUser;
    private String bookId, bookTitle;
    public static final String TAG = "DONER_LIST_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDonerListUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        bookTitle = intent.getStringExtra("bookTitle");

        binding.subTitleTv.setText(bookTitle);

        loadPdfList();

        // search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterDonerUser.getFilter().filter(s);
                }
                catch (Exception e) {
                    Log.d(TAG, "onTextChanged: "+e.getMessage());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // handles click, go to previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }


    private void loadPdfList() {
        // init list before adding data
        donerArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Details");
        ref.orderByChild("bookId").equalTo(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        donerArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ModelDoner model = ds.getValue(ModelDoner.class);
                            donerArrayList.add(model);


                            Log.d(TAG, "onDataChange: "+model.getId()+" "+model.getName());
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                            donerArrayList.sort((modelDoner, t1) -> {
                                String newCondition = modelDoner.getCondition();
                                if (modelDoner.getCondition().toLowerCase(Locale.ROOT).equals("used")) newCondition = "Nez";

                                String oldCondition = t1.getCondition();
                                if (t1.getCondition().toLowerCase(Locale.ROOT).equals("used")) oldCondition = "Nez";

                                StringBuilder builder = new StringBuilder();
                                String newData = builder.append(oldCondition).append("_").append(modelDoner.getViewsCount()).toString();
                                Log.d("NewDataCheck", newData);
                                builder.setLength(0);
                                String oldData = builder.append(newCondition).append("_").append(t1.getViewsCount()).toString();
                                Log.d("OldDataCheck", oldData);
                                return oldData.compareTo(newData);
                            });

                        }


                        for (ModelDoner item : donerArrayList){
                            Log.d("sort_firebase","" + new Gson().toJson(item));
                        }
                        // setup adapter
                        adapterDonerUser = new AdapterDonerUser(DonerListUserActivity.this, donerArrayList);
                        binding.bookRv.setAdapter(adapterDonerUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}