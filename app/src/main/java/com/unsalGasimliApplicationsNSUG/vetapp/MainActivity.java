package com.unsalGasimliApplicationsNSUG.vetapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // default to patients tab
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ManagePatientFragment())
                .commit();

        BottomNavigationView nav = findViewById(R.id.nav_manage);
        nav.setOnItemSelectedListener(item -> {
            Fragment frag;
            if (item.getItemId() == R.id.nav_staff) {
                frag = new ManageStaffFragment();
            } else {
                frag = new ManagePatientFragment();
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, frag)
                    .commit();
            return true;
        });
    }
}

