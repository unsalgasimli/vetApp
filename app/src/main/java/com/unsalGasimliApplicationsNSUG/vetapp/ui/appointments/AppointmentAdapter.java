package com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import java.text.SimpleDateFormat;
import java.util.*;

public class AppointmentAdapter
        extends RecyclerView.Adapter<AppointmentAdapter.VH> {

    public interface OnClick { void onClick(Appointment a); }

    private final List<Appointment> items = new ArrayList<>();
    private final OnClick listener;

    public AppointmentAdapter(OnClick listener) {
        this.listener = listener;
    }


    public void setItems(List<Appointment> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }


    public List<Appointment> getItems() {
        return items;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Appointment a = items.get(pos);
        h.name.setText(a.getPatientName());

        String dt = "";
        if (a.getDateTime() != null) {
            dt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(a.getDateTime().toDate());
        }
        h.date.setText(dt);
        h.status.setText(a.getStatus());
        h.itemView.setOnClickListener(v -> listener.onClick(a));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name, date, status;
        VH(@NonNull View itemView) {
            super(itemView);
            name   = itemView.findViewById(R.id.textPatientName);
            date   = itemView.findViewById(R.id.textDateTime);
            status = itemView.findViewById(R.id.textStatus);
        }
    }
}
