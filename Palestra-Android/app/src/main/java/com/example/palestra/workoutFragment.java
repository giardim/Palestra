package com.example.palestra;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class workoutFragment extends Fragment {
    private String currWorkout = "";
    private MainActivity mainActivity;
    private TCPClient tcpClient;
    private volatile boolean isTracking = false;
    private ArrayList<String> workoutStats = new ArrayList<String>();

    private TextView workout;

    public workoutFragment() {
        // Required empty public constructor
    }

    public workoutFragment(String currWorkout, MainActivity mainActivity, TCPClient tcpClient){
        this.currWorkout = currWorkout;
        this.mainActivity = mainActivity;
        this.tcpClient = tcpClient;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final String TAG = "***onCreateView***";
        View root = inflater.inflate(R.layout.fragment_workout, container, false);
        Button returnButton = (Button) root.findViewById(R.id.returnToMain);
        Button trackStats = (Button) root.findViewById(R.id.trackWorkout);
        workout = (TextView) root.findViewById(R.id.title);
        GraphView workoutGraph = (GraphView) root.findViewById(R.id.workoutStats);
        workout.setText(currWorkout);

        //configure graph
        workoutGraph.getViewport().setScrollable(true);
        workoutGraph.getViewport().setScalable(true);
        workoutGraph.getViewport().setScalableY(true);
        workoutGraph.getViewport().setScrollableY(true);

        trackStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTracking){
                    isTracking = false;
                    tcpClient.setStatus(false);
                    trackStats.setText("Track Workout...");
                    trackStats.setBackgroundColor(Color.parseColor("#00ff00"));
                    workoutStats = tcpClient.getWorkoutStats();
                    updateGraph(workoutGraph);
                }
                else{
                    isTracking = true;
                    tcpClient.setStatus(true);
                    trackStats.setText("Tracking...");
                    trackStats.setBackgroundColor(Color.parseColor("#ff0000"));
                    workoutGraph.removeAllSeries();
                }
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    mainActivity.replaceFragment(new profileFragment(mainActivity, tcpClient));
                }
                catch (Exception e){
                    Log.d(TAG, "***" + mainActivity + " " + e + "***");
                }

            }
        });
        return root;
    }

    void updateGraph(GraphView workoutGraph){
        workoutStats = tcpClient.getWorkoutStats();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{});

        for (int i = 0; i < workoutStats.size(); ++i){
            Log.d("STAT ARRAY", workoutStats.get(i));
            series.appendData(new DataPoint(i, Double.parseDouble(workoutStats.get(i))), true, 1000);
        }
        workoutGraph.addSeries(series);
    }
}