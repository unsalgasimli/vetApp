package com.unsalGasimliApplicationsNSUG.vetapp.ui.staff;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Staff;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentManageStaffBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageStaffFragment extends Fragment {
    private FragmentManageStaffBinding binding;
    private StaffAdapter adapter;
    private final List<Staff> staffList = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String selectedId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentManageStaffBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new StaffAdapter(staffList);
        binding.rvStaff.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvStaff.setAdapter(adapter);

        adapter.setOnItemClickListener(s -> {
            selectedId = s.getUniqueId();
            binding.etStaffFirst.setText(s.getFirstName());
            binding.etStaffLast.setText(s.getLastName());
            binding.etStaffEmail.setText(s.getEmail());
            binding.etStaffPassword.setText("");
            binding.etStaffPhone.setText(s.getPhone());
            binding.etStaffPosition.setText(s.getPosition());
            binding.etStaffDept.setText(s.getDepartment());

            binding.btnStaffAction.setText(R.string.action_update_staff);
            binding.btnStaffDelete.setVisibility(View.VISIBLE);
        });

        binding.btnStaffAction.setOnClickListener(v -> checkAndSubmit());
        binding.btnStaffDelete.setOnClickListener(v -> deleteStaff());

        loadStaff();
    }

    private void checkAndSubmit() {
        String first = binding.etStaffFirst.getText().toString().trim();
        String last = binding.etStaffLast.getText().toString().trim();
        String email = binding.etStaffEmail.getText().toString().trim();
        String password = binding.etStaffPassword.getText().toString().trim();
        String phone = binding.etStaffPhone.getText().toString().trim();
        String pos = binding.etStaffPosition.getText().toString().trim();
        String dept = binding.etStaffDept.getText().toString().trim();

        if (TextUtils.isEmpty(first) || TextUtils.isEmpty(last) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(pos) || TextUtils.isEmpty(dept)) {
            Toast.makeText(requireContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedId == null && TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(), R.string.enter_password, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedId == null) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener((AuthResult auth) -> addStaffRecord(auth.getUser().getUid(), first, last, email, phone, pos, dept))
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), getString(R.string.auth_failed, e.getMessage()), Toast.LENGTH_LONG).show());
        } else {
            updateStaff(first, last, email, phone, pos, dept);
        }
    }

    private void addStaffRecord(String uid, String first, String last, String email,
                                String phone, String position, String department) {
        Map<String,Object> data = new HashMap<>();
        data.put("firstName", first);
        data.put("lastName", last);
        data.put("email", email);
        data.put("phone", phone);
        data.put("position", position);
        data.put("department", department);
        data.put("role", "staff");
        data.put("registeredAt", Timestamp.now());

        db.collection("users").document(uid)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), R.string.staff_added, Toast.LENGTH_SHORT).show();
                    resetForm();
                    loadStaff();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), getString(R.string.firestore_failed, e.getMessage()), Toast.LENGTH_LONG).show());
    }

    private void updateStaff(String first, String last, String email,
                             String phone, String position, String department) {
        Map<String,Object> upd = new HashMap<>();
        upd.put("firstName", first);
        upd.put("lastName", last);
        upd.put("email", email);
        upd.put("phone", phone);
        upd.put("position", position);
        upd.put("department", department);

        db.collection("users").document(selectedId)
                .update(upd)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), R.string.staff_updated, Toast.LENGTH_SHORT).show();
                    resetForm();
                    loadStaff();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), getString(R.string.update_failed, e.getMessage()), Toast.LENGTH_LONG).show());
    }

    private void deleteStaff() {
        if (selectedId == null) {
            Toast.makeText(requireContext(), R.string.select_staff_first, Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("users").document(selectedId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
                    resetForm();
                    loadStaff();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), getString(R.string.delete_failed, e.getMessage()), Toast.LENGTH_LONG).show());
    }

    private void loadStaff() {
        staffList.clear();
        db.collection("users").whereEqualTo("role", "staff")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Staff s = doc.toObject(Staff.class);
                        s.setUniqueId(doc.getId());
                        staffList.add(s);
                    }
                    adapter.setStaff(staffList);
                });
    }

    private void resetForm() {
        selectedId = null;
        binding.etStaffFirst.setText("");
        binding.etStaffLast.setText("");
        binding.etStaffEmail.setText("");
        binding.etStaffPassword.setText("");
        binding.etStaffPhone.setText("");
        binding.etStaffPosition.setText("");
        binding.etStaffDept.setText("");

        binding.btnStaffAction.setText(R.string.action_add_staff);
        binding.btnStaffDelete.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
