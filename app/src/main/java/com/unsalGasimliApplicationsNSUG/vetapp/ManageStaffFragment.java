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

public class ManageStaffFragment extends Fragment {

    private EditText firstNameEditText, lastNameEditText, emailEditText,
            phoneEditText, dobEditText, passwordEditText, confirmPasswordEditText;
    private Button addButton, updateButton, deleteButton;
    private RecyclerView recyclerView;
    private UserAdapter adapter;

    private List<User> staffList = new ArrayList<>();
    private FirebaseFirestore db;

    // To store the selected staff unique ID for update/delete operations.
    private String selectedStaffId = null;

    public ManageStaffFragment() {
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
        addButton.setOnClickListener(v -> addStaff());
        updateButton.setOnClickListener(v -> updateStaff());
        deleteButton.setOnClickListener(v -> deleteStaff());

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.usersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(staffList);
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

            // Store the selected staff's unique ID
            selectedStaffId = user.getUniqueId();
        });

        // Load staff from Firestore
        loadStaffFromFirestore();

        return view;
    }

    private void loadStaffFromFirestore() {
        db.collection("users")
                .whereEqualTo("role", "staff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        staffList.clear();
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null) {
                            for (QueryDocumentSnapshot document : snapshot) {
                                User user = document.toObject(User.class);
                                staffList.add(user);
                            }
                        }
                        adapter.setUsers(staffList);
                    } else {
                        Log.e("AdminStaffFragment", "Error loading staff: ", task.getException());
                    }
                });
    }

    private void addStaff() {
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

        // Generate a unique staff ID.
        String staffId = db.collection("users").document().getId();

        Map<String, Object> staffData = new HashMap<>();
        staffData.put("uniqueId", staffId);
        staffData.put("firstName", firstName);
        staffData.put("lastName", lastName);
        staffData.put("email", email);
        staffData.put("phone", phone);
        staffData.put("dob", dob);
        staffData.put("role", "staff");
        staffData.put("registeredAt", Timestamp.now());

        db.collection("users").document(staffId)
                .set(staffData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Staff added successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                    loadStaffFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add staff: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStaff() {
        // Use selectedStaffId from the click event; if it's null, prompt the user.
        if (selectedStaffId == null) {
            Toast.makeText(getContext(), "Select a staff member from the list to update", Toast.LENGTH_SHORT).show();
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

        db.collection("users").document(selectedStaffId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Staff updated successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                    selectedStaffId = null;
                    loadStaffFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update staff: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteStaff() {
        if (selectedStaffId == null) {
            Toast.makeText(getContext(), "Select a staff member from the list to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(selectedStaffId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Staff deleted successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                    selectedStaffId = null;
                    loadStaffFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete staff: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
