package com.example.palestra;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

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
        setProfilePicture(profilePicture, true);
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            Log.d(TAG, "" + data.getData().toString());
                            setImageUri(data.getData());
                            setProfilePicture(profilePicture, false);
                            saveProfilePicture();
                        }
                    }
                });
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
                ImagePicker.with(profileFragment.this).cropSquare().compress(512).
                        maxResultSize(512, 512).createIntent(new Function1<Intent, Unit>() {
                            @Override
                            public Unit invoke(Intent intent) {
                                imagePickLauncher.launch(intent);
                                Log.d("***onClick***", "ImageURI: " + selectedImageUri);
                                return null;
                            }
                        });
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    public void setImageUri(Uri selectedImageUri){
        this.selectedImageUri = selectedImageUri;
    }

    public Uri getImageUri(){
        return this.selectedImageUri;
    }

    public void saveProfilePicture(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUser = mAuth.getCurrentUser().getDisplayName();
        DatabaseReference profileReference = FirebaseDatabase.getInstance().getReference();
        profileReference.child("Users")
                .child(currentUser)
                .child("profilepiture")
                .setValue(getImageUri().toString());
    }

    public void setProfilePicture(ImageButton profilePicture, boolean launch){
        if (launch) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            String currentUser = mAuth.getCurrentUser().getDisplayName();
            DatabaseReference profileReference = FirebaseDatabase.getInstance().getReference();
            profileReference.child("Users")
                    .child(currentUser)
                    .child("profilepiture");
            profileReference.removeValue();
            profileReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.getResult() == null){
                        //do nothing
                    }
                    else{
                        setImageUri(Uri.parse(task.getResult().toString()));
                    }

                }
            });
        }
        if (getImageUri() != null){
            Glide.with(profileFragment.this).load(getImageUri()).apply(RequestOptions.circleCropTransform())
                    .into(profilePicture);
        }

    }

}
