package com.unsalGasimliApplicationsNSUG.vetapp;

import com.google.firebase.Timestamp;

public class Prescription extends ReservationItem {
    private String dateFrom;  // Start date
    private String dateTo;    // End date
    private String frequency;
    private String info;
    private String type;

    public Prescription() {
        // Ensure the type is set to "Prescription"
        setType("Prescription");
    }

    public Prescription(String id, String docFullName, String patFullName, String petName,
                        String dateFrom, String dateTo, String frequency, String info, Timestamp timestamp) {
        super(id, ReservationType.PRESCRIPTION, docFullName, patFullName, petName, timestamp);
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.frequency = frequency;
        this.info = info;
        // Use the setter to assign the type consistently
        setType("Prescription");
    }

    // Getters and setters
    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getType() {
        return type;
    }

    public void setType(String reservationType) {
        this.type = reservationType;
    }
}
