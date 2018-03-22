package com.myproject.joy.bartapplication;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Vamshik B D on 3/15/2018.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private String[] destinationArrivalList;
    private String[] sourceArrivalList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mSourceTextView;
        public TextView mDestinationTextView;
        public ViewHolder(View v) {
            super(v);
            mSourceTextView = (TextView)v.findViewById(R.id.sourceTime);
            mDestinationTextView = (TextView)v.findViewById(R.id.destinationTime);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(String[] sourceArrivalList, String[] destinationArrivalList) {
        this.sourceArrivalList = sourceArrivalList;
        this.destinationArrivalList = destinationArrivalList;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mSourceTextView.setText(sourceArrivalList[position]);
        holder.mDestinationTextView.setText(destinationArrivalList[position]);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sourceArrivalList.length;
    }
}
