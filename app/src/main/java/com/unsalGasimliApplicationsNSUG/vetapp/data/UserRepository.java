package com.unsalGasimliApplicationsNSUG.vetapp.data;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Throwable t);
    }
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Fetch all patients (role == "patient") */
    public void fetchPatients(Callback<List<User>> cb) {
        db.collection("users")
                .whereEqualTo("role", "patient")
                .get()
                .addOnSuccessListener(snap -> {
                    List<User> out = new ArrayList<>();
                    for (QueryDocumentSnapshot d : snap) {
                        out.add(d.toObject(User.class));
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onError);
    }

    // TODO: add createPatient, updatePatient, deletePatient as needed
}
