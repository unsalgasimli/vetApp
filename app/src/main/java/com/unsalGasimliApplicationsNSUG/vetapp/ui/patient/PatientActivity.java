package com.unsalGasimliApplicationsNSUG.vetapp.ui.patient;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments.AppointmentFragment;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments.AppointmentListFragment;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions.PrescriptionFragment;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions.PrescriptionListFragment;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.staff.AllPrescriptionsFragment;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.staff.PatientSelectionFragment;

public class PatientActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);

        nav.setOnItemSelectedListener(item -> {
            Fragment f;
            switch (item.getItemId()) {
                case R.id.nav_appointments:
                    f = new AppointmentFragment();
                    break;
                case R.id.nav_prescription:
                    f = new PrescriptionListFragment();
                    break;
                default:
                    return false;
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, f)
                    .commit();
            return true;
        });

        // show the patients list by default:
        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_appointments);
        }
    }
}
