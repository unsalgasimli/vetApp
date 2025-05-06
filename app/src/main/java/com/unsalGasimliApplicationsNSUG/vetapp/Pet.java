package com.unsalGasimliApplicationsNSUG.vetapp;

public class Pet {
    private String id;
    private String ownerId;  // e.g., to link to the user who owns the pet
    private String name;
    private String species;  // e.g., Dog, Cat, etc.
    private String breed;
    private String age;      // can be a string like "2 years" or a number

    public Pet() {
        // Empty constructor needed for Firestore deserialization
    }

    public Pet(String id, String ownerId, String name, String species, String breed, String age) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
    }

    // Getters
    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getSpecies() { return species; }
    public String getBreed() { return breed; }
    public String getAge() { return age; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public void setName(String name) { this.name = name; }
    public void setSpecies(String species) { this.species = species; }
    public void setBreed(String breed) { this.breed = breed; }
    public void setAge(String age) { this.age = age; }
}
