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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.R;

import java.util.HashMap;
import java.util.Map;

public class PatientDataFragment extends Fragment {

    private TextInputEditText etFirst, etLast, etPhone, etDOB, etEmail;
    private MaterialButton btnSave;
    private FirebaseFirestore db;
    private String uid;

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_patient_data, container, false);


        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return v;
        }
        uid = user.getUid();


        etFirst = v.findViewById(R.id.etFirstName);
        etLast  = v.findViewById(R.id.etLastName);
        etPhone = v.findViewById(R.id.etPhone);
        etDOB   = v.findViewById(R.id.etDOB);
        etEmail = v.findViewById(R.id.etEmail);
        btnSave = v.findViewById(R.id.btnSaveProfile);


        loadProfile();


        btnSave.setOnClickListener(x -> saveProfile());

        return v;
    }

    private void loadProfile() {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(getContext(), "Profile data missing", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    etFirst.setText(doc.getString("firstName"));
                    etLast .setText(doc.getString("lastName"));
                    etPhone.setText(doc.getString("phone"));
                    etDOB  .setText(doc.getString("dob"));
                    etEmail.setText(doc.getString("email"));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to load: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void saveProfile() {
        String first = etFirst.getText().toString().trim();
        String last  = etLast .getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String dob   = etDOB  .getText().toString().trim();

        if (TextUtils.isEmpty(first)
                || TextUtils.isEmpty(last)
                || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(dob)) {
            Toast.makeText(getContext(),
                    "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String,Object> upd = new HashMap<>();
        upd.put("firstName", first);
        upd.put("lastName",  last);
        upd.put("phone",     phone);
        upd.put("dob",       dob);
        upd.put("updatedAt", Timestamp.now());

        db.collection("users")
                .document(uid)
                .update(upd)
                .addOnSuccessListener(v ->
                        Toast.makeText(getContext(),
                                "Profile saved", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Save failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
}
