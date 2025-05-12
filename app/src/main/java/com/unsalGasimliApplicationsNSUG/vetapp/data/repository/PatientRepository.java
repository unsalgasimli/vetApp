package com.unsalGasimliApplicationsNSUG.vetapp.data.repository;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientRepository {
    private final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private final FirebaseAuth      auth = FirebaseAuth.getInstance();

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }

    public void fetchAll(Callback<List<Patient>> cb) {
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
                    cb.onSuccess(list);
                })
                .addOnFailureListener(e ->
                        cb.onError(new Exception(e))
                );
    }

    public void create(Patient p, String password, Callback<Void> cb) {
        auth.createUserWithEmailAndPassword(p.getEmail(), password)
                .addOnSuccessListener((AuthResult authRes) -> {
                    String uid = authRes.getUser().getUid();
                    p.setUniqueId(uid);
                    p.setRole("patient");
                    p.setRegisteredAt(Timestamp.now());
                    db.collection("users").document(uid)
                            .set(p)
                            .addOnSuccessListener(a -> cb.onSuccess(null))
                            .addOnFailureListener(e -> cb.onError(new Exception(e)));
                })
                .addOnFailureListener(e -> cb.onError(new Exception(e)));
    }

    public void update(Patient p, Callback<Void> cb) {
        db.collection("users").document(p.getUniqueId())
                .update(
                        "firstName", p.getFirstName(),
                        "lastName",  p.getLastName(),
                        "phone",     p.getPhone(),
                        "dob",       p.getDob()
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
