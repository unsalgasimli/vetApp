package com.unsalGasimliApplicationsNSUG.vetapp;

import com.google.firebase.Timestamp;

public class Appointment extends ReservationItem {
    private String date;                  // Formatted date (e.g., "2025-03-24")
    private String time;                  // Formatted time (e.g., "10:30")
    private String appointmentCategory;   // E.g., "Consultation", "Vaccination", etc.
    private String info;
    private String status;
    private String type; // Optional local type field; the base class ReservationItem already stores the overall reservation type.

    // Default constructor sets the overall reservation type to APPOINTMENT.
    public Appointment() {
        setAppointmentType(ReservationType.APPOINTMENT);
    }

    public Appointment(String id, String docFullName, String patFullName, String petName,
                       String date, String time, String appointmentCategory, String info, String status, Timestamp timestamp) {
        super(id, ReservationType.APPOINTMENT, docFullName, patFullName, petName, timestamp);
        this.date = date;
        this.time = time;
        this.appointmentCategory = appointmentCategory;
        this.info = info;
        this.status = status;
        // Optionally, set the local type field as well.
        this.type = "APPOINTMENT";
    }

    // Getters and setters for appointment-specific fields.
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    // New getter and setter for the appointment category.
    public String getAppointmentCategory() {
        return appointmentCategory;
    }
    public void setAppointmentCategory(String appointmentCategory) {
        this.appointmentCategory = appointmentCategory;
    }

    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    // Optional local type field getter and setter.
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
