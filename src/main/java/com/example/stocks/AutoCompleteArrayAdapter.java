package com.example.stocks;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteArrayAdapter extends ArrayAdapter<String> implements Filterable {
    private List<String> suggestedTickers;

    public AutoCompleteArrayAdapter(@NonNull Context context, int resource){
        super(context,resource);
        suggestedTickers = new ArrayList<>();
    }

    public void setData(List<String> list){
        suggestedTickers.clear();
        suggestedTickers.addAll(list);
    }

    public int getCount(){
        return suggestedTickers.size();
    }
    public String getItem(int position){
        return suggestedTickers.get(position);
    }

    public String getObject(int position){
        return suggestedTickers.get(position);
    }

    public Filter getFilter(){
        Filter dataFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint){
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    filterResults.values = suggestedTickers;
                    filterResults.count = suggestedTickers.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                if (filterResults != null && (filterResults.count > 0)) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return dataFilter;
            }
        }

