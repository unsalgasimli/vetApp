package com.unsalGasimliApplicationsNSUG.vetapp;

import com.google.firebase.Timestamp;

public abstract class ReservationItem {
    private String id;
    private ReservationType appointmentType;
    private String docFullName;
    private String patFullName;
    private String petName;
    private Timestamp timestamp; // For sorting: Appointment time or Prescription DateFrom

    public ReservationItem() { }

    public ReservationItem(String id, ReservationType type, String docFullName, String patFullName, String petName, Timestamp timestamp) {
        this.id = id;
        this.appointmentType = type;
        this.docFullName = docFullName;
        this.patFullName = patFullName;
        this.petName = petName;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    // Updated getter return type to ReservationType
    public ReservationType getAppointmentType() {
        return appointmentType;
    }
    public void setAppointmentType(ReservationType type) {
        this.appointmentType = type;
    }

    public String getDocFullName() {
        return docFullName;
    }
    public void setDocFullName(String docFullName) {
        this.docFullName = docFullName;
    }

    public String getPatFullName() {
        return patFullName;
    }
    public void setPatFullName(String patFullName) {
        this.patFullName = patFullName;
    }

    public String getPetName() {
        return petName;
    }
    public void setPetName(String petName) {
        this.petName = petName;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
