package com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions;

import android.app.DatePickerDialog;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.PrescriptionRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Prescription;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PrescriptionFragment extends Fragment {
    private Spinner spinnerPatients;
    private EditText etName, etFrequency, etStart, etEnd;
    private Button btnPrescribe;
    private PrescriptionRepository repo = new PrescriptionRepository();
    private List<User> patients = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_request_prescription, container, false);
        spinnerPatients = root.findViewById(R.id.spinnerPatients);
        etName    = root.findViewById(R.id.etPrescriptionName);
        etFrequency = root.findViewById(R.id.etFrequency);
        etStart   = root.findViewById(R.id.etStartDate);
        etEnd     = root.findViewById(R.id.etEndDate);
        btnPrescribe = root.findViewById(R.id.btnPrescribe);
        return root;
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String currUid = FirebaseAuth.getInstance().getUid();
        if (currUid != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currUid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String role = doc.getString("role");
                        if ("patient".equals(role)) {
                            btnPrescribe.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // failed to fetch role – leave button visible by default
                    });
        }
        // Load all patients for spinner
        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("role", "patient")
                .get()
                .addOnSuccessListener(qs -> {
                    patients.clear();
                    List<String> names = new ArrayList<>();
                    for (DocumentSnapshot doc : qs) {
                        User u = doc.toObject(User.class);
                        u.setId(doc.getId());
                        patients.add(u);
                        names.add(u.getDisplayName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            names
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPatients.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load patients: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );

        // Date pickers
        etStart.setOnClickListener(v -> pickDate(etStart));
        etEnd.setOnClickListener(v -> pickDate(etEnd));

        btnPrescribe.setOnClickListener(v -> {
            int pos = spinnerPatients.getSelectedItemPosition();
            if (pos < 0 || pos >= patients.size()) {
                Toast.makeText(requireContext(), "Select a patient", Toast.LENGTH_SHORT).show();
                return;
            }
            User selected = patients.get(pos);
            String name = etName.getText().toString().trim();
            String freq = etFrequency.getText().toString().trim();
            String start = etStart.getText().toString().trim();
            String end   = etEnd.getText().toString().trim();
            if (name.isEmpty() || freq.isEmpty() || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Prescription p = new Prescription();
            p.setName(name);
            p.setFrequency(freq);
            p.setStartDate(start);
            p.setEndDate(end);
            p.setPatientId(selected.getId());
            p.setPatientName(selected.getDisplayName());
            String doctorId = FirebaseAuth.getInstance().getUid();
            p.setDoctorId(doctorId);

            repo.create(p, new PrescriptionRepository.Callback<Void>() {
                @Override public void onSuccess(Void data) {
                    Toast.makeText(requireContext(), "Prescription saved", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
                @Override public void onError(Throwable t) {
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void pickDate(EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (dp, y, m, d) ->
                target.setText(String.format("%04d-%02d-%02d", y, m+1, d)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
}
