package com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RequestAppointmentFragment extends Fragment {
    private static final String ARG_PATIENT_ID = "ARG_PATIENT_ID";

    private String patientId;
    private Spinner spinnerType, spinnerDoctor;
    private EditText etDate, etTime;
    private Button btnSubmit;
    private List<User> doctors = new ArrayList<>();
    private AppointmentRepository repo = new AppointmentRepository();

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
        Bundle args = getArguments();
        patientId = (args != null && args.containsKey(ARG_PATIENT_ID))
                ? args.getString(ARG_PATIENT_ID)
                : null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_request_appointment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Bind views
        spinnerType   = view.findViewById(R.id.spinnerTypes);
        spinnerDoctor = view.findViewById(R.id.spinnerDoctors);
        etDate        = view.findViewById(R.id.etDate);
        etTime        = view.findViewById(R.id.etTime);
        btnSubmit     = view.findViewById(R.id.btnSubmit);

        // 2) Populate appointment types
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.appointment_types,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // 3) Load doctors (only staff)
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
                    spinnerDoctor.setAdapter(doctorAdapter);
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(),
                        "Failed loading doctors: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());

        // 4) Date picker
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (dp, y, m, d) -> etDate.setText(String.format("%04d-%02d-%02d", y, m+1, d)),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // 5) Time picker
        etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(requireContext(),
                    (tp, h, mm) -> etTime.setText(String.format("%02d:%02d", h, mm)),
                    c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE),
                    true
            ).show();
        });

        // 6) Submit — fetch patient name, then create appointment
        btnSubmit.setOnClickListener(v -> {
            if (doctors.isEmpty()) {
                Toast.makeText(requireContext(), "Doctors still loading…", Toast.LENGTH_SHORT).show();
                return;
            }
            String dt = etDate.getText().toString().trim();
            String tm = etTime.getText().toString().trim();
            if (dt.isEmpty() || tm.isEmpty()) {
                Toast.makeText(requireContext(), "Pick both date & time", Toast.LENGTH_SHORT).show();
                return;
            }
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) {
                Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_LONG).show();
                return;
            }

            // a) Fetch patient name from Firestore
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        String first = userDoc.getString("firstName");
                        String last  = userDoc.getString("lastName");
                        String patientName = "";
                        if (first != null) patientName = first;
                        if (last != null)  patientName += " " + last;

                        // b) Build appointment
                        Appointment a = new Appointment();
                        a.setType(spinnerType.getSelectedItem().toString());
                        int pos = spinnerDoctor.getSelectedItemPosition();
                        pos = Math.max(0, Math.min(pos, doctors.size() - 1));
                        User selected = doctors.get(pos);
                        a.setDoctorId(selected.getId());
                        a.setDoctorName(selected.getDisplayName());
                        a.setPatientId(uid);
                        a.setPatientName(patientName);

                        // c) Parse date/time
                        try {
                            String[] date = dt.split("-");
                            String[] time = tm.split(":");
                            Calendar cal = Calendar.getInstance();
                            cal.set(
                                    Integer.parseInt(date[0]),
                                    Integer.parseInt(date[1]) - 1,
                                    Integer.parseInt(date[2]),
                                    Integer.parseInt(time[0]),
                                    Integer.parseInt(time[1])
                            );
                            a.setDateTime(new Timestamp(cal.getTime()));
                        } catch (Exception ignored) {}

                        a.setStatus("Requested");

                        // d) Save
                        repo.create(a, new AppointmentRepository.Callback<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(requireContext(),
                                        "Appointment requested!", Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            }
                            @Override
                            public void onError(Throwable t) {
                                Toast.makeText(requireContext(),
                                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(),
                            "Failed fetching patient info: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });
    }
}