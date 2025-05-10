package com.unsalGasimliApplicationsNSUG.vetapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unsalGasimliApplicationsNSUG.vetapp.data.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.data.Prescription;
import com.unsalGasimliApplicationsNSUG.vetapp.data.ReservationItem;
import com.unsalGasimliApplicationsNSUG.vetapp.data.ReservationType;

import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_APPOINTMENT   = 0;
    public static final int VIEW_TYPE_PRESCRIPTION  = 1;

    private List<ReservationItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ReservationItem item, int position);
    }

    public ReservationAdapter(List<ReservationItem> items, OnItemClickListener listener) {
        this.items   = items;
        this.listener = listener;
    }

    public void updateData(List<ReservationItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ReservationItem item = items.get(position);
        return (item.getReservationType() == ReservationType.APPOINTMENT)
                ? VIEW_TYPE_APPOINTMENT
                : VIEW_TYPE_PRESCRIPTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_APPOINTMENT) {
            View view = inflater.inflate(R.layout.item_appointment, parent, false);
            return new AppointmentViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_prescription, parent, false);
            return new PrescriptionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position
    ) {
        ReservationItem item = items.get(position);

        if (holder instanceof AppointmentViewHolder
                && item.getReservationType() == ReservationType.APPOINTMENT) {
            ((AppointmentViewHolder) holder)
                    .bind((Appointment) item, position, listener);

        } else if (holder instanceof PrescriptionViewHolder
                && item.getReservationType() == ReservationType.PRESCRIPTION) {
            ((PrescriptionViewHolder) holder)
                    .bind((Prescription) item, position, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoc, tvPat, tvPet, tvDate, tvTime, tvType, tvInfo, tvStatus;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoc   = itemView.findViewById(R.id.tvDoc);
            tvPat   = itemView.findViewById(R.id.tvPat);
            tvPet   = itemView.findViewById(R.id.tvPet);
            tvDate  = itemView.findViewById(R.id.tvDate);
            tvTime  = itemView.findViewById(R.id.tvTime);
            tvType  = itemView.findViewById(R.id.tvAppointmentType);
            tvInfo  = itemView.findViewById(R.id.tvInfo);
            tvStatus= itemView.findViewById(R.id.tvStatus);
        }

        public void bind(
                Appointment appointment,
                int position,
                OnItemClickListener listener
        ) {
            tvDoc.setText("Doc: " + appointment.getDocFullName());
            tvPat.setText("Patient: " + appointment.getPatFullName());
            tvPet.setText("Pet: " + appointment.getPetName());
            tvDate.setText("Date: " + appointment.getDate());
            tvTime.setText("Time: " + appointment.getTime());
            tvType.setText("Type: " + appointment.getAppointmentCategory());
            tvInfo.setText("Info: " + appointment.getInfo());
            tvStatus.setText("Status: " + appointment.getStatus());

            itemView.setOnClickListener(v ->
                    listener.onItemClick(appointment, position)
            );
        }
    }

    static class PrescriptionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoc, tvPat, tvPet, tvDateFrom, tvDateTo, tvFreq, tvInfo;

        public PrescriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoc      = itemView.findViewById(R.id.tvDoc);
            tvPat      = itemView.findViewById(R.id.tvPat);
            tvPet      = itemView.findViewById(R.id.tvPet);
            tvDateFrom = itemView.findViewById(R.id.tvDateFrom);
            tvDateTo   = itemView.findViewById(R.id.tvDateTo);
            tvFreq     = itemView.findViewById(R.id.tvFrequency);
            tvInfo     = itemView.findViewById(R.id.tvInfo);
        }

        public void bind(
                Prescription prescription,
                int position,
                OnItemClickListener listener
        ) {
            tvDoc.setText("Doc: " + prescription.getDocFullName());
            tvPat.setText("Patient: " + prescription.getPatFullName());
            tvPet.setText("Pet: " + prescription.getPetName());
            tvDateFrom.setText("From: " + prescription.getDateFrom());
            tvDateTo.setText("To: " + prescription.getDateTo());
            tvFreq.setText("Freq: " + prescription.getFrequency());
            tvInfo.setText("Info: " + prescription.getInfo());

            itemView.setOnClickListener(v ->
                    listener.onItemClick(prescription, position)
            );
        }
    }
}
