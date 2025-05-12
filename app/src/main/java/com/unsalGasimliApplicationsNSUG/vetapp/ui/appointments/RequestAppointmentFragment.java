package com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.User;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.AppointmentRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentRequestAppointmentBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RequestAppointmentFragment extends Fragment {

    private static final String ARG_PATIENT_ID = "ARG_PATIENT_ID";

    private FragmentRequestAppointmentBinding binding;
    private String patientId;
    private final List<User> doctors = new ArrayList<>();
    private final AppointmentRepository repo = new AppointmentRepository();


    public static RequestAppointmentFragment newInstance(@NonNull String patientId) {
        RequestAppointmentFragment frag = new RequestAppointmentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PATIENT_ID, patientId);
        frag.setArguments(args);
        return frag;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            patientId = getArguments().getString(ARG_PATIENT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentRequestAppointmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        setupTypeSpinner();
        loadDoctorsFromFirestore();
        setupDatePicker();
        setupTimePicker();
        binding.btnSubmit.setOnClickListener(view -> trySubmit());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void setupTypeSpinner() {
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.appointment_types,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTypes.setAdapter(typeAdapter);
    }

    private void setupDatePicker() {
        binding.etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(
                    requireContext(),
                    (dp, y, m, d) -> binding.etDate.setText(String.format("%04d-%02d-%02d", y, m + 1, d)),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void setupTimePicker() {
        binding.etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(
                    requireContext(),
                    (tp, h, mm) -> binding.etTime.setText(String.format("%02d:%02d", h, mm)),
                    c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE),
                    true
            ).show();
        });
    }


    private void loadDoctorsFromFirestore() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("role", "staff")
                .get()
                .addOnSuccessListener(snap -> {
                    doctors.clear();
                    List<String> names = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        User d = doc.toObject(User.class);
                        doctors.add(d);
                        names.add(d.getDisplayName());
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
                                "Failed loading doctors: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }


    private void trySubmit() {


        if (doctors.isEmpty()) {
            Toast.makeText(requireContext(), "Doctors still loading…", Toast.LENGTH_SHORT).show();
            return;
        }

        /* 1) Form doğrulama */
        String dateStr = binding.etDate.getText().toString().trim();
        String timeStr = binding.etTime.getText().toString().trim();
        if (dateStr.isEmpty() || timeStr.isEmpty()) {
            Toast.makeText(requireContext(), "Pick both date & time", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_LONG).show();
            return;
        }

        /* 2) Hasta ad-soyadını çek */
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String patientName = combineName(
                            userDoc.getString("firstName"),
                            userDoc.getString("lastName")
                    );
                    createAndSaveAppointment(uid, patientName, dateStr, timeStr);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Failed fetching patient info: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }


    private void createAndSaveAppointment(String patientUid,
                                          String patientName,
                                          String dateStr,
                                          String timeStr) {


        int pos = binding.spinnerDoctors.getSelectedItemPosition();
        pos = Math.max(0, Math.min(pos, doctors.size() - 1));
        User selectedDoctor = doctors.get(pos);


        Timestamp ts = null;
        try {
            String[] d = dateStr.split("-");
            String[] t = timeStr.split(":");
            Calendar cal = Calendar.getInstance();
            cal.set(
                    Integer.parseInt(d[0]),
                    Integer.parseInt(d[1]) - 1,
                    Integer.parseInt(d[2]),
                    Integer.parseInt(t[0]),
                    Integer.parseInt(t[1])
            );
            ts = new Timestamp(cal.getTime());
        } catch (Exception ignored) { }


        Appointment a = new Appointment();
        a.setType(binding.spinnerTypes.getSelectedItem().toString());
        a.setDoctorId(selectedDoctor.getId());
        a.setDoctorName(selectedDoctor.getDisplayName());
        a.setPatientId(patientUid);
        a.setPatientName(patientName);
        a.setDateTime(ts);
        a.setStatus("Requested");


        repo.create(a, new AppointmentRepository.Callback<Void>() {
            @Override public void onSuccess(Void unused) {
                Toast.makeText(requireContext(),
                        "Appointment requested!", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
            @Override public void onError(Throwable t) {
                Toast.makeText(requireContext(),
                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @NonNull
    private static String combineName(@Nullable String first, @Nullable String last) {
        StringBuilder sb = new StringBuilder();
        if (first != null) sb.append(first);
        if (last  != null) sb.append(" ").append(last);
        return sb.toString().trim();
    }
}
