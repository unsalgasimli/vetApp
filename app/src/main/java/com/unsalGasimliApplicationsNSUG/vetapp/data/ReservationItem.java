// ReservationItem.java
package com.unsalGasimliApplicationsNSUG.vetapp.data;

import com.google.firebase.Timestamp;

public abstract class ReservationItem {
    private String id;
    public String getId()           { return id; }
    public void setId(String id)    { this.id = id; }

    /** For sorting */
    public abstract Timestamp getTimestamp();

    /** Used by adapter to pick view‐type */
    public abstract ReservationType getReservationType();

    /** Used in filtering by pet */
    public abstract String getPetName();
}
