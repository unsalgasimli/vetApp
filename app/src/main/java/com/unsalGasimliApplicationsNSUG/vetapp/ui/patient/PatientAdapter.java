package com.unsalGasimliApplicationsNSUG.vetapp.ui.patient;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.VH> {
    public interface OnItemClickListener {
        void onItemClick(@NonNull Patient patient);
    }

    private final List<Patient> patients = new ArrayList<>();
    @Nullable
    private OnItemClickListener listener;

    public PatientAdapter(@Nullable List<Patient> initial) {
        if (initial != null) patients.addAll(initial);
    }

    public void setPatients(@NonNull List<Patient> newList) {
        patients.clear();
        patients.addAll(newList);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(@Nullable OnItemClickListener l) {
        listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        Patient p = patients.get(pos);

        holder.name.setText(p.getFirstName() + " " + p.getLastName());
        holder.email.setText(p.getEmail());
        holder.phone.setText(p.getPhone());
        holder.dob.setText(p.getDob());


        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onItemClick(p));
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView email;
        final TextView phone;
        final TextView dob;

        VH(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
            email = itemView.findViewById(R.id.tvEmail);
            phone = itemView.findViewById(R.id.tvPhone);
            dob = itemView.findViewById(R.id.tvDob);
        }
    }
}
