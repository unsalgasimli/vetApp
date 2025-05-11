
package com.unsalGasimliApplicationsNSUG.vetapp.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.Timestamp;

@IgnoreExtraProperties
public class Patient {
    @DocumentId
    private String uniqueId;





    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String dob;
    private String role;
    private Timestamp registeredAt;

    public Patient() { /* Firestore */ }

    public String getUniqueId()        { return uniqueId; }
    public void   setUniqueId(String id) { this.uniqueId = id; }

    public String getFirstName()  { return firstName; }
    public void   setFirstName(String fn) { this.firstName = fn; }

    public String getLastName()   { return lastName; }
    public void   setLastName(String ln)  { this.lastName = ln; }

    public String getEmail()      { return email; }
    public void   setEmail(String e)       { this.email = e; }

    public String getPhone()      { return phone; }
    public void   setPhone(String p)       { this.phone = p; }

    public String getDob()        { return dob; }
    public void   setDob(String d)         { this.dob = d; }

    public String getRole()       { return role; }
    public void   setRole(String r)        { this.role = r; }

    public Timestamp getRegisteredAt()     { return registeredAt; }
    public void      setRegisteredAt(Timestamp t) { this.registeredAt = t; }
}
