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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Patient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagePatientFragment extends Fragment {
    private TextInputEditText etFirstName, etLastName, etEmail, etPassword, etPhone, etDOB;
    private MaterialButton btnAction, btnDelete;
    private RecyclerView rvPatients;
    private PatientAdapter adapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String selectedId = null;

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_manage_patient, container, false);

        etFirstName = v.findViewById(R.id.etFirstName);
        etLastName  = v.findViewById(R.id.etLastName);
        etEmail     = v.findViewById(R.id.etEmail);
        etPassword  = v.findViewById(R.id.etPassword);
        etPhone     = v.findViewById(R.id.etPhone);
        etDOB       = v.findViewById(R.id.etDOB);
        btnAction   = v.findViewById(R.id.btnAction);
        btnDelete   = v.findViewById(R.id.btnDeletePatient);
        rvPatients  = v.findViewById(R.id.rvPatients);

        adapter = new PatientAdapter(new ArrayList<>());
        rvPatients.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPatients.setAdapter(adapter);

        adapter.setOnItemClickListener(p -> {
            selectedId = p.getUniqueId();
            etFirstName.setText(p.getFirstName());
            etLastName .setText(p.getLastName());
            etEmail    .setText(p.getEmail());
            etPassword .setText("");
            etPhone    .setText(p.getPhone());
            etDOB      .setText(p.getDob());

            btnAction.setText(R.string.action_update_patient);
            btnDelete.setVisibility(View.VISIBLE);
        });

        btnAction.setOnClickListener(__ -> {
            if (selectedId == null) createPatient();
            else                    updatePatient();
        });
        btnDelete.setOnClickListener(__ -> deletePatient());

        resetForm();
        loadPatients();
        return v;
    }

    private void createPatient() {
        String first    = etFirstName.getText().toString().trim();
        String last     = etLastName .getText().toString().trim();
        String email    = etEmail    .getText().toString().trim();
        String password = etPassword .getText().toString().trim();
        String phone    = etPhone    .getText().toString().trim();
        String dob      = etDOB      .getText().toString().trim();

        if (TextUtils.isEmpty(first) || TextUtils.isEmpty(last) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(phone) || TextUtils.isEmpty(dob)) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener((AuthResult auth) -> {
                    String uid = auth.getUser().getUid();
                    Map<String,Object> data = new HashMap<>();


                    data.put("firstName",   first);
                    data.put("lastName",    last);
                    data.put("email",       email);
                    data.put("phone",       phone);
                    data.put("dob",         dob);
                    data.put("role",        "patient");
                    data.put("registeredAt", Timestamp.now());

                    db.collection("users").document(uid)
                            .set(data)
                            .addOnSuccessListener(__ -> {
                                Toast.makeText(getContext(), "Patient added", Toast.LENGTH_SHORT).show();
                                resetForm();
                                loadPatients();
                            })
                            .addOnFailureListener(err ->
                                    Toast.makeText(getContext(),
                                            "Firestore failed: " + err.getMessage(),
                                            Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Auth failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void updatePatient() {
        String first = etFirstName.getText().toString().trim();
        String last  = etLastName .getText().toString().trim();
        String email = etEmail    .getText().toString().trim();
        String phone = etPhone    .getText().toString().trim();
        String dob   = etDOB      .getText().toString().trim();

        if (TextUtils.isEmpty(first) || TextUtils.isEmpty(last) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(dob)) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String,Object> upd = new HashMap<>();
        upd.put("firstName", first);
        upd.put("lastName",  last);
        upd.put("email",     email);
        upd.put("phone",     phone);
        upd.put("dob",       dob);

        db.collection("users").document(selectedId)
                .update(upd)
                .addOnSuccessListener(__ -> {
                    Toast.makeText(getContext(), "Patient updated", Toast.LENGTH_SHORT).show();
                    resetForm();
                    loadPatients();
                })
                .addOnFailureListener(err ->
                        Toast.makeText(getContext(),
                                "Update failed: " + err.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void deletePatient() {
        if (selectedId == null) return;
        db.collection("users").document(selectedId)
                .delete()
                .addOnSuccessListener(__ -> {
                    Toast.makeText(getContext(), "Patient deleted", Toast.LENGTH_SHORT).show();
                    resetForm();
                    loadPatients();
                })
                .addOnFailureListener(err ->
                        Toast.makeText(getContext(),
                                "Delete failed: " + err.getMessage(),
                                Toast.LENGTH_LONG).show()
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
        etFirstName.getText().clear();
        etLastName .getText().clear();
        etEmail    .getText().clear();
        etPassword .getText().clear();
        etPhone    .getText().clear();
        etDOB      .getText().clear();

        btnAction.setText(R.string.action_add_patient);
        btnDelete.setVisibility(View.GONE);
    }
}
