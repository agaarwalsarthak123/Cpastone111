package com.example.cpastone.adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cpastone.activities.DonateActivity;
import com.example.cpastone.activities.DonerListUserActivity;
import com.example.cpastone.databinding.RowBookBinding;
import com.example.cpastone.filters.FilterBook;
import com.example.cpastone.models.ModelBook;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterBook extends RecyclerView.Adapter<AdapterBook.HolderBook> implements Filterable {

    private DonateActivity context;
    public ArrayList<ModelBook> bookArrayList, filterList;

    private RowBookBinding binding;

    // instance of our filter class
    private FilterBook filter;

    public AdapterBook(DonateActivity context, ArrayList<ModelBook> bookArrayList) {
        this.context = context;
        this.bookArrayList = bookArrayList;
        this.filterList = bookArrayList;
    }

    @NonNull
    @Override
    public AdapterBook.HolderBook onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // binding row_book.xml
        binding = RowBookBinding.inflate(LayoutInflater.from(context),parent, false);

        return new HolderBook(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterBook.HolderBook holder, int position) {
        // get data
        ModelBook model = bookArrayList.get(position);
        String id = model.getId();
        String book = model.getBook();
        String uid = model.getUid();

        // set data
        holder.bookTv.setText(book);

        // handles click delete book
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // confirm delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage(" Are you Damm sure you want to delete this book")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // beginning to delete
                                Toast.makeText(context, "Deleting.....", Toast.LENGTH_SHORT).show();
                                deleteBook(model, holder);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        // handles click, go to DonerListAdminActivity, passing doner details linked wit their book donation
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DonerListUserActivity.class);
                intent.putExtra("bookId", id);
                intent.putExtra("bookTitle", book);
                context. startActivity(intent);
            }
        });
    }

    private void deleteBook(ModelBook model, HolderBook holder) {
        // getting id of book to delete
        String id = model.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Doner");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // deleted successfully
                        Toast.makeText(context, "Successfully Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed to delete
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterBook(filterList, this);
        }
        return filter;
    }




    @Override
    public int getItemCount() {
        return bookArrayList.size();
    }

    class HolderBook extends RecyclerView.ViewHolder {

        // ui view for row_category.xml
        TextView bookTv;
        ImageButton deleteBtn;

        public HolderBook( View itemView) {
            super(itemView);
            bookTv = binding.bookTv;
            deleteBtn = binding.deleteBtn;
        }
    }
}
