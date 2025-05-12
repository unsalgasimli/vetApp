package com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.*;
import androidx.recyclerview.widget.*;

import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Prescription;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.ItemPrescriptionBinding;

public class PrescriptionAdapter
        extends ListAdapter<Prescription, PrescriptionAdapter.VH> {

    public interface OnClick { void invoke(Prescription p); }
    private final OnClick listener;

    public PrescriptionAdapter(OnClick l) {
        super(new DiffUtil.ItemCallback<Prescription>() {
            @Override public boolean areItemsTheSame(@NonNull Prescription a, @NonNull Prescription b) {
                return a.getId().equals(b.getId());
            }
            @SuppressLint("DiffUtilEquals")
            @Override public boolean areContentsTheSame(@NonNull Prescription a, @NonNull Prescription b) {
                return a.equals(b);
            }
        });
        listener = l;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        return new VH(ItemPrescriptionBinding.inflate(
                LayoutInflater.from(p.getContext()), p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Prescription p = getItem(pos);
        h.binding.txtDosage.setText(p.getPatientName());
        h.binding.txtMedication.setText(p.getName());
        h.binding.getRoot().setOnClickListener(v -> listener.invoke(p));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemPrescriptionBinding binding;
        VH(ItemPrescriptionBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}
