// ManagePatientFragment.java
package com.unsalGasimliApplicationsNSUG.vetapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.adapters.PatientAdapter;
import com.unsalGasimliApplicationsNSUG.vetapp.models.Patient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagePatientFragment extends Fragment {

    private TextInputEditText etFirstName, etLastName, etEmail, etPhone, etDOB;
    private MaterialButton btnAddPatient, btnUpdatePatient, btnDeletePatient;
    private RecyclerView rvPatients;
    private PatientAdapter adapter;
    private List<Patient> patients = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String selectedId = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_manage_patient, container, false);

        // bind
        etFirstName      = v.findViewById(R.id.etFirstName);
        etLastName       = v.findViewById(R.id.etLastName);
        etEmail          = v.findViewById(R.id.etEmail);
        etPhone          = v.findViewById(R.id.etPhone);
        etDOB            = v.findViewById(R.id.etDOB);
        btnAddPatient    = v.findViewById(R.id.btnAddPatient);
        btnUpdatePatient = v.findViewById(R.id.btnUpdatePatient);
        btnDeletePatient = v.findViewById(R.id.btnDeletePatient);
        rvPatients       = v.findViewById(R.id.rvPatients);

        // RecyclerView setup
        adapter = new PatientAdapter(patients);
        rvPatients.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPatients.setAdapter(adapter);

        adapter.setOnItemClickListener(p -> {
            selectedId = p.getUniqueId();
            etFirstName.setText(p.getFirstName());
            etLastName .setText(p.getLastName());
            etEmail    .setText(p.getEmail());
            etPhone    .setText(p.getPhone());
            etDOB      .setText(p.getDob());

            btnAddPatient.setEnabled(false);
            btnUpdatePatient.setEnabled(true);
            btnDeletePatient.setEnabled(true);
        });

        btnAddPatient.setOnClickListener(__ -> submit(false));
        btnUpdatePatient.setOnClickListener(__ -> submit(true));
        btnDeletePatient.setOnClickListener(__ -> deletePatient());

        resetForm();
        loadPatients();
        return v;
    }

    private void submit(boolean isUpdate) {
        String f = etFirstName.getText().toString().trim();
        String l = etLastName .getText().toString().trim();
        String e = etEmail    .getText().toString().trim();
        String p = etPhone    .getText().toString().trim();
        String d = etDOB      .getText().toString().trim();

        if (TextUtils.isEmpty(f)||TextUtils.isEmpty(l)
                ||TextUtils.isEmpty(e)||TextUtils.isEmpty(p)
                ||TextUtils.isEmpty(d)) {
            Toast.makeText(getContext(),
                    "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // check duplicate email
        db.collection("users")
                .whereEqualTo("role","patient")
                .whereEqualTo("email", e)
                .get()
                .addOnSuccessListener(snap -> {
                    boolean conflict = false;
                    for (QueryDocumentSnapshot doc: snap) {
                        String id = doc.getString("uniqueId");
                        if (!isUpdate || !id.equals(selectedId)) {
                            conflict = true; break;
                        }
                    }
                    if (conflict) {
                        Toast.makeText(getContext(),
                                "Email in use", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (isUpdate) updatePatient(f,l,e,p,d);
                    else          addPatient(f,l,e,p,d);
                })
                .addOnFailureListener(err -> Toast.makeText(
                        getContext(),"Check failed: "+err.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

    private void addPatient(String f, String l, String e, String p, String d) {
        String id = db.collection("users").document().getId();
        Map<String,Object> data = new HashMap<>();
        data.put("uniqueId", id);
        data.put("firstName", f);
        data.put("lastName",  l);
        data.put("email",     e);
        data.put("phone",     p);
        data.put("dob",       d);
        data.put("role",      "patient");
        data.put("registeredAt", Timestamp.now());

        db.collection("users").document(id)
                .set(data)
                .addOnSuccessListener(__ -> {
                    Toast.makeText(getContext(),"Added",Toast.LENGTH_SHORT).show();
                    resetForm(); loadPatients();
                })
                .addOnFailureListener(err -> Toast.makeText(
                        getContext(),"Add failed: "+err.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

    private void updatePatient(String f, String l, String e, String p, String d) {
        Map<String,Object> upd = new HashMap<>();
        upd.put("firstName", f);
        upd.put("lastName",  l);
        upd.put("email",     e);
        upd.put("phone",     p);
        upd.put("dob",       d);

        db.collection("users").document(selectedId)
                .update(upd)
                .addOnSuccessListener(__ -> {
                    Toast.makeText(getContext(),"Updated",Toast.LENGTH_SHORT).show();
                    resetForm(); loadPatients();
                })
                .addOnFailureListener(err -> Toast.makeText(
                        getContext(),"Update failed: "+err.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

    private void deletePatient() {
        if (selectedId==null) return;
        db.collection("users").document(selectedId)
                .delete()
                .addOnSuccessListener(__ -> {
                    Toast.makeText(getContext(),"Deleted",Toast.LENGTH_SHORT).show();
                    resetForm(); loadPatients();
                })
                .addOnFailureListener(err -> Toast.makeText(
                        getContext(),"Delete failed: "+err.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

    private void loadPatients() {
        db.collection("users")
                .whereEqualTo("role","patient")
                .get()
                .addOnSuccessListener(snap -> {
                    patients.clear();
                    for (QueryDocumentSnapshot d: snap) {
                        patients.add(d.toObject(Patient.class));
                    }
                    adapter.setPatients(patients);
                });
    }

    private void resetForm() {
        selectedId = null;
        etFirstName.getText().clear();
        etLastName .getText().clear();
        etEmail    .getText().clear();
        etPhone    .getText().clear();
        etDOB      .getText().clear();
        btnAddPatient   .setEnabled(true);
        btnUpdatePatient.setEnabled(false);
        btnDeletePatient.setEnabled(false);
    }
}
