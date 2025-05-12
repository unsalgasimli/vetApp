package com.unsalGasimliApplicationsNSUG.vetapp.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Prescription;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(Throwable t);
    }

    private CollectionReference prescriptionsOf(String patientId) {
        return db.collection("users")
                .document(patientId)
                .collection("prescriptions");
    }

    public void fetchAllPrescriptions(Callback<List<Prescription>> cb) {
        db.collectionGroup("prescriptions")
                .get()
                .addOnSuccessListener(qs -> {
                    List<Prescription> all = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        Prescription p = doc.toObject(Prescription.class);
                        String patientId = doc.getReference()
                                .getParent()
                                .getParent()
                                .getId();
                        p.setPatientId(patientId);
                        p.setId(doc.getId());
                        all.add(p);
                    }
                    cb.onSuccess(all);
                })
                .addOnFailureListener(cb::onError);
    }

    public void fetchById(String patientId, String prescId, Callback<Prescription> cb) {
        prescriptionsOf(patientId)
                .document(prescId)
                .get()
                .addOnSuccessListener(doc -> {
                    Prescription p = doc.toObject(Prescription.class);
                    if (p != null) {
                        p.setId(doc.getId());
                        p.setPatientId(patientId);
                    }
                    cb.onSuccess(p);
                })
                .addOnFailureListener(cb::onError);
    }

    public void fetchForPatient(String patientId, Callback<List<Prescription>> cb) {
        prescriptionsOf(patientId)
                .get()
                .addOnSuccessListener(qs -> {
                    List<Prescription> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        Prescription p = doc.toObject(Prescription.class);
                        p.setId(doc.getId());
                        p.setPatientId(patientId);
                        list.add(p);
                    }
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onError);
    }

    public void create(Prescription p, Callback<Void> cb) {
        DocumentReference ref = prescriptionsOf(p.getPatientId()).document();
        p.setId(ref.getId());
        ref.set(p)
                .addOnSuccessListener(aVoid -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    public void updatePrescription(Prescription p, Callback<Void> cb) {
        prescriptionsOf(p.getPatientId())
                .document(p.getId())
                .set(p, SetOptions.merge())
                .addOnSuccessListener(aVoid -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    public void deletePrescription(String patientId, String prescId, Callback<Void> cb) {
        prescriptionsOf(patientId)
                .document(prescId)
                .delete()
                .addOnSuccessListener(aVoid -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    public Task<Void> create(Prescription p) {
        DocumentReference ref = prescriptionsOf(p.getPatientId()).document();
        p.setId(ref.getId());
        return ref.set(p);
    }

    public Task<Void> update(Prescription p) {
        return prescriptionsOf(p.getPatientId())
                .document(p.getId())
                .set(p, SetOptions.merge());
    }

    public Task<Void> delete(String patientId, String prescId) {
        return prescriptionsOf(patientId)
                .document(prescId)
                .delete();
    }
}
