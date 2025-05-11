// File: app/src/main/java/com/unsalGasimliApplicationsNSUG/vetapp/data/PrescriptionRepository.java
package com.unsalGasimliApplicationsNSUG.vetapp.data;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Prescription;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionRepository {
    public interface Callback<T> {
        void onSuccess(T data);
        void onError(Throwable t);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Save a new prescription under /users/{patientId}/prescriptions */
    public void create(@NonNull Prescription p,
                       @NonNull Callback<Void> cb) {
        CollectionReference col = db
                .collection("users")
                .document(p.getPatientId())
                .collection("prescriptions");
        DocumentReference docRef = col.document();
        p.setId(docRef.getId());
        docRef.set(p)
                .addOnSuccessListener(__ -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    /** Fetch prescriptions for a specific patient */
    public void fetchForPatient(@NonNull String patientId,
                                @NonNull Callback<List<Prescription>> cb) {
        db.collection("users")
                .document(patientId)
                .collection("prescriptions")
                .orderBy("dateTimestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    List<Prescription> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        Prescription p = doc.toObject(Prescription.class);
                        p.setId(doc.getId());
                        list.add(p);
                    }
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onError);
    }

    /** Fetch prescriptions created by a specific doctor across all patients */
    public void fetchForDoctor(@NonNull String doctorId,
                               @NonNull Callback<List<Prescription>> cb) {
        db.collectionGroup("prescriptions")
                .whereEqualTo("doctorId", doctorId)
                .orderBy("dateTimestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    List<Prescription> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        Prescription p = doc.toObject(Prescription.class);
                        p.setId(doc.getId());
                        list.add(p);
                    }
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onError);
    }

    /** Fetch all prescriptions across all patients */
    public void fetchAllPrescriptions(@NonNull Callback<List<Prescription>> cb) {
        db.collectionGroup("prescriptions")
                .orderBy("dateTimestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    List<Prescription> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        Prescription p = doc.toObject(Prescription.class);
                        p.setId(doc.getId());
                        list.add(p);
                    }
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onError);
    }

    /** Delete a prescription by patientId and prescriptionId */
    public void deletePrescription(@NonNull String patientId,
                                   @NonNull String prescriptionId,
                                   @NonNull Callback<Void> cb) {
        db.collection("users")
                .document(patientId)
                .collection("prescriptions")
                .document(prescriptionId)
                .delete()
                .addOnSuccessListener(__ -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    public void updatePrescription(@NonNull Prescription p,
                                   @NonNull Callback<Void> cb) {
        db.collection("users")
                .document(p.getPatientId())
                .collection("prescriptions")
                .document(p.getId())
                .set(p)                          // overwrite with new fields
                .addOnSuccessListener(__ -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }





    public void fetchById(@NonNull String patientId,
                          @NonNull String prescriptionId,
                          @NonNull Callback<Prescription> cb) {
        db.collection("users")
                .document(patientId)
                .collection("prescriptions")
                .document(prescriptionId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Prescription p = doc.toObject(Prescription.class);
                        cb.onSuccess(p);
                    } else {
                        cb.onError(new IllegalArgumentException("No such prescription"));
                    }
                })
                .addOnFailureListener(cb::onError);
    }
}