package com.example.palestra;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.palestra.MainActivity;

public class workoutFragment extends Fragment {
    private String currWorkout = "";
    private MainActivity mainActivity;
    private TCPClient tcpClient;
    private String serverMessage;
    private boolean isTracking = false;

    public workoutFragment() {
        // Required empty public constructor
    }

    public workoutFragment(String currWorkout, MainActivity mainActivity, TCPClient tcpClient){
        this.currWorkout = currWorkout;
        this.mainActivity = mainActivity;
        this.tcpClient = tcpClient;
    }
    public workoutFragment(String currWorkout, MainActivity mainActivity){
        this.currWorkout = currWorkout;
        this.mainActivity = mainActivity;
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
        TextView workout = (TextView) root.findViewById(R.id.title);
        workout.setText(currWorkout);


        trackStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTracking){
                    isTracking = false;
                    tcpClient.interrupt();
                    trackStats.setText("Track Workout...");
                    trackStats.setBackgroundColor(Color.parseColor("#00ff00"));
                }
                else{
                    isTracking = true;
                    tcpClient = new TCPClient();
                    tcpClient.start();
                    trackStats.setText("Tracking...");
                    trackStats.setBackgroundColor(Color.parseColor("#ff0000"));
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

    public boolean getIsTracking(){
        return isTracking;
    }

}