    package com.unsalGasimliApplicationsNSUG.vetapp.ui.patient;

    import android.os.Bundle;
    import android.text.TextUtils;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.fragment.app.Fragment;
    import androidx.recyclerview.widget.LinearLayoutManager;

    import com.google.firebase.Timestamp;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.QueryDocumentSnapshot;
    import com.unsalGasimliApplicationsNSUG.vetapp.R;
    import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Patient;
    import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentManagePatientBinding;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    public class ManagePatientFragment extends Fragment {
        private FragmentManagePatientBinding binding;
        private PatientAdapter adapter;
        private final FirebaseFirestore db = FirebaseFirestore.getInstance();
        private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        private String selectedId = null;

        @Nullable
        @Override
        public View onCreateView(
                @NonNull LayoutInflater inflater,
                @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState
        ) {
            binding = FragmentManagePatientBinding.inflate(inflater, container, false);
            return binding.getRoot();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            adapter = new PatientAdapter(new ArrayList<>());
            binding.rvPatients.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.rvPatients.setAdapter(adapter);

            adapter.setOnItemClickListener(patient -> {
                selectedId = patient.getUniqueId();
                binding.etFirstName.setText(patient.getFirstName());
                binding.etLastName.setText(patient.getLastName());
                binding.etEmail.setText(patient.getEmail());
                binding.etPassword.setText("");
                binding.etPhone.setText(patient.getPhone());
                binding.etDOB.setText(patient.getDob());
                binding.btnAction.setText(R.string.action_update_patient);
                binding.btnDeletePatient.setVisibility(View.VISIBLE);
            });

            binding.btnAction.setOnClickListener(v -> {
                if (selectedId == null) createPatient();
                else updatePatient();
            });
            binding.btnDeletePatient.setOnClickListener(v -> deletePatient());

            resetForm();
            loadPatients();
        }

        private void createPatient() {
            String first = binding.etFirstName.getText().toString().trim();
            String last = binding.etLastName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();
            String dob = binding.etDOB.getText().toString().trim();

            if (TextUtils.isEmpty(first) || TextUtils.isEmpty(last)
                    || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                    || TextUtils.isEmpty(phone) || TextUtils.isEmpty(dob)) {
                Toast.makeText(requireContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();
                        Map<String, Object> data = new HashMap<>();
                        data.put("firstName", first);
                        data.put("lastName", last);
                        data.put("email", email);
                        data.put("phone", phone);
                        data.put("dob", dob);
                        data.put("role", "patient");
                        data.put("registeredAt", Timestamp.now());

                        db.collection("users").document(uid)
                                .set(data)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(requireContext(), R.string.patient_added, Toast.LENGTH_SHORT).show();
                                    resetForm();
                                    loadPatients();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), getString(R.string.firestore_failed, e.getMessage()), Toast.LENGTH_LONG).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), getString(R.string.auth_failed, e.getMessage()), Toast.LENGTH_LONG).show()
                    );
        }

        private void updatePatient() {
            String first = binding.etFirstName.getText().toString().trim();
            String last = binding.etLastName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();
            String dob = binding.etDOB.getText().toString().trim();

            if (TextUtils.isEmpty(first) || TextUtils.isEmpty(last)
                    || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)
                    || TextUtils.isEmpty(dob)) {
                Toast.makeText(requireContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> upd = new HashMap<>();
            upd.put("firstName", first);
            upd.put("lastName", last);
            upd.put("email", email);
            upd.put("phone", phone);
            upd.put("dob", dob);

            db.collection("users").document(selectedId)
                    .update(upd)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), R.string.patient_updated, Toast.LENGTH_SHORT).show();
                        resetForm();
                        loadPatients();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), getString(R.string.update_failed, e.getMessage()), Toast.LENGTH_LONG).show()
                    );
        }

        private void deletePatient() {
            if (selectedId == null) return;
            db.collection("users").document(selectedId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), R.string.patient_deleted, Toast.LENGTH_SHORT).show();
                        resetForm();
                        loadPatients();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), getString(R.string.delete_failed, e.getMessage()), Toast.LENGTH_LONG).show()
                    );
        }

        private void loadPatients() {
            db.collection("users")
                    .whereEqualTo("role", "patient")
                    .get()
                    .addOnSuccessListener(snap -> {
                        List<Patient> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snap) {
                            Patient p = doc.toObject(Patient.class);
                            p.setUniqueId(doc.getId());
                            list.add(p);
                        }
                        adapter.setPatients(list);
                    });
        }

        private void resetForm() {
            selectedId = null;
            binding.etFirstName.getText().clear();
            binding.etLastName.getText().clear();
            binding.etEmail.getText().clear();
            binding.etPassword.getText().clear();
            binding.etPhone.getText().clear();
            binding.etDOB.getText().clear();

            binding.btnAction.setText(R.string.action_add_patient);
            binding.btnDeletePatient.setVisibility(View.GONE);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;
        }
    }
