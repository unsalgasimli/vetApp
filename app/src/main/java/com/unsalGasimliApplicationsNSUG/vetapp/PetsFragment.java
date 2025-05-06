package com.unsalGasimliApplicationsNSUG.vetapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import androidx.recyclerview.widget.ItemTouchHelper;
import java.util.List;


public class PetsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PetAdapter adapter;
    private List<Pet> petList;
    private FirebaseFirestore db;

    public PetsFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment (fragment_pets.xml)
        return inflater.inflate(R.layout.fragment_pets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewPets);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        petList = new ArrayList<>();
        adapter = new PetAdapter(petList, (pet, position) -> {
            // Open dialog for updating the selected pet
            showPetDialog(pet, position);
        });
        recyclerView.setAdapter(adapter);

        // Main FAB for adding a new pet (from fragment_pets.xml)
        FloatingActionButton fabMain = view.findViewById(R.id.fabMainPets);
        fabMain.setOnClickListener(v -> {
            // Passing null indicates adding a new pet
            showPetDialog(null, -1);
        });

        // Setup swipe-to-delete functionality using ItemTouchHelper
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                // We don't support moving items in this example.
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                Pet pet = petList.get(pos);
                // Delete the pet document from Firestore using its id.
                db.collection("pets").document(pet.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            // Firestore realtime listener will update the petList.
                        })
                        .addOnFailureListener(e -> {
                            // Optionally handle the error and notify the user.
                        });
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);

        // Listen for realtime updates from Firestore (collection "pets")
        db.collection("pets")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            // Handle the error if needed.
                            return;
                        }
                        if (querySnapshot != null) {
                            petList.clear();
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                Pet pet = doc.toObject(Pet.class);
                                if (pet != null) {
                                    pet.setId(doc.getId()); // Set the document ID manually.
                                    petList.add(pet);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    /**
     * Displays a dialog to add a new pet or update an existing one.
     *
     * @param pet The Pet object to update; if null, a new pet will be added.
     * @param position The position in the list (used when updating).
     */
    private void showPetDialog(@Nullable final Pet pet, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_pet, null);
        builder.setView(dialogView);

        // Get references to the input fields in the dialog.
        EditText etName = dialogView.findViewById(R.id.etPetName);
        EditText etSpecies = dialogView.findViewById(R.id.etSpecies);
        EditText etBreed = dialogView.findViewById(R.id.etBreed);
        EditText etAge = dialogView.findViewById(R.id.etAge);

        // If updating an existing pet, pre-populate the fields.
        if (pet != null) {
            etName.setText(pet.getName());
            etSpecies.setText(pet.getSpecies());
            etBreed.setText(pet.getBreed());
            etAge.setText(pet.getAge());
            builder.setTitle("Update Pet");
        } else {
            builder.setTitle("Add Pet");
        }

        builder.setPositiveButton(pet != null ? "Update" : "Add", (dialog, which) -> {
            // Retrieve input values.
            String name = etName.getText().toString().trim();
            String species = etSpecies.getText().toString().trim();
            String breed = etBreed.getText().toString().trim();
            String age = etAge.getText().toString().trim();

            // Get the current user.
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Log.e("showPetDialog", "No user is currently logged in.");
                return;
            }
            String uid = currentUser.getUid();

            if (pet == null) {
                // Create a new Pet object. The ID is null at this point because Firestore hasn't created the document yet.
                Pet newPet = new Pet(null, uid, name, species, breed, age);
                db.collection("pets")
                        .add(newPet)
                        .addOnSuccessListener(documentReference -> {
                            // Firestore assigns an ID after successful document creation.
                            newPet.setId(documentReference.getId());
                            Log.d("showPetDialog", "Pet added with ID: " + newPet.getId());
                            // Optionally refresh your pet list here.
                        })
                        .addOnFailureListener(e -> {
                            Log.e("showPetDialog", "Error adding pet", e);
                            // Optionally show a Toast message to the user.
                        });
            } else {
                // Update the existing pet's data.
                pet.setName(name);
                pet.setSpecies(species);
                pet.setBreed(breed);
                pet.setAge(age);
                db.collection("pets").document(pet.getId())
                        .set(pet)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("showPetDialog", "Pet updated successfully.");
                            // Optionally refresh your pet list here.
                        })
                        .addOnFailureListener(e -> {
                            Log.e("showPetDialog", "Error updating pet", e);
                            // Optionally show a Toast message to the user.
                        });
            }
        });

        builder.setNegativeButton("Dismiss", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

}
