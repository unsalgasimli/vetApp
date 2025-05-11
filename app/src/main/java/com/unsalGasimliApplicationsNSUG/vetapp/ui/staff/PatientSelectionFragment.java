package com.unsalGasimliApplicationsNSUG.vetapp.ui.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.AppointmentRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.User;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments.AppointmentFragment;

import java.util.ArrayList;
import java.util.List;

public class PatientSelectionFragment extends Fragment {

    private RecyclerView recycler;
    private UserAdapter adapter;
    private final List<User> patients = new ArrayList<>();
    private final AppointmentRepository repo = new AppointmentRepository();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_selection, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recycler = view.findViewById(R.id.recyclerPatients);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new UserAdapter(patients, user -> {
            // When staff taps a patient, show that patient’s appointments
            Fragment next = AppointmentFragment.newInstance(user.getId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.staff_fragment_container, next)
                    .addToBackStack(null)
                    .commit();
        });
        recycler.setAdapter(adapter);

        // load all users with role == "patient"
        repo.fetchPatients(new AppointmentRepository.Callback<List<User>>() {
            @Override public void onSuccess(List<User> data) {
                patients.clear();
                patients.addAll(data);
                adapter.notifyDataSetChanged();
            }
            @Override public void onError(Throwable t) {
                Toast.makeText(requireContext(),
                        "Failed to load patients: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
