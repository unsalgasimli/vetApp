package com.unsalGasimliApplicationsNSUG.vetapp.ui.pets;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.unsalGasimliApplicationsNSUG.vetapp.data.Pet;
import com.unsalGasimliApplicationsNSUG.vetapp.data.PetRepository;

import java.util.List;

public class PetListViewModel extends ViewModel {
    private final PetRepository repo = new PetRepository();

    public LiveData<List<Pet>> getPets() {
        return repo.getAllPets();
    }

    public void addPet(Pet pet) {
        repo.addPet(pet);
    }

    public void updatePet(Pet pet) {
        repo.updatePet(pet);
    }

    public void deletePet(Pet pet) {
        if (pet.getId() != null) {
            repo.deletePet(pet.getId());
        }
    }
}
