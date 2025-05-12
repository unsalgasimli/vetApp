package com.unsalGasimliApplicationsNSUG.vetapp.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.patient.ManagePatientFragment;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.staff.ManageStaffFragment;

public class AdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);


        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();

            if (id == R.id.nav_patient) {
                selected = new ManagePatientFragment();
            } else {
                selected = new ManageStaffFragment();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.admin_fragment_container, selected)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_patient);
        }
    }
}
