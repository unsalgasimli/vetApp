package com.unsalGasimliApplicationsNSUG.vetapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PatientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

//        BottomNavigationView bottomNavigationView = findViewById(R.id.patient_bottom_navigation);

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.patient_fragment_container, new ReservationsFragment())
                    .commit();
        }

//        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
//            Fragment selectedFragment = null;
//            int itemId = item.getItemId();
//
//            if (itemId == R.id.nav_reservations) {
//                selectedFragment = new ReservationsFragment();
//            } else if (itemId == R.id.nav_pets) {
//                selectedFragment = new PetsFragment();
//            } else if (itemId == R.id.nav_patient_data) {
//                selectedFragment = new PatientDataFragment();
//            }
//
//            if (selectedFragment != null) {
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.patient_fragment_container, selectedFragment)
//                        .commit();
//            }
//
//            return true;
//        });

    }
}
