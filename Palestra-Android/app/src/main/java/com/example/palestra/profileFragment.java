package com.example.palestra;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.example.palestra.MainActivity;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class profileFragment extends Fragment {
    private ActivityResultLauncher<Intent> imagePickLauncher;
    private Uri selectedImageUri;
    private MainActivity mainActivity;
    private TCPClient tcpClient;

    public profileFragment() {
        // Required empty public constructor
    }

    public profileFragment(MainActivity mainActivity, TCPClient tcpClient) {
        this.mainActivity = mainActivity;
        this.tcpClient = tcpClient;
    }

    String getWorkoutName(String workoutName){
        return workoutName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            this.selectedImageUri = data.getData();
                        }
                    }
                });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final String TAG = "onCreateView";
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        ImageButton benchPressButton = rootView.findViewById(R.id.benchPressIcon);
        ImageButton squatButton = rootView.findViewById(R.id.squatIcon);
        ImageButton deadliftButton = rootView.findViewById(R.id.deadliftIcon);
        ImageButton profilePicture = rootView.findViewById(R.id.profilePictureButton);
        TextView usernameText = rootView.findViewById(R.id.usernameText);
        benchPressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    mainActivity.replaceFragment(new workoutFragment("Benchpress", mainActivity, tcpClient));
                }
                catch (Exception e){
                    Log.d(TAG, "***" + mainActivity + " " + e + "***");
                }
            }
        });

        squatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    mainActivity.replaceFragment(new workoutFragment("Squat", mainActivity, tcpClient));
                }
                catch (Exception e){
                    Log.d(TAG, "***" + mainActivity + " " + e + "***");
                }
            }
        });

        deadliftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                    mainActivity.replaceFragment(new workoutFragment("Deadlift", mainActivity, tcpClient));


            }
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked profile picture");
                ImagePicker.with(profileFragment.this).cropSquare().compress(512).
                        maxResultSize(512, 512).createIntent(new Function1<Intent, Unit>() {
                            @Override
                            public Unit invoke(Intent intent) {
                                imagePickLauncher.launch(intent);
                                Log.d("***onClick***", "ImageURI: " + selectedImageUri);
                                if (selectedImageUri == null) {
                                    //do nothing
                                }
                                else{
                                    Glide.with(profileFragment.this).load(selectedImageUri).apply(RequestOptions.circleCropTransform())
                                            .into(profilePicture);
                                }
                                return null;
                            }
                        });
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

}
