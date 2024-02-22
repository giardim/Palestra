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
    private final RecyclerViewInterface recyclerViewInterface;

    public tsRecyclerViewAdapter (Context context, ArrayList<allTimestampsModel> timeStampModel, RecyclerViewInterface recyclerViewInterface){
        this.context = context;
        this.timestampsModels = timeStampModel;
        this.recyclerViewInterface = recyclerViewInterface;
    }
    @NonNull
    @Override
    public tsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.stat_item, parent, false);
        return new tsRecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
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


        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            timestamps = itemView.findViewById(R.id.timestamp);
            statsList = itemView.findViewById(R.id.statList);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerViewInterface != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
