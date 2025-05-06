package com.unsalGasimliApplicationsNSUG.vetapp;

import com.google.firebase.Timestamp;

public class Reservation {
    private String id;
    private String by;
    private String pet;
    private String date; // formatted "yyyy-MM-dd"
    private String time; // e.g., "10:30"
    private String status; // e.g., "Reserved", "Cancelled"
    private String reason;
    private String veterinarian;
    private Timestamp reservationTime; // Used for calendar date highlighting

    public Reservation() {
        // Empty constructor required for Firestore
    }

    public Reservation(String id, String by, String pet, String date, String time,
                       String status, String reason, String veterinarian, Timestamp reservationTime) {
        this.id = id;
        this.by = by;
        this.pet = pet;
        this.date = date;
        this.time = time;
        this.status = status;
        this.reason = reason;
        this.veterinarian = veterinarian;
        this.reservationTime = reservationTime;
    }

    // Getters
    public String getId() { return id; }
    public String getBy() { return by; }
    public String getPet() { return pet; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public String getVeterinarian() { return veterinarian; }
    public Timestamp getReservationTime() { return reservationTime; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setBy(String by) { this.by = by; }
    public void setPet(String pet) { this.pet = pet; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setStatus(String status) { this.status = status; }
    public void setReason(String reason) { this.reason = reason; }
    public void setVeterinarian(String veterinarian) { this.veterinarian = veterinarian; }
    public void setReservationTime(Timestamp reservationTime) { this.reservationTime = reservationTime; }
}
