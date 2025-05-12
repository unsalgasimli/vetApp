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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupTypeSpinner();
        loadDoctors();
        setupDatePicker();
        setupTimePicker();
        binding.btnSubmit.setOnClickListener(v -> submit());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.appointment_types,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTypes.setAdapter(adapter);
    }

    private void loadDoctors() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("role", "staff")
                .get()
                .addOnSuccessListener(snap -> {
                    doctors.clear();
                    List<String> names = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        User u = doc.toObject(User.class);
                        doctors.add(u);
                        names.add(u.getDisplayName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spinnerDoctors.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                getString(R.string.failed_loading_doctors, e.getMessage()),
                                Toast.LENGTH_LONG).show());
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

    private void submit() {
        if (doctors.isEmpty()) {
            Toast.makeText(requireContext(), R.string.doctors_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        String date = binding.etDate.getText().toString().trim();
        String time = binding.etTime.getText().toString().trim();
        if (date.isEmpty() || time.isEmpty()) {
            Toast.makeText(requireContext(), R.string.pick_date_time, Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(requireContext(), R.string.user_not_authenticated, Toast.LENGTH_LONG).show();
            return;
        }
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String name = combine(userDoc.getString("firstName"), userDoc.getString("lastName"));
                    saveAppointment(uid, name, date, time);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                getString(R.string.failed_fetching_patient, e.getMessage()),
                                Toast.LENGTH_LONG).show());
    }

    private void saveAppointment(String pid, String pname, String date, String time) {
        int pos = binding.spinnerDoctors.getSelectedItemPosition();
        pos = Math.max(0, Math.min(pos, doctors.size() - 1));
        User doc = doctors.get(pos);
        Timestamp ts;
        try {
            String[] d = date.split("-");
            String[] t = time.split(":");
            Calendar c = Calendar.getInstance();
            c.set(Integer.parseInt(d[0]), Integer.parseInt(d[1]) - 1, Integer.parseInt(d[2]),
                    Integer.parseInt(t[0]), Integer.parseInt(t[1]));
            ts = new Timestamp(c.getTime());
        } catch (Exception ex) { ts = null; }
        Appointment a = new Appointment();
        a.setType(binding.spinnerTypes.getSelectedItem().toString());
        a.setDoctorId(doc.getId());
        a.setDoctorName(doc.getDisplayName());
        a.setPatientId(pid);
        a.setPatientName(pname);
        a.setDateTime(ts);
        a.setStatus("Requested");
        repo.create(a, new AppointmentRepository.Callback<Void>() {
            @Override public void onSuccess(Void data) {
                Toast.makeText(requireContext(), R.string.appointment_requested, Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
            @Override public void onError(Throwable t) {
                Toast.makeText(requireContext(),
                        getString(R.string.create_error, t.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @NonNull
    private static String combine(String f, String l) {
        StringBuilder sb = new StringBuilder();
        if (f != null) sb.append(f);
        if (l != null) sb.append(" ").append(l);
        return sb.toString().trim();
    }
}