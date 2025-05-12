package com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.AppointmentRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions.RequestPrescriptionFragment;
import java.util.List;

public class AppointmentListFragment extends Fragment {
    private RecyclerView           recycler;
    private FloatingActionButton fabAppt, fabPresc;
    private AppointmentAdapter     adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup c, Bundle s) {
        return inf.inflate(R.layout.fragment_appointments, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v,s);
        recycler   = v.findViewById(R.id.recyclerAppointments);
        fabAppt    = v.findViewById(R.id.fabAddAppointment);
        fabPresc   = v.findViewById(R.id.fabAddPrescription);

        adapter = new AppointmentAdapter(a -> {

        });
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        fabAppt.setOnClickListener(__ ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,
                                new RequestAppointmentFragment())
                        .addToBackStack(null)
                        .commit()
        );
        fabPresc.setOnClickListener(__ ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,
                                new RequestPrescriptionFragment())
                        .addToBackStack(null)
                        .commit()
        );

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_LONG).show();
            return;
        }
        new AppointmentRepository().fetchAppointments(uid, new AppointmentRepository.Callback<List<Appointment>>() {
            @Override public void onSuccess(List<Appointment> data) {
                adapter.setItems(data);
            }
            @Override public void onError(Throwable t) {
                Toast.makeText(requireContext(),
                        "Load error: "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
