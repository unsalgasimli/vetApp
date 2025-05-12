package com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.AppointmentRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentAppointmentsBinding;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions.RequestPrescriptionFragment;

import java.util.List;

public class AppointmentFragment extends Fragment {
    private static final String ARG_PATIENT_ID = "ARG_PATIENT_ID";
    private static final String TAG = "AppointmentFragment";

    private FragmentAppointmentsBinding binding;
    private String patientId;
    private AppointmentAdapter adapter;

    public static AppointmentFragment newInstance(@NonNull String patientId) {
        AppointmentFragment frag = new AppointmentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PATIENT_ID, patientId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_PATIENT_ID)) {
            patientId = args.getString(ARG_PATIENT_ID);
        } else {
            patientId = FirebaseAuth.getInstance().getUid();
        }
    }

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

        binding.recyclerAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AppointmentAdapter(appt -> {
            // handle appointment click if needed
        });
        binding.recyclerAppointments.setAdapter(adapter);

        binding.fabAddAppointment.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container,
                                RequestAppointmentFragment.newInstance(patientId))
                        .addToBackStack(null)
                        .commit());

//        binding.fabAddPrescription.setVisibility(View.GONE);
//        String currentUid = FirebaseAuth.getInstance().getUid();
//        if (currentUid != null) {
//            FirebaseFirestore.getInstance()
//                    .collection("users")
//                    .document(currentUid)
//                    .get()
//                    .addOnSuccessListener((DocumentSnapshot doc) -> {
//                        String role = doc.getString("role");
//                        if ("staff".equals(role)) {
//                            binding.fabAddPrescription.setVisibility(View.VISIBLE);
//                        }
//                    })
//                    .addOnFailureListener(e ->
//                            Log.w(TAG, "Couldn't load user role", e)
//                    );
//        }

        if (patientId == null) {
            Toast.makeText(requireContext(), R.string.not_logged_in, Toast.LENGTH_LONG).show();
            return;
        }

        new AppointmentRepository()
                .fetchAll(patientId, new AppointmentRepository.Callback<List<Appointment>>() {
                    @Override
                    public void onSuccess(List<Appointment> data) {
                        adapter.setItems(data);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "Error loading appointments", t);
                        Toast.makeText(requireContext(),
                                getString(R.string.error_loading_appointments, t.getMessage()),
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
