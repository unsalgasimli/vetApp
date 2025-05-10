package com.unsalGasimliApplicationsNSUG.vetapp.models;

import com.google.firebase.firestore.DocumentId;

public class Staff {
    @DocumentId
    private String uniqueId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String position;
    private String department;

    public Staff() { /* needed for Firestore */ }

    public String getUniqueId() { return uniqueId; }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName;  }
    public String getEmail()     { return email;     }
    public String getPhone()     { return phone;     }
    public String getPosition()  { return position;  }
    public String getDepartment(){ return department;}

    public void setFirstName(String f){ firstName = f;}
    public void setLastName (String l){ lastName  = l;}
    public void setEmail    (String e){ email     = e;}
    public void setPhone    (String p){ phone     = p;}
    public void setPosition (String p){ position  = p;}
    public void setDepartment(String d){ department = d;}
}
