package com.example.cpastone.filters;

import android.widget.Filter;

import com.example.cpastone.adapters.AdapterBook;
import com.example.cpastone.adapters.AdapterCategory;
import com.example.cpastone.models.ModelBook;
import com.example.cpastone.models.ModelCategory;

import java.util.ArrayList;

public class FilterBook extends Filter {

    // array list in which search is being performed
    ArrayList<ModelBook> filterList;
    // adapter in which filter needs to implemented
    AdapterBook adapterBook;

    // constructor
    public FilterBook(ArrayList<ModelBook> filterList, AdapterBook adapterBook) {
        this.filterList = filterList;
        this.adapterBook = adapterBook;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        // value should not be null and empty
        if(constraint != null && constraint.length() > 0) {
            // change to upper case, or lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelBook> filteredModels = new ArrayList<>();

            for(int i=0; i<filterList.size();i++) {
                // validating
                if(filterList.get(i).getBook().toUpperCase().contains(constraint)) {
                    // adding to filtered list
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
        adapterBook.bookArrayList = (ArrayList<ModelBook>) results.values;

        // notifying changes
        adapterBook.notifyDataSetChanged();

    }
}
