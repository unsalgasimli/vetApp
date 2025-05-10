package com.unsalGasimliApplicationsNSUG.vetapp;  // or your package

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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.adapters.StaffAdapter;
import com.unsalGasimliApplicationsNSUG.vetapp.models.Staff;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageStaffFragment extends Fragment {

    private TextInputEditText etFirst, etLast, etEmail, etPhone, etPosition, etDepartment;
    private MaterialButton btnAction, btnDelete;
    private RecyclerView rvStaff;
    private StaffAdapter adapter;
    private List<Staff> staffList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String selectedId = null; // null = ADD mode

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_manage_staff, container, false);

        etFirst      = v.findViewById(R.id.etStaffFirst);
        etLast       = v.findViewById(R.id.etStaffLast);
        etEmail      = v.findViewById(R.id.etStaffEmail);
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
            etFirst     .setText(s.getFirstName());
            etLast      .setText(s.getLastName());
            etEmail     .setText(s.getEmail());
            etPhone     .setText(s.getPhone());
            etPosition  .setText(s.getPosition());
            etDepartment.setText(s.getDepartment());
            btnAction.setText("Update Staff");
        });

        btnAction.setOnClickListener(x -> checkAndSubmit());
        btnDelete.setOnClickListener(x -> deleteStaff());

        loadStaff();
        return v;
    }

    private void checkAndSubmit() {
        String first = etFirst.getText().toString().trim();
        String last  = etLast .getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String pos   = etPosition.getText().toString().trim();
        String dept  = etDepartment.getText().toString().trim();

        if (TextUtils.isEmpty(first)||TextUtils.isEmpty(last)||
                TextUtils.isEmpty(email)||TextUtils.isEmpty(phone)||
                TextUtils.isEmpty(pos)||TextUtils.isEmpty(dept)) {
            Toast.makeText(getContext(),"Fill all fields",Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedId==null) addNewStaff(first,last,email,phone,pos,dept);
        else updateStaff(first,last,email,phone,pos,dept);
    }

    private void addNewStaff(String f,String l,String e,String p,String pos,String d) {
        String id = db.collection("users").document().getId();
        Map<String,Object> data = new HashMap<>();
        data.put("firstName", f);
        data.put("lastName",  l);
        data.put("email",     e);
        data.put("phone",     p);
        data.put("position",  pos);
        data.put("department",d);
        data.put("role","staff");
        data.put("registeredAt", Timestamp.now());

        db.collection("users").document(id)
                .set(data)
                .addOnSuccessListener(a->{
                    Toast.makeText(getContext(),"Staff added",Toast.LENGTH_SHORT).show();
                    resetForm();
                })
                .addOnFailureListener(e->Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show());
    }

    private void updateStaff(String f,String l,String e,String p,String pos,String d) {
        Map<String,Object> upd=new HashMap<>();
        upd.put("firstName", f);
        upd.put("lastName",  l);
        upd.put("email",     e);
        upd.put("phone",     p);
        upd.put("position",  pos);
        upd.put("department",d);

        db.collection("users").document(selectedId)
                .update(upd)
                .addOnSuccessListener(a->{
                    Toast.makeText(getContext(),"Staff updated",Toast.LENGTH_SHORT).show();
                    resetForm();
                })
                .addOnFailureListener(e->Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show());
    }

    private void deleteStaff() {
        if (selectedId==null) {
            Toast.makeText(getContext(),"Select a staff to delete",Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("users").document(selectedId)
                .delete()
                .addOnSuccessListener(a->{
                    Toast.makeText(getContext(),"Deleted",Toast.LENGTH_SHORT).show();
                    resetForm();
                })
                .addOnFailureListener(e->Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show());
    }

    private void loadStaff() {
        db.collection("users").whereEqualTo("role","staff")
                .get()
                .addOnSuccessListener(snapshot->{
                    staffList.clear();
                    for (QueryDocumentSnapshot doc: snapshot) {
                        staffList.add(doc.toObject(Staff.class));
                    }
                    adapter.setStaff(staffList);
                });
    }

    private void resetForm() {
        etFirst.setText(""); etLast.setText("");
        etEmail.setText(""); etPhone.setText("");
        etPosition.setText(""); etDepartment.setText("");
        selectedId = null;
        btnAction.setText("Add Staff");
        loadStaff();
    }
}
