package com.unsalGasimliApplicationsNSUG.vetapp.data.repository;

import com.google.firebase.firestore.*;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.User;

import java.util.ArrayList;
import java.util.List;


public class AppointmentRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    public interface Callback<T> {
        void onSuccess(T data);
        void onError(Throwable t);
    }

    private String pathFor(String patientId) {
        return "users/" + patientId + "/appointments";
    }


    public void fetchForPatient(String patientId, Callback<List<Appointment>> cb) {
        db.collection(pathFor(patientId))
                .get()
                .addOnSuccessListener(snap -> {
                    List<Appointment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        Appointment a = doc.toObject(Appointment.class);
                        a.setId(doc.getId());
                        list.add(a);
                    }
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onError);
    }


    public void fetchPatients(Callback<List<User>> cb) {
        db.collection("users")
                .whereEqualTo("role", "patient")
                .get()
                .addOnSuccessListener(qs -> {
                    List<User> out = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        User u = doc.toObject(User.class);
                        u.setId(doc.getId());
                        out.add(u);
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onError);
    }

    public void fetchAllAppointments(Callback<List<Appointment>> cb) {
        db.collectionGroup("appointments")
                .get()
                .addOnSuccessListener(snap -> {
                    List<Appointment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        Appointment a = doc.toObject(Appointment.class);
                        a.setId(doc.getId());

                        DocumentReference parent = doc.getReference().getParent().getParent();
                        if (parent != null) {
                            a.setPatientId(parent.getId());
                        }
                        list.add(a);
                    }
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onError);
    }


    public void createAppointment(Appointment a, Callback<Void> cb) {
        DocumentReference ref = db.collection(pathFor(a.getPatientId())).document();
        a.setId(ref.getId());
        ref.set(a)
                .addOnSuccessListener(__ -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    public void updateAppointment(Appointment a, Callback<Void> cb) {
        db.collection(pathFor(a.getPatientId()))
                .document(a.getId())
                .set(a, SetOptions.merge())
                .addOnSuccessListener(__ -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }


    public void deleteAppointment(String patientId, String apptId, Callback<Void> cb) {
        db.collection(pathFor(patientId))
                .document(apptId)
                .delete()
                .addOnSuccessListener(__ -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }
}
