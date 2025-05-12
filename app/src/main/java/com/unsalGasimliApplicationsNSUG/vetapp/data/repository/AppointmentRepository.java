package com.unsalGasimliApplicationsNSUG.vetapp.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository {

    public interface Callback<T> {
        void onSuccess(T data);

        void onError(Throwable t);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    public void create(@NonNull Appointment appt, @NonNull Callback<Void> cb) {
        CollectionReference col = db
                .collection("users")
                .document(appt.getPatientId())
                .collection("appointments");
        DocumentReference docRef = col.document();
        appt.setId(docRef.getId());
        docRef.set(appt)
                .addOnSuccessListener(__ -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    public void fetchAll(@NonNull String patientId, @NonNull Callback<List<Appointment>> cb) {
        db.collection("users")
                .document(patientId)
                .collection("appointments")
                .orderBy("dateTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    List<Appointment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        Appointment a = doc.toObject(Appointment.class);
                        list.add(a);
                    }
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onError);
    }


    public void fetchAppointments(@NonNull String patientId, @NonNull Callback<List<Appointment>> cb) {
        fetchAll(patientId, cb);
    }


    public void fetchDoctors(@NonNull Callback<List<User>> cb) {
        db.collection("users")
                .whereEqualTo("role", "doctor")
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    List<User> doctors = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        doctors.add(doc.toObject(User.class));
                    }
                    cb.onSuccess(doctors);
                })
                .addOnFailureListener(cb::onError);
    }


    public void fetchPatients(@NonNull Callback<List<User>> cb) {
        db.collection("users")
                .whereEqualTo("role", "patient")
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    List<User> patients = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        patients.add(doc.toObject(User.class));
                    }
                    cb.onSuccess(patients);
                })
                .addOnFailureListener(cb::onError);
    }

    public void fetchAllAppointments(@NonNull Callback<List<Appointment>> cb) {
        db.collectionGroup("appointments")
                .orderBy("dateTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    List<Appointment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        Appointment a = doc.toObject(Appointment.class);
                        a.setId(doc.getId());
                        list.add(a);
                    }
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onError);
    }

    public void updateStatus(@NonNull String patientId,
                             @NonNull String appointmentId,
                             @NonNull String newStatus,
                             @NonNull Callback<Void> cb) {
        DocumentReference docRef = db
                .collection("users")
                .document(patientId)
                .collection("appointments")
                .document(appointmentId);
        docRef.update("status", newStatus)
                .addOnSuccessListener(__ -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }


}
