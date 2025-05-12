package com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentAppointmentsBinding;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class AppointmentListFragment extends Fragment {
    private FragmentAppointmentsBinding binding;
    private AppointmentViewModel vm;
    private AppointmentAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAppointmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        vm = new ViewModelProvider(this).get(AppointmentViewModel.class);

        adapter = new AppointmentAdapter(a -> {
            // handle click, e.g. open detail
            Toast.makeText(requireContext(), "Clicked "+a.getId(), Toast.LENGTH_SHORT).show();
        });
        binding.recyclerAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAppointments.setAdapter(adapter);

        vm.getAppointments().observe(getViewLifecycleOwner(), this::onList);
        vm.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg!=null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                vm.clearMessage();
            }
        });

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid!=null) vm.loadForPatient(uid);
    }

    private void onList(List<Appointment> list) {
        adapter.setItems(list);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
