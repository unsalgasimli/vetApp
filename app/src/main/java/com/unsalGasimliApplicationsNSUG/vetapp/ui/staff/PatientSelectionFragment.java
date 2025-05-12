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

import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.User;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.AppointmentRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentPatientSelectionBinding;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments.AppointmentFragment;

import java.util.ArrayList;
import java.util.List;

public class PatientSelectionFragment extends Fragment {
    private FragmentPatientSelectionBinding binding;
    private final List<User> patients = new ArrayList<>();
    private final AppointmentRepository repo = new AppointmentRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPatientSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerPatients.setLayoutManager(new LinearLayoutManager(requireContext()));

        UserAdapter adapter = new UserAdapter(patients, user -> {
            Fragment next = AppointmentFragment.newInstance(user.getId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.staff_fragment_container, next)
                    .addToBackStack(null)
                    .commit();
        });
        binding.recyclerPatients.setAdapter(adapter);

        repo.fetchPatients(new AppointmentRepository.Callback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                patients.clear();
                patients.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable t) {
                Toast.makeText(requireContext(),
                        getString(R.string.load_patients_failed, t.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
