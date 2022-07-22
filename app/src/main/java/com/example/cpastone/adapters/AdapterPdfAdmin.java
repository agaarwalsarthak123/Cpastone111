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
import com.example.cpastone.activities.PdfDetailActivity;
import com.example.cpastone.activities.PdfEditActivity;
import com.example.cpastone.databinding.RowPdfAdminBinding;
import com.example.cpastone.filters.FilterPdfAdmin;
import com.example.cpastone.models.ModelPdf;
// import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {
    // context
    private Context context;
    // array List to hold list of data of type ModelPdf
    public ArrayList<ModelPdf> pdfArrayList, filterList;

    // view binding row_pdf_admin.xml
    private RowPdfAdminBinding binding;

    private FilterPdfAdmin filter;

    public static final String TAG = "PDF_ADAPTER_TAG";

    // progress
    private ProgressDialog progressDialog;

    // constructor
    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        // init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        // getting data
        ModelPdf model = pdfArrayList.get(position);
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();


        // set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);

        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv
        );

        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title
        );
        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTv
        );
        // handles click, shows dialog with option to delete and edit
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                moreOptionsDialog(model, holder);
            }
        });
        // handles click, open pdf details, passing pdf/book id to get details of it
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", pdfId);
                context.startActivity(intent);
            }
        });


    }

    private void moreOptionsDialog(ModelPdf model, HolderPdfAdmin holder) {
        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();
        // options to show in dialod
        String[] options = {"Edit", "Delete"};

        // elert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // handles dialog option click
                            if (which == 0) {
                                // edit closed, open PdfEditActivity to edit book info
                                Intent intent = new Intent(context, PdfEditActivity.class);
                                intent.putExtra("bookId", bookId);
                                context.startActivity(intent);

                            } else if (which == 1) {
                                // delete clicked
                                MyApplication.deleteBook(
                                        context,
                                        ""+bookId,
                                        ""+bookUrl,
                                        ""+bookTitle
                                );
                            }
                        }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null) {
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }

    //    view holder class for row_pdf_admin.xml
    class HolderPdfAdmin extends RecyclerView.ViewHolder {

        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv;
        ImageButton moreBtn;
        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            moreBtn = binding.moreBtn;

        }
    }

}

