package com.example.palestra;

import android.icu.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class WorkoutStatsModel {
    private List<String> workoutStats;
    private Date date;
    private HashMap<String, String> workoutMap = new HashMap<>();
    private SimpleDateFormat df;
    public WorkoutStatsModel(){
        //Do nothing
    }

    public WorkoutStatsModel(ArrayList<String> workoutStats){
        this.workoutStats = workoutStats;
        date = Calendar.getInstance().getTime();
        for (int i = 0; i < workoutStats.size(); ++i){
            workoutMap.put(i + "", workoutStats.get(i));
        }
         this.df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault());
    }

    public String getDate(){
         return df.format(date);
    }

    public HashMap<String, String> getWorkoutMap(){
        return workoutMap;
    }
}
