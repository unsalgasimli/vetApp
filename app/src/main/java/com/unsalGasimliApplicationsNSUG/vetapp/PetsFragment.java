package com.unsalGasimliApplicationsNSUG.vetapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unsalGasimliApplicationsNSUG.vetapp.data.Pet;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.pets.PetListViewModel;

import java.util.ArrayList;

public class PetsFragment extends Fragment {

    private PetListViewModel viewModel;
    private PetAdapter adapter;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Firestore & ViewModel setup ---
        db = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(this)
                .get(PetListViewModel.class);

        // --- RecyclerView + Adapter ---
        RecyclerView rv = view.findViewById(R.id.recyclerViewPets);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PetAdapter(new ArrayList<>(), (pet, pos) -> {
            showPetDialog(pet);
        });
        rv.setAdapter(adapter);

        // --- Observe LiveData ---
        viewModel.getPets().observe(getViewLifecycleOwner(), pets -> {
            adapter.setPetList(pets);
        });

        // --- FAB to add a new pet ---
        FloatingActionButton fab = view.findViewById(R.id.fabMainPets);
        fab.setOnClickListener(v -> showPetDialog(null));

        // --- Swipe to delete ---
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override public boolean onMove(@NonNull RecyclerView rv,
                                            @NonNull RecyclerView.ViewHolder vh,
                                            @NonNull RecyclerView.ViewHolder tgt) {
                return false;
            }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh,
                                           int dir) {
                Pet p = adapter.getPetAt(vh.getAdapterPosition());
                viewModel.deletePet(p);
            }
        }).attachToRecyclerView(rv);
    }

    private void showPetDialog(@Nullable final Pet pet) {
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_pet, null);
        b.setView(dialogView);

        EditText etName    = dialogView.findViewById(R.id.etPetName);
        EditText etSpecies = dialogView.findViewById(R.id.etSpecies);
        EditText etBreed   = dialogView.findViewById(R.id.etBreed);
        EditText etAge     = dialogView.findViewById(R.id.etAge);

        // Title: “Add Pet” or “Update Pet”
        b.setTitle(pet != null
                ? getString(R.string.update_pet)
                : getString(R.string.add_pet));

        // Positive Button (uses CharSequence overload so it’s not ambiguous)
        b.setPositiveButton(
                pet != null
                        ? getString(R.string.update)
                        : getString(R.string.add),
                (DialogInterface dialog, int which) -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) return; // no-op if not logged in

                    String uid     = user.getUid();
                    String name    = etName   .getText().toString().trim();
                    String species = etSpecies.getText().toString().trim();
                    String breed   = etBreed  .getText().toString().trim();
                    String age     = etAge    .getText().toString().trim();

                    if (pet == null) {
                        Pet newPet = new Pet(null, uid, name, species, breed, age);
                        viewModel.addPet(newPet);
                    } else {
                        pet.setName   (name);
                        pet.setSpecies(species);
                        pet.setBreed  (breed);
                        pet.setAge    (age);
                        viewModel.updatePet(pet);
                    }
                }
        );

        // Negative Button
        b.setNegativeButton(
                getString(R.string.dismiss),
                (DialogInterface dialog, int which) -> dialog.dismiss()
        );

        b.show();
    }
}
