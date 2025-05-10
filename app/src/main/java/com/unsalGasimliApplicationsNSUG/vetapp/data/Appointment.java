// Appointment.java
package com.unsalGasimliApplicationsNSUG.vetapp.data;

import com.google.firebase.Timestamp;

public class Appointment extends ReservationItem {
    private String docFullName;
    private String patFullName;
    private String petName;
    private String date;
    private String time;
    private String appointmentCategory;
    private String info;
    private String status;
    private Timestamp timestamp;
    private String type; // you write "APPOINTMENT" here

    public Appointment() {}

    // — Getter/setters for all fields —
    public String getDocFullName()     { return docFullName; }
    public void setDocFullName(String d){ this.docFullName = d; }

    public String getPatFullName()     { return patFullName; }
    public void setPatFullName(String p){ this.patFullName = p; }

    @Override
    public String getPetName()         { return petName; }
    public void setPetName(String p)   { this.petName = p; }

    public String getDate()            { return date; }
    public void setDate(String d)      { this.date = d; }

    public String getTime()            { return time; }
    public void setTime(String t)      { this.time = t; }

    public String getAppointmentCategory() { return appointmentCategory; }
    public void setAppointmentCategory(String c) {
        this.appointmentCategory = c;
    }

    public String getInfo()            { return info; }
    public void setInfo(String i)      { this.info = i; }

    public String getStatus()          { return status; }
    public void setStatus(String s)    { this.status = s; }

    @Override
    public Timestamp getTimestamp()    { return timestamp; }
    public void setTimestamp(Timestamp t) { this.timestamp = t; }

    public String getType()            { return type; }
    public void setType(String t)      { this.type = t; }

    @Override
    public ReservationType getReservationType() {
        return ReservationType.APPOINTMENT;
    }
}
