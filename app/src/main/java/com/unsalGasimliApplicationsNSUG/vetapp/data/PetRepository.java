package com.unsalGasimliApplicationsNSUG.vetapp.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PetRepository {
    private final CollectionReference petsRef =
            FirebaseFirestore.getInstance().collection("pets");

    private final MutableLiveData<List<Pet>> petsLiveData = new MutableLiveData<>();

    public PetRepository() {
        // listen to all docs in "pets"
        petsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snap, FirebaseFirestoreException e) {
                if (e != null) return;
                List<Pet> list = new ArrayList<>();
                for (DocumentSnapshot doc : snap.getDocuments()) {
                    Pet pet = doc.toObject(Pet.class);
                    if (pet != null) {
                        pet.setId(doc.getId());
                        list.add(pet);
                    }
                }
                petsLiveData.postValue(list);
            }
        });
    }

    public LiveData<List<Pet>> getAllPets() {
        return petsLiveData;
    }

    public void addPet(Pet pet) {
        petsRef.add(pet);
    }

    public void updatePet(Pet pet) {
        petsRef.document(pet.getId()).set(pet);
    }

    public void deletePet(String petId) {
        petsRef.document(petId).delete();
    }
}
