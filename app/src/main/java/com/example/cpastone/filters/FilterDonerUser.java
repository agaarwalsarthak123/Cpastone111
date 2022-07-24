package com.example.cpastone.filters;

import android.content.Intent;
import android.util.Log;
import android.widget.Filter;

import com.example.cpastone.adapters.AdapterDonerUser;
import com.example.cpastone.adapters.AdapterPdfAdmin;
import com.example.cpastone.models.ModelDoner;
import com.example.cpastone.models.ModelPdf;

import java.util.ArrayList;
public class FilterDonerUser extends Filter {

    // arrayList in which  search is being performed
    ArrayList<ModelDoner> filterList;
    // adapter in which filter needs to implemented
    AdapterDonerUser adapterDonerUser;

    // constructor
    public FilterDonerUser(ArrayList<ModelDoner> filterList, AdapterDonerUser adapterDonerUser) {
        this.filterList = filterList;
        this.adapterDonerUser = adapterDonerUser;}


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        String part[] = {};
       FilterResults results = new FilterResults();
        Log.d("FilterCheck1", "FilterCheck1: ");
       // value should not be null and empty
        if(constraint != null && constraint.length() >0) {
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelDoner> filteredModels = new ArrayList<>();
            for(int i=0; i<filterList.size(); i++) {
                String constraint1 = ""+constraint;
                int commaIndex = constraint1.indexOf(",") + 1;
                // validating
                if(constraint1.contains(",") && !(constraint1.substring(commaIndex).equals(" ")) &&
                        !(constraint1.substring(commaIndex).equals("")) && !(constraint1.substring(commaIndex).equals(" -"))&&
                        !(constraint1.substring(commaIndex).equals("-")))
                {
                    int check1 = (constraint1.indexOf(",") + 2);
                    int check2 = (constraint1.indexOf(",") + 3);
                    int check3 = (constraint1.indexOf(",") + 4);

                        if (check1 != 2 && check2 != 3 && check3 != 4) {

                            if(constraint1.contains(",")) {
                                part = constraint1.split(",");
                            }
                            else {
                                part = constraint1.split(", ");
                            }
                            String part1Lat = part[0];
                            String part2Long = part[1];
                            //latitude
                            double checkLat = Double.parseDouble(part1Lat);
                            double check1Lat = checkLat - 0.25;
                            double check2Lat = checkLat + 0.25;
                            // longitude
                                double checkLong = Double.parseDouble(part2Long);
                                double check1Long = checkLong - 0.25;
                                double check2Long = checkLong + 0.25;

                                double latitude = Double.parseDouble(filterList.get(i).getLatitude().toUpperCase());
                                double longitude = Double.parseDouble(filterList.get(i).getLongitude().toUpperCase());

                                if (check1Lat < latitude && latitude < check2Lat && check1Long < longitude && longitude < check2Long) {
                                    // add to filtered list
                                    filteredModels.add(filterList.get(i));
                                }
                            }
                        }
                if(filterList.get(i).getCondition().toUpperCase().contains(constraint)) {
                    // add to filtered list
                    filteredModels.add(filterList.get(i));
                }
                if(filterList.get(i).getName().toUpperCase().contains(constraint)) {
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
        adapterDonerUser.donerArrayList = (ArrayList<ModelDoner>) results.values;

        // notifying changes
        adapterDonerUser.notifyDataSetChanged();
    }
}
