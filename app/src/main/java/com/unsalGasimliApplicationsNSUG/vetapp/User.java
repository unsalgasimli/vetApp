package com.unsalGasimliApplicationsNSUG.vetapp;

public class User {
    private String uniqueId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String phone;
    private String dob; // Date of birth in format YYYY-MM-DD

    // No-argument constructor required for Firestore
    public User() {
    }

    public User(String uniqueId, String firstName, String lastName, String email, String role, String phone, String dob) {
        this.uniqueId = uniqueId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.phone = phone;
        this.dob = dob;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
