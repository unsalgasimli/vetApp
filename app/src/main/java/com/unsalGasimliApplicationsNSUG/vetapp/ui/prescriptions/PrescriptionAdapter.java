package com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Prescription;
import java.text.SimpleDateFormat;
import java.util.*;

public class PrescriptionAdapter
        extends RecyclerView.Adapter<PrescriptionAdapter.VH> {

    public interface OnClick { void onClick(Prescription p); }

    private final List<Prescription> items = new ArrayList<>();
    private final OnClick listener;
    private final SimpleDateFormat fmt =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public PrescriptionAdapter(OnClick l) {
        this.listener = l;
    }

    public void setItems(List<Prescription> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new VH(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_prescription,p,false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Prescription p = items.get(position);
        holder.txtMeds.setText(p.getName());
        holder.txtDose.setText(p.getFrequency());
        holder.itemView.setOnClickListener(v -> listener.onClick(p));
        // 1) Try your Timestamp first
        String displayDate;
        com.google.firebase.Timestamp ts = p.getDateTimestamp();
        if (ts != null) {
            // ts.toDate() returns a java.util.Date
            Date date = ts.toDate();
            // format however you like
            displayDate = android.text.format.DateFormat.format(
                    "yyyy-MM-dd HH:mm", date).toString();
        } else {
            // 2) Fallback to your string fields
            String start = p.getStartDate() != null ? p.getStartDate() : "";
            String end   = p.getEndDate()   != null ? p.getEndDate()   : "";
            displayDate = start + (end.isEmpty() ? "" : " – " + end);
        }
        holder.txtDate.setText(displayDate);
    }
    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtMeds, txtDose, txtDate;
        VH(@NonNull View v) {
            super(v);
            txtMeds = v.findViewById(R.id.txtMedication);
            txtDose = v.findViewById(R.id.txtDosage);
            txtDate = v.findViewById(R.id.txtPrescDate);
        }
    }
}
