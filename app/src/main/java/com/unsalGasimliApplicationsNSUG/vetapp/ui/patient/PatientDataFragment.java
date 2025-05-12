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

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentPatientDataBinding;

import java.util.HashMap;
import java.util.Map;

public class PatientDataFragment extends Fragment {
    private FragmentPatientDataBinding binding;
    private FirebaseFirestore db;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPatientDataBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), R.string.user_not_logged_in, Toast.LENGTH_SHORT).show();
            return;
        }
        uid = user.getUid();

        loadProfile();
        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(requireContext(), R.string.profile_data_missing, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    binding.etFirstName.setText(doc.getString("firstName"));
                    binding.etLastName.setText(doc.getString("lastName"));
                    binding.etPhone.setText(doc.getString("phone"));
                    binding.etDOB.setText(doc.getString("dob"));
                    binding.etEmail.setText(doc.getString("email"));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), getString(R.string.load_failed, e.getMessage()), Toast.LENGTH_LONG).show()
                );
    }

    private void saveProfile() {
        String first = binding.etFirstName.getText().toString().trim();
        String last = binding.etLastName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String dob = binding.etDOB.getText().toString().trim();

        if (TextUtils.isEmpty(first) || TextUtils.isEmpty(last)
                || TextUtils.isEmpty(phone) || TextUtils.isEmpty(dob)) {
            Toast.makeText(requireContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> upd = new HashMap<>();
        upd.put("firstName", first);
        upd.put("lastName", last);
        upd.put("phone", phone);
        upd.put("dob", dob);
        upd.put("updatedAt", Timestamp.now());

        db.collection("users")
                .document(uid)
                .update(upd)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(requireContext(), R.string.profile_saved, Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), getString(R.string.save_failed, e.getMessage()), Toast.LENGTH_LONG).show()
                );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}