package com.example.palestra;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class tsRecyclerViewAdapter extends RecyclerView.Adapter<tsRecyclerViewAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<allTimestampsModel> timestampsModels;

    public tsRecyclerViewAdapter (Context context, ArrayList<allTimestampsModel> timeStampModel){
        this.context = context;
        this.timestampsModels = timeStampModel;
    }
    @NonNull
    @Override
    public tsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.stat_item, parent, false);
        return new tsRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull tsRecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.timestamps.setText("Timestamp: " + timestampsModels.get(position).getTimestamp());
        holder.statsList.setText("Stats: " + timestampsModels.get(position).getStats());
    }

    @Override
    public int getItemCount() {
        return timestampsModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView timestamps;
        TextView statsList;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            timestamps = itemView.findViewById(R.id.timestamp);
            statsList = itemView.findViewById(R.id.statList);
        }
    }
}
