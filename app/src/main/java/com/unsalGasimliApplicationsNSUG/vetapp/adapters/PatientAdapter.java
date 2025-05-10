// PatientAdapter.java
package com.unsalGasimliApplicationsNSUG.vetapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.models.Patient;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(Patient p);
    }

    private List<Patient> list;
    private OnItemClickListener listener;

    public PatientAdapter(List<Patient> list) {
        this.list = list;
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        listener = l;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Patient p = list.get(pos);
        h.name.setText(p.getFirstName() + " " + p.getLastName());
        h.email.setText(p.getEmail());
    }

    @Override public int getItemCount() { return list.size(); }

    public void setPatients(List<Patient> data) {
        list = data;
        notifyDataSetChanged();
    }

    class VH extends RecyclerView.ViewHolder {
        TextView name, email;
        VH(@NonNull View itemView) {
            super(itemView);
            name  = itemView.findViewById(R.id.textViewName);
            email = itemView.findViewById(R.id.textViewEmail);
            itemView.setOnClickListener(v -> {
                int i = getAdapterPosition();
                if (i!=RecyclerView.NO_POSITION && listener!=null) {
                    listener.onItemClick(list.get(i));
                }
            });
        }
    }
}
