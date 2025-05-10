// Prescription.java
package com.unsalGasimliApplicationsNSUG.vetapp.data;

import com.google.firebase.Timestamp;

public class Prescription extends ReservationItem {
    private String docFullName;
    private String patFullName;
    private String petName;
    private String dateFrom;
    private String dateTo;
    private String frequency;
    private String info;
    private Timestamp timestamp;
    private String type; // you write "PRESCRIPTION" here

    public Prescription() {}

    public String getDocFullName()     { return docFullName; }
    public void setDocFullName(String d){ this.docFullName = d; }

    public String getPatFullName()     { return patFullName; }
    public void setPatFullName(String p){ this.patFullName = p; }

    @Override
    public String getPetName()         { return petName; }
    public void setPetName(String p)   { this.petName = p; }

    public String getDateFrom()        { return dateFrom; }
    public void setDateFrom(String d)  { this.dateFrom = d; }

    public String getDateTo()          { return dateTo; }
    public void setDateTo(String d)    { this.dateTo = d; }

    public String getFrequency()       { return frequency; }
    public void setFrequency(String f) { this.frequency = f; }

    public String getInfo()            { return info; }
    public void setInfo(String i)      { this.info = i; }

    @Override
    public Timestamp getTimestamp()    { return timestamp; }
    public void setTimestamp(Timestamp t) { this.timestamp = t; }

    public String getType()            { return type; }
    public void setType(String t)      { this.type = t; }

    @Override
    public ReservationType getReservationType() {
        return ReservationType.PRESCRIPTION;
    }
}
