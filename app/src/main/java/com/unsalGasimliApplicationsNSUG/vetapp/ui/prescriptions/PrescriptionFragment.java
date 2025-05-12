package com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Prescription;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.User;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.PrescriptionRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentRequestPrescriptionBinding;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PrescriptionFragment extends Fragment {
    private FragmentRequestPrescriptionBinding binding;
    private final PrescriptionRepository repo = new PrescriptionRepository();
    private final List<User> patients = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRequestPrescriptionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String currUid = FirebaseAuth.getInstance().getUid();
        if (currUid != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currUid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if ("patient".equals(doc.getString("role"))) {
                            binding.btnPrescribe.setVisibility(View.GONE);

                        }
                    });
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("role", "patient")
                .get()
                .addOnSuccessListener(qs -> {
                    patients.clear();
                    List<String> names = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        User u = doc.toObject(User.class);
                        u.setId(doc.getId());
                        patients.add(u);
                        names.add(u.getDisplayName());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            names
                    );
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spinnerPatients.setAdapter(spinnerAdapter);
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(),
                        getString(R.string.load_patients_failed, e.getMessage()),
                        Toast.LENGTH_LONG).show()
                );

        binding.etStartDate.setOnClickListener(v -> pickDate(binding.etStartDate));
        binding.etEndDate.setOnClickListener(v -> pickDate(binding.etEndDate));

        binding.btnPrescribe.setOnClickListener(v -> {
            int pos = binding.spinnerPatients.getSelectedItemPosition();
            if (pos < 0 || pos >= patients.size()) {
                Toast.makeText(requireContext(), R.string.select_patient, Toast.LENGTH_SHORT).show();
                return;
            }
            User selected = patients.get(pos);
            String name = binding.etPrescriptionName.getText().toString().trim();
            String freq = binding.etFrequency.getText().toString().trim();
            String start = binding.etStartDate.getText().toString().trim();
            String end = binding.etEndDate.getText().toString().trim();
            if (name.isEmpty() || freq.isEmpty() || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(requireContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            Prescription p = new Prescription();
            p.setName(name);
            p.setFrequency(freq);
            p.setStartDate(start);
            p.setEndDate(end);
            p.setPatientId(selected.getId());
            p.setPatientName(selected.getDisplayName());
            p.setDoctorId(currUid);

            repo.create(p)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), R.string.prescription_saved, Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(),
                            getString(R.string.prescription_error, e.getMessage()),
                            Toast.LENGTH_LONG).show()
                    );
        });
    }

    private void pickDate(EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                requireContext(),
                (dp, y, m, d) -> target.setText(String.format("%04d-%02d-%02d", y, m + 1, d)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
