package com.example.cpastone;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cpastone.adapters.AdapterPdfUser;
import com.example.cpastone.databinding.FragmentBooksUserBinding;
import com.example.cpastone.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BooksUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BooksUserFragment extends Fragment {
    private String categoryId;
    private String category;
    private String uid;

    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfUser adapterPdfUser;

    // view binding
    private FragmentBooksUserBinding binding;

    public static final String TAG = "BOOKS_USER_TAG";

    public BooksUserFragment() {
        // Required empty public constructor
    }

    public static BooksUserFragment newInstance(String categoryId, String category, String uid) {
        BooksUserFragment fragment = new BooksUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(getContext()),container, false);

        Log.d(TAG, "onCreateView: Category: "+category);
        if(category.equals("All")) {
            // load all books
            loadAllBooks();
        }
        else if(category.equals("Most Viewed")) {
            // load most viewed
            loadMostViewedDownloadedBooks("viewCount");
        }
        else if (category.equals("Most Downloaded")) {
            // load most downloaded
            loadMostViewedDownloadedBooks("downloadsCount");

        }
        else {
            // load selected category books
            loadCategorizedBooks();
        }

        // search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterPdfUser.getFilter().filter(s);

                }
                catch (Exception e){
                    Log.d(TAG, "onTextChanged: "+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return binding.getRoot();
    }



    private void loadAllBooks() {
        // inti list
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    // getting data
                    ModelPdf model = ds.getValue(ModelPdf.class);
                    // adding to the list
                    pdfArrayList.add(model);
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    long totalViewCount = 0;
                    long totalDownloadCount = 0;
                    long totalDaysCount = 0;
                    long currentTimeStamp = System.currentTimeMillis();

                    for (ModelPdf item : pdfArrayList){
                        totalViewCount += item.getViewsCount();
                        totalDownloadCount += item.getDownloadCount();

                        long dateDiffFromToday = ((currentTimeStamp -  Long.parseLong(item.getTimestamp()))/(86400000));
                        totalDaysCount += dateDiffFromToday;

                    }

                    long avgViewCount = totalViewCount/pdfArrayList.size();
                    long avgDownloadCount = totalDownloadCount/pdfArrayList.size();
                    long avgDayCount = totalDaysCount/pdfArrayList.size();

                    int m = 10;
                    long r = ((2*avgViewCount) + (3*avgDownloadCount))/2;

                    pdfArrayList.sort((oldItem, newItem) -> {

                        long vNew = newItem.getDownloadCount();
                        long vOld = oldItem.getDownloadCount();

                        if (vNew == 0) vNew = 1;
                        if (vOld == 0) vOld = 1;

                        String resultNew = String.valueOf((((vNew/(vNew + m)) * r) + ((m/vNew) * avgDayCount)));
                        String resultOld =  String.valueOf((((vOld/(vOld + m)) * r) + ((m/vOld) * avgDayCount)));

                        return resultOld.compareTo(resultNew);
                    });

                }

                // setting up adapter
                adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                // setting adapter to recyclerview
                binding.booksRv.setAdapter(adapterPdfUser);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMostViewedDownloadedBooks(String orderBy) {
        // inti list
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy).limitToLast(10)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            // getting data
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            //    Log.d("Sarthak", model.getTimestamp());
                            // adding to the list
                            pdfArrayList.add(model);
                        }
                        // setting up adapter
                        adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                        // setting adapter to recyclerview
                        binding.booksRv.setAdapter(adapterPdfUser);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadCategorizedBooks() {
        // inti list
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            // getting data
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            // adding to the list
                            pdfArrayList.add(model);
                        }
                        // setting up adapter
                        adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                        // setting adapter to recyclerview
                        binding.booksRv.setAdapter(adapterPdfUser);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}