// File: app/src/main/java/com/unsalGasimliApplicationsNSUG/vetapp/data/repository/StaffRepository.java
package com.unsalGasimliApplicationsNSUG.vetapp.data.repository;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Staff;

import java.util.ArrayList;
import java.util.List;

public class StaffRepository {
    private final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private final FirebaseAuth      auth = FirebaseAuth.getInstance();

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }

    public void fetchAll(Callback<List<Staff>> cb) {
        db.collection("users")
                .whereEqualTo("role", "staff")
                .get()
                .addOnSuccessListener(snap -> {
                    List<Staff> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        Staff s = doc.toObject(Staff.class);
                        s.setUniqueId(doc.getId());
                        list.add(s);
                    }
                    cb.onSuccess(list);
                })
                .addOnFailureListener(e -> cb.onError(new Exception(e)));
    }

    public void create(Staff s, String password, Callback<Void> cb) {
        auth.createUserWithEmailAndPassword(s.getEmail(), password)
                .addOnSuccessListener((AuthResult ar) -> {
                    String uid = ar.getUser().getUid();
                    s.setUniqueId(uid);
                    s.setRole("staff");
                    db.collection("users").document(uid)
                            .set(s)
                            .addOnSuccessListener(a -> cb.onSuccess(null))
                            .addOnFailureListener(e -> cb.onError(new Exception(e)));
                })
                .addOnFailureListener(e -> cb.onError(new Exception(e)));
    }

    public void update(Staff s, Callback<Void> cb) {
        db.collection("users").document(s.getUniqueId())
                .update(
                        "firstName",  s.getFirstName(),
                        "lastName",   s.getLastName(),
                        "email",      s.getEmail(),
                        "phone",      s.getPhone(),
                        "position",   s.getPosition(),
                        "department", s.getDepartment()
                )
                .addOnSuccessListener(a -> cb.onSuccess(null))
                .addOnFailureListener(e -> cb.onError(new Exception(e)));
    }

    public void delete(String uid, Callback<Void> cb) {
        db.collection("users").document(uid)
                .delete()
                .addOnSuccessListener(a -> cb.onSuccess(null))
                .addOnFailureListener(e -> cb.onError(new Exception(e)));
    }
}
