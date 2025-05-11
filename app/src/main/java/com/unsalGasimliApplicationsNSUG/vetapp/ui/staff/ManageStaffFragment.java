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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Staff;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageStaffFragment extends Fragment {

    private TextInputEditText etFirst, etLast, etEmail, etPassword, etPhone, etPosition, etDepartment;
    private MaterialButton btnAction, btnDelete;
    private RecyclerView rvStaff;
    private StaffAdapter adapter;
    private List<Staff> staffList = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String selectedId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_manage_staff, container, false);

        etFirst      = v.findViewById(R.id.etStaffFirst);
        etLast       = v.findViewById(R.id.etStaffLast);
        etEmail      = v.findViewById(R.id.etStaffEmail);
        etPassword   = v.findViewById(R.id.etStaffPassword);
        etPhone      = v.findViewById(R.id.etStaffPhone);
        etPosition   = v.findViewById(R.id.etStaffPosition);
        etDepartment = v.findViewById(R.id.etStaffDept);

        btnAction = v.findViewById(R.id.btnStaffAction);
        btnDelete = v.findViewById(R.id.btnStaffDelete);

        rvStaff = v.findViewById(R.id.rvStaff);
        rvStaff.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StaffAdapter(staffList);
        rvStaff.setAdapter(adapter);

        adapter.setOnItemClickListener(s -> {
            selectedId = s.getUniqueId();
            etFirst    .setText(s.getFirstName());
            etLast     .setText(s.getLastName());
            etEmail    .setText(s.getEmail());
            etPassword .setText("");
            etPhone    .setText(s.getPhone());
            etPosition .setText(s.getPosition());
            etDepartment.setText(s.getDepartment());

            btnAction.setText(R.string.action_update_staff);
            btnDelete.setVisibility(View.VISIBLE);
        });

        btnAction.setOnClickListener(x -> checkAndSubmit());
        btnDelete.setOnClickListener(x -> deleteStaff());

        loadStaff();
        return v;
    }

    private void checkAndSubmit() {
        String first    = etFirst   .getText().toString().trim();
        String last     = etLast    .getText().toString().trim();
        String email    = etEmail   .getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone    = etPhone   .getText().toString().trim();
        String pos      = etPosition.getText().toString().trim();
        String dept     = etDepartment.getText().toString().trim();

        if (TextUtils.isEmpty(first) || TextUtils.isEmpty(last) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(pos)   || TextUtils.isEmpty(dept)) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedId == null && TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedId == null) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener((AuthResult auth) -> {
                        String uid = auth.getUser().getUid();
                        addStaffRecord(uid, first, last, email, phone, pos, dept);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(),
                                    "Auth failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
        } else {
            updateStaff(first, last, email, phone, pos, dept);
        }
    }

    private void addStaffRecord(
            String uid, String first, String last, String email,
            String phone, String position, String department
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("uniqueId",   uid);
        data.put("firstName",  first);
        data.put("lastName",   last);
        data.put("email",      email);
        data.put("phone",      phone);
        data.put("position",   position);
        data.put("department", department);
        data.put("role",       "staff");        // ← add this
        data.put("registeredAt", Timestamp.now()); // optionally add timestamp if your model has it

        db.collection("users").document(uid)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Staff added", Toast.LENGTH_SHORT).show();
                    resetForm();
                    loadStaff();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Firestore failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }


    private void updateStaff(
            String first, String last, String email,
            String phone, String position, String department
    ) {
        Map<String, Object> upd = new HashMap<>();
        upd.put("firstName",  first);
        upd.put("lastName",   last);
        upd.put("email",      email);
        upd.put("phone",      phone);
        upd.put("position",   position);
        upd.put("department", department);

        db.collection("users").document(selectedId)
                .update(upd)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Staff updated", Toast.LENGTH_SHORT).show();
                    resetForm();
                    loadStaff();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void deleteStaff() {
        if (selectedId == null) {
            Toast.makeText(getContext(), "Select staff first", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("users").document(selectedId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                    resetForm();
                    loadStaff();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Delete failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void loadStaff() {
        staffList.clear();
        db.collection("users")
                .whereEqualTo("role", "staff")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Staff> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Staff s = doc.toObject(Staff.class);
                        s.setUniqueId(doc.getId());
                        list.add(s);
                    }
                    adapter.setStaff(list);
                });
    }

    private void resetForm() {
        selectedId = null;
        etFirst   .setText("");
        etLast    .setText("");
        etEmail   .setText("");
        etPassword.setText("");
        etPhone   .setText("");
        etPosition.setText("");
        etDepartment.setText("");

        btnAction.setText(R.string.action_add_staff);
        btnDelete.setVisibility(View.GONE);
    }
}
