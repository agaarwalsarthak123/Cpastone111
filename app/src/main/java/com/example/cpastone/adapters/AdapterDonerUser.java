package com.example.cpastone.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cpastone.MyApplication;
import com.example.cpastone.activities.DonerDetailActivity;
import com.example.cpastone.activities.PdfDetailActivity;
import com.example.cpastone.activities.PdfEditActivity;
import com.example.cpastone.activities.ProfileActivity;
import com.example.cpastone.databinding.RowDonerUserBinding;
import com.example.cpastone.databinding.RowPdfAdminBinding;
import com.example.cpastone.filters.FilterDonerUser;
import com.example.cpastone.filters.FilterPdfAdmin;
import com.example.cpastone.models.ModelDoner;
import com.example.cpastone.models.ModelPdf;

import java.util.ArrayList;

public class AdapterDonerUser extends RecyclerView.Adapter<AdapterDonerUser.HolderDonerUser> implements Filterable {
    // context
    private Context context;
    // arrayList list to hold list of data of type ModelDoner
    public ArrayList<ModelDoner> donerArrayList, filterList;

    // view binding row_doner_user.xml
    private RowDonerUserBinding binding;

    private FilterDonerUser filter;

    public static final String TAG = "Doner_Adapter_TAG";

            // progress
    private ProgressDialog progressDialog;

    public AdapterDonerUser(Context context, ArrayList<ModelDoner> donerArrayList) {
        this.context = context;
        this.donerArrayList = donerArrayList;
        this.filterList = donerArrayList;

        // init dialog box
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

    }

    @NonNull
    @Override
    public AdapterDonerUser.HolderDonerUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowDonerUserBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderDonerUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderDonerUser holder, int position) {
        // getting data
        ModelDoner model = donerArrayList.get(position);
        String donerId = model.getId();
        String bookId = model.getBookId();
        String name = model.getName();
        String condition = model.getCondition();
        Log.d("Sarthak3", condition);
        String latitude = model.getLatitude();
        Log.d("Sarthak4", latitude);
        String longitude = model.getLongitude();
        String donerUrl = model.getUrl();
        Log.d("Sarthak5", donerUrl);


        // set data
        holder.donerTv.setText(name);
        holder.conditionTv.setText(condition);
        holder.latTv.setText(latitude);
        holder.longTv.setText(longitude);

        MyApplication.loadBook(
                ""+bookId,
                holder.bookTv
        );
        MyApplication.loadDonerFromUrlSinglePage(
                ""+donerUrl,
                ""+name
        );

        MyApplication.loadDonerSize(
                ""+donerUrl,
                 ""+name,
                holder.sizeTv
        );

        // handles click, open doner details, passing doner id to get details of it
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DonerDetailActivity.class);
                intent.putExtra("bookId", donerId);
                context.startActivity(intent);
            }
        });


    }

    @Override
    public Filter getFilter() {
        if(filter == null) {
            filter = new FilterDonerUser(filterList, this);
        }
        return filter;
    }





    @Override
    public int getItemCount() {
        return donerArrayList.size();
    }

    public class HolderDonerUser extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        TextView donerTv, conditionTv, latTv,longTv, sizeTv, dateTv, bookTv;
        public HolderDonerUser(@NonNull View itemView) {
            super(itemView);
            donerTv = binding.donerTv;
            conditionTv = binding.conditionTv;
            latTv = binding.latTv;
            longTv = binding.longTv;
            sizeTv = binding.sizeTv;
            bookTv = binding.bookTv;

        }
    }
}

