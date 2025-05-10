package com.unsalGasimliApplicationsNSUG.vetapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String uniqueId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String dob;            // optional for patients
    private String role;           // "patient" or "staff"
    private String position;       // optional for staff
    private String department;     // optional for staff
    private Timestamp registeredAt;

    public User() {
        // Firestore needs a public no-arg constructor
    }

    // full constructor if you like
    public User(String uniqueId, String firstName, String lastName,
                String email, String phone, String dob,
                String role, String position, String department,
                Timestamp registeredAt) {
        this.uniqueId     = uniqueId;
        this.firstName    = firstName;
        this.lastName     = lastName;
        this.email        = email;
        this.phone        = phone;
        this.dob          = dob;
        this.role         = role;
        this.position     = position;
        this.department   = department;
        this.registeredAt = registeredAt;
    }

    // Getters & setters (only getters shown for brevity)
    public String getUniqueId()     { return uniqueId; }
    public String getFirstName()    { return firstName; }
    public String getLastName()     { return lastName; }
    public String getEmail()        { return email; }
    public String getPhone()        { return phone; }
    public String getDob()          { return dob; }
    public String getRole()         { return role; }
    public String getPosition()     { return position; }
    public String getDepartment()   { return department; }
    public Timestamp getRegisteredAt(){ return registeredAt; }

    /** helper to show “First Last” */
    public String getFullName() {
        return (firstName != null ? firstName : "")
                + " "
                + (lastName  != null ? lastName  : "");
    }
}
