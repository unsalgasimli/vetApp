package com.unsalGasimliApplicationsNSUG.vetapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagePatientFragment extends Fragment {

    private EditText firstNameEditText, lastNameEditText, emailEditText,
            phoneEditText, dobEditText, passwordEditText, confirmPasswordEditText;
    private Button addButton, updateButton, deleteButton;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> patientList = new ArrayList<>();
    private FirebaseFirestore db;

    // To store the selected patient unique ID for update/delete operations.
    private String selectedpatientId = null;

    public ManagePatientFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        db = FirebaseFirestore.getInstance();

        // Initialize input fields
        firstNameEditText = view.findViewById(R.id.editTextAdmFirstName);
        lastNameEditText = view.findViewById(R.id.editTextAdmLastName);
        emailEditText = view.findViewById(R.id.editTextAdmEmail);
        phoneEditText = view.findViewById(R.id.editTextAdmPhone);
        dobEditText = view.findViewById(R.id.editTextAdmDOB);
        passwordEditText = view.findViewById(R.id.editTextAdmPassword);
        confirmPasswordEditText = view.findViewById(R.id.editTextAdmConfirmPassword);

        addButton = view.findViewById(R.id.buttonAddAdm);
        updateButton = view.findViewById(R.id.buttonUpdateAdm);
        deleteButton = view.findViewById(R.id.buttonDeleteAdm);

        // Set button listeners
        addButton.setOnClickListener(v -> addpatient());
        updateButton.setOnClickListener(v -> updatepatient());
        deleteButton.setOnClickListener(v -> deletepatient());

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.usersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(patientList);
        recyclerView.setAdapter(adapter);

        // Set item click listener on adapter
        adapter.setOnItemClickListener(user -> {
            // Populate input fields with the selected user's data
            firstNameEditText.setText(user.getFirstName());
            lastNameEditText.setText(user.getLastName());
            emailEditText.setText(user.getEmail());
            phoneEditText.setText(user.getPhone());
            dobEditText.setText(user.getDob());
            // For security reasons, you may choose not to populate password fields
            // Here we clear them:
            passwordEditText.setText("");
            confirmPasswordEditText.setText("");

            // Store the selected patient's unique ID
            selectedpatientId = user.getUniqueId();
        });

        // Load patient from Firestore
        loadpatientFromFirestore();

        return view;
    }

    private void loadpatientFromFirestore() {
        db.collection("users")
                .whereEqualTo("role", "patient")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        patientList.clear();
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null) {
                            for (QueryDocumentSnapshot document : snapshot) {
                                User user = document.toObject(User.class);
                                patientList.add(user);
                            }
                        }
                        adapter.setUsers(patientList);
                    } else {
                        Log.e("AdminPatientFragment", "Error loading staff: ", task.getException());
                    }
                });
    }


    private void addpatient() {
        final String firstName = firstNameEditText.getText().toString().trim();
        final String lastName = lastNameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String phone = phoneEditText.getText().toString().trim();
        final String dob = dobEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();
        final String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(phone) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique patient ID.
        String patientId = db.collection("users").document().getId();

        Map<String, Object> patientData = new HashMap<>();
        patientData.put("uniqueId", patientId);
        patientData.put("firstName", firstName);
        patientData.put("lastName", lastName);
        patientData.put("email", email);
        patientData.put("phone", phone);
        patientData.put("dob", dob);
        patientData.put("role", "patient");
        patientData.put("registeredAt", Timestamp.now());

        db.collection("users").document(patientId)
                .set(patientData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "patient added successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                    loadpatientFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add patient: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updatepatient() {
        // Use selectedpatientId from the click event; if it's null, prompt the user.
        if (selectedpatientId == null) {
            Toast.makeText(getContext(), "Select a patient member from the list to update", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(firstName)) updates.put("firstName", firstName);
        if (!TextUtils.isEmpty(lastName)) updates.put("lastName", lastName);
        if (!TextUtils.isEmpty(phone)) updates.put("phone", phone);
        if (!TextUtils.isEmpty(dob)) updates.put("dob", dob);

        db.collection("users").document(selectedpatientId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "patient updated successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                    selectedpatientId = null;
                    loadpatientFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update patient: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deletepatient() {
        if (selectedpatientId == null) {
            Toast.makeText(getContext(), "Select a patient member from the list to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(selectedpatientId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "patient deleted successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                    selectedpatientId = null;
                    loadpatientFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete patient: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearFields() {
        firstNameEditText.setText("");
        lastNameEditText.setText("");
        emailEditText.setText("");
        phoneEditText.setText("");
        dobEditText.setText("");
        passwordEditText.setText("");
        confirmPasswordEditText.setText("");
    }
}
