// File: app/src/main/java/com/unsalGasimliApplicationsNSUG/vetapp/ui/appointments/AppointmentAdapter.java
package com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppointmentAdapter
        extends RecyclerView.Adapter<AppointmentAdapter.VH> {

    public interface OnClick { void onClick(Appointment a); }

    private final List<Appointment> items = new ArrayList<>();
    private final OnClick listener;

    public AppointmentAdapter(OnClick l) {
        this.listener = l;
    }

    public void setItems(List<Appointment> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        Appointment a = items.get(position);
        h.txtType.setText(a.getType());
        h.txtCounterparty.setText(a.getDoctorName());

        // Format the server timestamp into "yyyy-MM-dd HH:mm"
        Timestamp ts = a.getDateTime();
        if (ts != null) {
            Date date = ts.toDate();
            String formatted = DateFormat.format("yyyy-MM-dd HH:mm", date).toString();
            h.txtDate.setText(formatted);
        } else {
            h.txtDate.setText("");
        }

        h.itemView.setOnClickListener(v -> listener.onClick(a));
    }

    @Override public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtType, txtCounterparty, txtDate,txtStatus;
        VH(@NonNull View v) {
            super(v);
            txtType         = v.findViewById(R.id.txtType);
            txtCounterparty = v.findViewById(R.id.txtCounterparty);
            txtDate         = v.findViewById(R.id.txtDate);
            txtStatus       = v.findViewById(R.id.txtStatus);

        }
    }
}
