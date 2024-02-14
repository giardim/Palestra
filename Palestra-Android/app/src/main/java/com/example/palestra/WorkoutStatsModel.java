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
    private HashMap<Integer, String> workoutMap;
    public WorkoutStatsModel(){
        //Do nothing
    }

    public WorkoutStatsModel(ArrayList<String> workoutStats){
        this.workoutStats = workoutStats;
        date = Calendar.getInstance().getTime();
        for (int i = 0; i < workoutStats.size(); ++i){
            workoutMap.put(i, workoutStats.get(i));
        }
        SimpleDateFormat df = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss", Locale.getDefault());
    }

    public Date getDate(){
        return date;
    }

    public HashMap<Integer, String> getWorkoutMap(){
        return workoutMap;
    }
}
