package com.unsalGasimliApplicationsNSUG.vetapp.ui.patient;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.ActivityPatientBinding;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments.AppointmentFragment;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions.PrescriptionListFragment;

public class PatientActivity extends AppCompatActivity {
    private ActivityPatientBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView nav = binding.bottomNavigationView;
        nav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;
            switch (item.getItemId()) {
                case R.id.nav_appointments:
                    selectedFragment = new AppointmentFragment();
                    break;
                case R.id.nav_prescription:
                    selectedFragment = new PrescriptionListFragment();
                    break;
                default:
                    return false;
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_appointments);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
