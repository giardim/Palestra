package com.example.palestra;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class StatsFragment extends Fragment {

    final private String TAG = "STATS FRAGMENT";
    final private String[] workoutItems = {"Benchpress", "Squat", "Deadlift"};
    private ArrayList<allTimestampsModel> timestampsModels = new ArrayList<>();
    private ArrayList<String> allTimeStamps = new ArrayList<>();
    private ArrayList<String> statsList = new ArrayList<>();
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> arrayAdapter;
    private FirebaseAuth mAuth;
    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stats, container, false);
        autoCompleteTextView = root.findViewById(R.id.autoCompleteText);
        arrayAdapter = new ArrayAdapter<String>(getContext() , R.layout.list_item, workoutItems);
        autoCompleteTextView.setAdapter(arrayAdapter);

        RecyclerView recyclerView = root.findViewById(R.id.workoutList);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                getAllTimestamps(item);
                tsRecyclerViewAdapter adapter = new tsRecyclerViewAdapter(getContext(), timestampsModels);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
        });

        return root;
    }

    public void getAllTimestamps(String item){
        timestampsModels.clear();
        mAuth = FirebaseAuth.getInstance();
        String currentUser = mAuth.getCurrentUser().getDisplayName();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("WorkoutStats")
                //change this back to currentUser later
                .child("test123")
                .child(item);

        reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()){
                    allTimeStamps.clear();
                    DataSnapshot dataSnapshot = task.getResult();
                    for (DataSnapshot i : dataSnapshot.getChildren()){
                        allTimeStamps.add(i.getKey());
                        statsList.add(i.getValue().toString());
                    }
                    arrayAdapter.notifyDataSetChanged();
                }
                else{
                    Toast.makeText(getContext(), "Could not read from database", Toast.LENGTH_SHORT).show();
                }
            }
        });

        updateModel();
    }
    public void updateModel(){
        for (int i = 0; i < allTimeStamps.size(); ++i){
            timestampsModels.add(new allTimestampsModel(allTimeStamps.get(i), statsList.get(i)));
        }

    }
}