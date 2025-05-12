package com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Prescription;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.User;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentRequestPrescriptionBinding;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RequestPrescriptionFragment extends Fragment {
    private static final String ARG_PATIENT_ID      = "ARG_PATIENT_ID";
    private static final String ARG_PRESCRIPTION_ID = "ARG_PRESCRIPTION_ID";

    private FragmentRequestPrescriptionBinding binding;
    private PrescriptionViewModel vm;
    private String patientId;
    private String prescriptionId;
    private final List<User> patients = new ArrayList<>();

    public RequestPrescriptionFragment() {
        super(R.layout.fragment_request_prescription);
    }

    public static RequestPrescriptionFragment newInstance(@NonNull String patientId,
                                                          @Nullable String prescId) {
        RequestPrescriptionFragment f = new RequestPrescriptionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PATIENT_ID, patientId);
        args.putString(ARG_PRESCRIPTION_ID, prescId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            patientId = args.getString(ARG_PATIENT_ID);
            prescriptionId = args.getString(ARG_PRESCRIPTION_ID);
        }
        if (patientId == null) {
            patientId = FirebaseAuth.getInstance().getUid();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentRequestPrescriptionBinding.bind(view);
        vm = new ViewModelProvider(this).get(PrescriptionViewModel.class);

        String currUid = FirebaseAuth.getInstance().getUid();
        if (currUid != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currUid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if ("patient".equals(doc.getString("role"))) {
                            binding.btnPrescribe.setVisibility(View.GONE);
                            binding.spinnerPatients.setEnabled(false);
                            binding.etPrescriptionName.setEnabled(false);
                            binding.etFrequency.setEnabled(false);
                            binding.etStartDate.setEnabled(false);
                            binding.etEndDate.setEnabled(false);
                        }
                    });
        }

        vm.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                String[] parts = msg.split(":", 2);
                int resId = getResources().getIdentifier(parts[0], "string", requireContext().getPackageName());
                String text = (resId != 0)
                        ? (parts.length > 1 ? getString(resId, parts[1]) : getString(resId))
                        : msg;
                Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
                vm.clearMessage();
                if (!msg.startsWith("error")) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            names
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spinnerPatients.setAdapter(adapter);
                    if (prescriptionId != null) {
                        binding.btnPrescribe.setText(R.string.update_prescription);
                        vm.getPrescription().observe(getViewLifecycleOwner(), p -> {
                            if (p != null) {
                                binding.etPrescriptionName.setText(p.getName());
                                binding.etFrequency.setText(p.getFrequency());
                                binding.etStartDate.setText(p.getStartDate());
                                binding.etEndDate.setText(p.getEndDate());
                                for (int i = 0; i < patients.size(); i++) {
                                    if (patients.get(i).getId().equals(p.getPatientId())) {
                                        binding.spinnerPatients.setSelection(i);
                                        break;
                                    }
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                getString(R.string.load_patients_failed, e.getMessage()),
                                Toast.LENGTH_LONG
                        ).show()
                );

        binding.etStartDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(
                    requireContext(),
                    (dp, y, m, d) -> binding.etStartDate.setText(String.format("%04d-%02d-%02d", y, m + 1, d)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        binding.etEndDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(
                    requireContext(),
                    (dp, y, m, d) -> binding.etEndDate.setText(String.format("%04d-%02d-%02d", y, m + 1, d)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

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
            p.setPatientId(selected.getId());
            p.setPatientName(selected.getDisplayName());
            p.setName(name);
            p.setFrequency(freq);
            p.setStartDate(start);
            p.setEndDate(end);
            p.setDoctorId(FirebaseAuth.getInstance().getUid());
            if (prescriptionId != null) {
                p.setId(prescriptionId);
                vm.updatePrescription(p);
            } else {
                vm.createPrescription(p);
            }
        });

        if (prescriptionId != null) {
            vm.fetchPrescriptionById(patientId, prescriptionId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
