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

public class RequestPrescriptionFragment extends Fragment {
    private static final String ARG_PATIENT_ID = "ARG_PATIENT_ID";
    private static final String ARG_PRESCRIPTION_ID = "ARG_PRESCRIPTION_ID";

    public static RequestPrescriptionFragment newInstance(@NonNull String patientId) {
        return newInstance(patientId, null);
    }

    public static RequestPrescriptionFragment newInstance(@NonNull String patientId,
                                                          @Nullable String prescriptionId) {
        RequestPrescriptionFragment frag = new RequestPrescriptionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PATIENT_ID, patientId);
        args.putString(ARG_PRESCRIPTION_ID, prescriptionId);
        frag.setArguments(args);
        return frag;
    }

    private FragmentRequestPrescriptionBinding binding;
    private String patientId;
    private String prescriptionId;
    private String doctorName = "";
    private final PrescriptionRepository repo = new PrescriptionRepository();
    private final List<User> patients = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            patientId = args.getString(ARG_PATIENT_ID);
            prescriptionId = args.getString(ARG_PRESCRIPTION_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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

        // Load doctor name
        String docId = FirebaseAuth.getInstance().getUid();
        if (docId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(docId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String fn = doc.getString("firstName");
                        String ln = doc.getString("lastName");
                        doctorName = (fn == null ? "" : fn) + " " + (ln == null ? "" : ln);
                    });
        }

        // Populate patients
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
                        names.add(u.getFirstName() + " " + u.getLastName());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            names
                    );
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spinnerPatients.setAdapter(spinnerAdapter);

                    if (prescriptionId != null) {
                        repo.fetchById(patientId, prescriptionId, new PrescriptionRepository.Callback<Prescription>() {
                            @Override
                            public void onSuccess(Prescription p) {
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

                            @Override
                            public void onError(Throwable t) {
                                Toast.makeText(requireContext(),
                                        getString(R.string.load_failed, t.getMessage()),
                                        Toast.LENGTH_LONG
                                ).show();
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

        // Date pickers
        binding.etStartDate.setOnClickListener(v -> pickDate(binding.etStartDate));
        binding.etEndDate.setOnClickListener(v -> pickDate(binding.etEndDate));

        // Save
        binding.btnPrescribe.setOnClickListener(v -> {
            int pos = binding.spinnerPatients.getSelectedItemPosition();
            if (pos < 0 || pos >= patients.size()) {
                Toast.makeText(requireContext(), R.string.select_patient, Toast.LENGTH_SHORT).show();
                return;
            }
            User u = patients.get(pos);
            String name = binding.etPrescriptionName.getText().toString().trim();
            String freq = binding.etFrequency.getText().toString().trim();
            String start = binding.etStartDate.getText().toString().trim();
            String end = binding.etEndDate.getText().toString().trim();
            if (name.isEmpty() || freq.isEmpty() || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(requireContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            Prescription p = new Prescription();
            p.setId(prescriptionId != null ? prescriptionId : "");
            p.setName(name);
            p.setFrequency(freq);
            p.setStartDate(start);
            p.setEndDate(end);
            p.setPatientId(u.getId());
            p.setPatientName(u.getFirstName() + " " + u.getLastName());
            p.setDoctorId(docId);
            p.setDoctorName(doctorName);

            if (prescriptionId == null) {
                repo.create(p, new PrescriptionRepository.Callback<Void>() {
                    @Override
                    public void onSuccess(Void d) {
                        Toast.makeText(requireContext(), R.string.prescription_added, Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }

                    @Override
                    public void onError(Throwable t) {
                        Toast.makeText(requireContext(),
                                getString(R.string.prescription_error, t.getMessage()),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            } else {
                repo.updatePrescription(p, new PrescriptionRepository.Callback<Void>() {
                    @Override
                    public void onSuccess(Void d) {
                        Toast.makeText(requireContext(), R.string.prescription_updated, Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }

                    @Override
                    public void onError(Throwable t) {
                        Toast.makeText(requireContext(),
                                getString(R.string.prescription_error, t.getMessage()),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        });
    }

    private void pickDate(EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
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