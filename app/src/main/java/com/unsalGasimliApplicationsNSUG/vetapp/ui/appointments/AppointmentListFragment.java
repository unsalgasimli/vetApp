package com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.AppointmentRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentAppointmentsBinding;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions.RequestPrescriptionFragment;

import java.util.List;

public class AppointmentListFragment extends Fragment {
    private FragmentAppointmentsBinding binding;
    private AppointmentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAppointmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new AppointmentAdapter(a -> {
            // handle appointment click if needed
        });
        binding.recyclerAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAppointments.setAdapter(adapter);

        binding.fabAddAppointment.setOnClickListener(__ ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new RequestAppointmentFragment())
                        .addToBackStack(null)
                        .commit()
        );
//        binding.fabAddPrescription.setOnClickListener(__ ->
//                requireActivity().getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.fragment_container, new RequestPrescriptionFragment())
//                        .addToBackStack(null)
//                        .commit()
//        );

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(requireContext(), R.string.not_logged_in, Toast.LENGTH_LONG).show();
            return;
        }

        new AppointmentRepository().fetchAppointments(uid, new AppointmentRepository.Callback<List<Appointment>>() {
            @Override
            public void onSuccess(List<Appointment> data) {
                adapter.setItems(data);
            }

            @Override
            public void onError(Throwable t) {
                Toast.makeText(requireContext(),
                        getString(R.string.load_error, t.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}