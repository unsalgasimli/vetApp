package com.unsalGasimliApplicationsNSUG.vetapp.ui.staff;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments.AppointmentFragment;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments.AppointmentListFragment;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions.PrescriptionFragment;

public class StaffActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);

        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(this::onNavItemSelected);

        // If this is first launch, explicitly load the patients list
        if (savedInstanceState == null) {
            loadFragment(new PatientSelectionFragment());
            bottomNav.setSelectedItemId(R.id.nav_appointments);
        }
    }

    /** Handles bottom‐nav item clicks */
    private boolean onNavItemSelected(MenuItem item) {
        Fragment frag;
        switch (item.getItemId()) {
            case R.id.nav_appointments:
                frag = new AllAppointmentsFragment();
                break;
            case R.id.nav_prescription:
                frag = new AllPrescriptionsFragment();
                break;
            default:
                return false;
        }
        loadFragment(frag);
        return true;
    }

    /** Utility: swap in the given fragment */
    private void loadFragment(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.staff_fragment_container, f)
                .commit();
    }
}
