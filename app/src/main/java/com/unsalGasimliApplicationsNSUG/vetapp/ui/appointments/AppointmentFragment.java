package com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentAppointmentsBinding;

/**
 * Shows the list of appointments for one patient.
 */
public class AppointmentFragment extends Fragment {
    private static final String ARG_PATIENT_ID = "ARG_PATIENT_ID";

    public static AppointmentFragment newInstance(String patientId) {
        Bundle args = new Bundle();
        args.putString(ARG_PATIENT_ID, patientId);
        AppointmentFragment f = new AppointmentFragment();
        f.setArguments(args);
        return f;
    }

    private FragmentAppointmentsBinding binding;
    private AppointmentViewModel        vm;
    private AppointmentAdapter          adapter;
    private String                      patientId;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAppointmentsBinding.inflate(inf, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_PATIENT_ID)) {
            patientId = getArguments().getString(ARG_PATIENT_ID);
        } else {
            patientId = FirebaseAuth.getInstance().getUid();
        }

        vm = new ViewModelProvider(this).get(AppointmentViewModel.class);
        adapter = new AppointmentAdapter(a -> {

        });
        binding.recyclerAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAppointments.setAdapter(adapter);


        vm.getAppointments().observe(getViewLifecycleOwner(), list -> adapter.setItems(list));
        vm.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                vm.clearMessage();
            }
        });

        binding.fabAddAppointment.setOnClickListener(v -> {

            RequestAppointmentFragment reqFrag = RequestAppointmentFragment.newInstance(patientId);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, reqFrag)
                    .addToBackStack(null)
                    .commit();
        });


        if (patientId != null) {
            vm.loadForPatient(patientId);
        } else {
            Toast.makeText(requireContext(), R.string.not_logged_in, Toast.LENGTH_LONG).show();
        }
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
