package com.unsalGasimliApplicationsNSUG.vetapp.ui.staff;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.ActivityStaffBinding;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments.AppointmentFragment;

public class StaffActivity extends AppCompatActivity {
    private ActivityStaffBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStaffBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnItemSelectedListener(this::onNavItemSelected);

        if (savedInstanceState == null) {
            loadFragment(new PatientSelectionFragment());
            binding.bottomNavigationView.setSelectedItemId(R.id.nav_appointments);
        }
    }
    
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


    private void loadFragment(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.staff_fragment_container, f)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
