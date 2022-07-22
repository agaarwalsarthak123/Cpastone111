package com.example.cpastone.filters;

import android.widget.Filter;

import com.example.cpastone.adapters.AdapterCategory;
import com.example.cpastone.adapters.AdapterPdfAdmin;
import com.example.cpastone.models.ModelCategory;
import com.example.cpastone.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {

    // arrayList in which  search is being performed
    ArrayList<ModelPdf> filterList;
    // adapter in which filter needs to implemented
    AdapterPdfAdmin adapterPdfAdmin;

    // constructor
    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
       FilterResults results = new FilterResults();
       // value should not be null and empty
        if(constraint != null && constraint.length() >0) {
            // change to upper case, or lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();

            for(int i=0; i<filterList.size(); i++) {
               // validating
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)) {
                    // add to filtered list
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;

        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        // applying filter changes
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>) results.values;

        // notifying changes
        adapterPdfAdmin.notifyDataSetChanged();
    }
}
