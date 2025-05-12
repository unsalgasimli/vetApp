// File: app/src/main/java/com/unsalGasimliApplicationsNSUG/vetapp/ui/appointments/RequestAppointmentFragment.java
package com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.User;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentRequestAppointmentBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RequestAppointmentFragment extends Fragment {
    private static final String ARG_PATIENT_ID = "ARG_PATIENT_ID";

    private FragmentRequestAppointmentBinding binding;
    private AppointmentViewModel                vm;
    private String                              patientId;
    private String                              currentPatientName = "";
    private final List<User>                    doctors = new ArrayList<>();

    public RequestAppointmentFragment() {
        super(R.layout.fragment_request_appointment);
    }

    public static RequestAppointmentFragment newInstance(String patientId) {
        RequestAppointmentFragment f = new RequestAppointmentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PATIENT_ID, patientId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding = FragmentRequestAppointmentBinding.bind(view);
        vm      = new ViewModelProvider(this).get(AppointmentViewModel.class);


        if (getArguments() != null && getArguments().containsKey(ARG_PATIENT_ID)) {
            patientId = requireArguments().getString(ARG_PATIENT_ID);
        } else {
            patientId = FirebaseAuth.getInstance().getUid();
        }

        if (patientId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(patientId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            User u = doc.toObject(User.class);
                            if (u != null && u.getDisplayName() != null) {
                                currentPatientName = u.getDisplayName();
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(),
                                    getString(R.string.load_doctors_failed, e.getMessage()),
                                    Toast.LENGTH_LONG
                            ).show()
                    );
        }


        vm.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                String[] parts = msg.split(":", 2);
                Toast.makeText(
                        requireContext(),
                        getStringResource(parts[0], parts.length>1?parts[1]:""),
                        Toast.LENGTH_SHORT
                ).show();
                vm.clearMessage();
            }
        });


        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.appointment_types,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTypes.setAdapter(typeAdapter);


        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("role","staff")
                .get()
                .addOnSuccessListener(qs -> {
                    doctors.clear();
                    List<String> names = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        User u = doc.toObject(User.class);
                        u.setId(doc.getId());
                        doctors.add(u);
                        names.add(u.getDisplayName());
                    }
                    ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            names
                    );
                    doctorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spinnerDoctors.setAdapter(doctorAdapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                getString(R.string.load_doctors_failed, e.getMessage()),
                                Toast.LENGTH_LONG
                        ).show()
                );


        binding.etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (dp,y,m,d)-> binding.etDate.setText(String.format("%04d-%02d-%02d", y, m+1, d)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
        binding.etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(requireContext(),
                    (tp,h,mm)-> binding.etTime.setText(String.format("%02d:%02d", h, mm)),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
            ).show();
        });


        binding.btnSubmit.setOnClickListener(v -> {
            String date = binding.etDate.getText().toString().trim();
            String time = binding.etTime.getText().toString().trim();
            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(requireContext(), R.string.fill_date_time, Toast.LENGTH_SHORT).show();
                return;
            }
            int docPos = binding.spinnerDoctors.getSelectedItemPosition();
            if (docPos < 0 || docPos >= doctors.size()) {
                Toast.makeText(requireContext(), R.string.select_doctor, Toast.LENGTH_SHORT).show();
                return;
            }


            Appointment a = new Appointment();
            a.setPatientId(patientId);
            a.setPatientName(currentPatientName);
            a.setType(binding.spinnerTypes.getSelectedItem().toString());

            User chosenDoctor = doctors.get(docPos);
            a.setDoctorId(chosenDoctor.getId());
            a.setDoctorName(chosenDoctor.getDisplayName());


            Calendar cal = Calendar.getInstance();
            String[] ds = date.split("-"), ts = time.split(":");
            cal.set(
                    Integer.parseInt(ds[0]),
                    Integer.parseInt(ds[1]) - 1,
                    Integer.parseInt(ds[2]),
                    Integer.parseInt(ts[0]),
                    Integer.parseInt(ts[1])
            );
            a.setDateTime(new Timestamp(cal.getTime()));
            a.setStatus("Requested");


            vm.createAppointment(a);
        });
    }

    private String getStringResource(String key, String arg) {
        int id = getResources().getIdentifier(key, "string", requireContext().getPackageName());
        return id == 0 ? key : getString(id, arg);
    }
}
