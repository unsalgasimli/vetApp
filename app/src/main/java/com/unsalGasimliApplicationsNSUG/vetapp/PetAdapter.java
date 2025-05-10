// File: app/src/main/java/com/unsalGasimliApplicationsNSUG/vetapp/PetAdapter.java

package com.unsalGasimliApplicationsNSUG.vetapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unsalGasimliApplicationsNSUG.vetapp.data.Pet;

import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    public interface OnItemClickListener {
        void onPetClick(Pet pet, int position);
    }

    private List<Pet> pets;
    private final OnItemClickListener listener;

    public PetAdapter(List<Pet> pets, OnItemClickListener listener) {
        this.pets = pets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pet, parent, false);
        return new PetViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = pets.get(position);
        holder.bind(pet, position, listener);
    }

    @Override
    public int getItemCount() {
        return pets != null ? pets.size() : 0;
    }

    /** Replace the current list and refresh the RecyclerView */
    public void setPetList(List<Pet> list) {
        this.pets = list;
        notifyDataSetChanged();
    }

    /** Return the Pet at the given position (for swipe/delete) */
    public Pet getPetAt(int position) {
        return pets.get(position);
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvSpecies;
        private final TextView tvBreed;
        private final TextView tvAge;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tvName);
            tvSpecies = itemView.findViewById(R.id.tvSpecies);
            tvBreed   = itemView.findViewById(R.id.tvBreed);
            tvAge     = itemView.findViewById(R.id.tvAge);
        }

        public void bind(Pet pet, int position, OnItemClickListener listener) {
            tvName   .setText("Name: "    + pet.getName());
            tvSpecies.setText("Species: " + pet.getSpecies());
            tvBreed  .setText("Breed: "   + pet.getBreed());
            tvAge    .setText("Age: "     + pet.getAge());

            itemView.setOnClickListener(v -> listener.onPetClick(pet, position));
        }
    }
}
