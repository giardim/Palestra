package com.example.palestra;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import com.example.palestra.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private final TCPClient tcpClient = new TCPClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityMainBinding binding;
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new profileFragment(this, tcpClient));
        tcpClient.start();


        binding.navBar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == (R.id.profileIcon)){
                replaceFragment(new profileFragment(this, tcpClient));
            }
            else if (item.getItemId() == (R.id.addFriendIcon)){
                replaceFragment(new searchFragment());
            }
            return true;
        });


    }

    public void replaceFragment(Fragment fragment){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.homeFragment, fragment);
        ft.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}