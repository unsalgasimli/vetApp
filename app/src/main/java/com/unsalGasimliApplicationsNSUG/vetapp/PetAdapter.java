package com.unsalGasimliApplicationsNSUG.vetapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private List<Pet> pets;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onPetClick(Pet pet, int position);
    }

    public PetAdapter(List<Pet> pets, OnItemClickListener listener) {
        this.pets = pets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet, parent, false);
        return new PetViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = pets.get(position);
        holder.bind(pet, position, listener);
    }

    @Override
    public int getItemCount() {
        return pets.size();
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName, tvSpecies, tvBreed, tvAge;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvSpecies = itemView.findViewById(R.id.tvSpecies);
            tvBreed = itemView.findViewById(R.id.tvBreed);
            tvAge = itemView.findViewById(R.id.tvAge);
        }

        public void bind(Pet pet, int position, OnItemClickListener listener) {
            tvName.setText("Name: " + pet.getName());
            tvSpecies.setText("Species: " + pet.getSpecies());
            tvBreed.setText("Breed: " + pet.getBreed());
            tvAge.setText("Age: " + pet.getAge());
            itemView.setOnClickListener(v -> listener.onPetClick(pet, position));
        }
    }
}
