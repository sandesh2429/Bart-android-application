package com.myproject.joy.bartapplication;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Vamshik B D on 3/15/2018.
 */

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
    private String[] destinationArrivalList;
    private String[] sourceArrivalList;
    private String[] sourceArrivalAtLegList;
    private String[] departureAtLegList;
    private String[] legStationNameList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mSourceTextView;
        public TextView mDestinationAtLegTextView;
        public TextView mSourceTextAtLegView;
        public TextView mDestinationTextView;

        public ViewHolder(View v) {
            super(v);
            mSourceTextView = (TextView)v.findViewById(R.id.sourceTimeLeg1);
            mDestinationAtLegTextView = (TextView)v.findViewById(R.id.destinationTimeLeg1);
            mSourceTextAtLegView = (TextView) v.findViewById(R.id.sourceTimeLeg2);
            mDestinationTextView = (TextView) v.findViewById(R.id.destinationTimeLeg2);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ScheduleAdapter(String[] sourceArrivalList, String[] destinationArrivalList, String[] sourceArrivalAtLegList, String[] departureAtLegList, String[] legStationNameList) {
        this.sourceArrivalList = sourceArrivalList;
        this.destinationArrivalList = destinationArrivalList;
        this.sourceArrivalAtLegList = sourceArrivalAtLegList;
        this.departureAtLegList = departureAtLegList;
        this.legStationNameList = legStationNameList;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ScheduleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_layout_two_legs, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mSourceTextView.setText(sourceArrivalList[position]);
        holder.mDestinationAtLegTextView.setText(legStationNameList[position]+": " + sourceArrivalAtLegList[position]);
        holder.mSourceTextAtLegView.setText(legStationNameList[position]+": " +departureAtLegList[position]);
        holder.mDestinationTextView.setText(destinationArrivalList[position]);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sourceArrivalList.length;
    }
}
