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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import com.example.palestra.WorkoutStatsModel;

public class workoutFragment extends Fragment {
    private String currWorkout = "";
    private MainActivity mainActivity;
    private TCPClient tcpClient;
    private volatile boolean isTracking = false;
    private ArrayList<String> workoutStats = new ArrayList<String>();
    private WorkoutStatsModel workoutStatsModel = new WorkoutStatsModel();

    private TextView workout;
    private DatabaseReference workoutRef;
    private FirebaseAuth mAuth;

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
                    updateDatabase(currWorkout);
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
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        workoutGraph.addSeries(series);
        Log.d("SIZEOF WORKOUT", " " +  workoutStats.size());
        DataPoint[] values = new DataPoint[workoutStats.size()];
        for (int i = 0; i < workoutStats.size(); ++i) {
            Log.d("STAT ARRAY", " " + workoutStats.get(i));
            DataPoint point = new DataPoint(i, Double.parseDouble(workoutStats.get(i)));
            series.appendData(point, true, workoutStats.size());
        }
        workoutGraph.addSeries(series);
    }

    void updateDatabase(String currWorkout){
        workoutStatsModel = new WorkoutStatsModel(workoutStats);
        String workoutString = "WorkoutStats";
        mAuth = FirebaseAuth.getInstance();
        String currentUser = mAuth.getCurrentUser().getDisplayName();
        String currentDate = workoutStatsModel.getDate();
        Log.d("CurrentUser", currentDate + " ");
        workoutRef = FirebaseDatabase.getInstance().getReference();
        workoutRef.child(workoutString)
                .child(currentUser)
                .child(currWorkout)
                .child(currentDate)
                .setValue(workoutStatsModel.getWorkoutMap());

    }
}