// File: app/src/main/java/com/unsalGasimliApplicationsNSUG/vetapp/ui/patient/PatientAdapter.java
package com.unsalGasimliApplicationsNSUG.vetapp.ui.patient;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Patient;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.ItemPatientBinding;

public class PatientAdapter
        extends ListAdapter<Patient, PatientAdapter.VH> {

    public interface OnItemClickListener {
        void onClick(Patient p);
    }

    private final OnItemClickListener listener;

    public PatientAdapter(OnItemClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Patient> DIFF =
            new DiffUtil.ItemCallback<Patient>() {
                @Override
                public boolean areItemsTheSame(@NonNull Patient a, @NonNull Patient b) {
                    return a.getUniqueId().equals(b.getUniqueId());
                }
                @Override
                public boolean areContentsTheSame(@NonNull Patient a, @NonNull Patient b) {
                    return a.equals(b);
                }
            };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPatientBinding b = ItemPatientBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        Patient p = getItem(pos);
        holder.binding.tvName .setText(p.getFirstName() + " " + p.getLastName());
        holder.binding.tvEmail.setText(p.getEmail());
        holder.binding.getRoot().setOnClickListener(v -> listener.onClick(p));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemPatientBinding binding;
        VH(ItemPatientBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}
