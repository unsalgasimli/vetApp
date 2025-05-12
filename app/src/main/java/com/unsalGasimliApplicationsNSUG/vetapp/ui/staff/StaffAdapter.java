package com.unsalGasimliApplicationsNSUG.vetapp.ui.staff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Staff;

import java.util.ArrayList;
import java.util.List;


public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.VH> {
    public interface OnItemClickListener {
        void onItemClick(@NonNull Staff staff);
    }

    private final List<Staff> staff = new ArrayList<>();
    @Nullable private OnItemClickListener listener;

    public StaffAdapter(@Nullable List<Staff> initial) {
        if (initial != null) staff.addAll(initial);
    }

    public void setStaff(@NonNull List<Staff> newList) {
        staff.clear();
        staff.addAll(newList);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(@Nullable OnItemClickListener l) {
        listener = l;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        Staff s = staff.get(pos);
        holder.name.setText(s.getFirstName() + " " + s.getLastName());
        holder.email.setText(s.getEmail());
        holder.position.setText(s.getPosition());
        holder.department.setText(s.getDepartment());

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onItemClick(s));
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override public int getItemCount() {
        return staff.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView email;
        final TextView position;
        final TextView department;

        VH(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textViewName);
            email = itemView.findViewById(R.id.textViewEmail);
            position = itemView.findViewById(R.id.textViewPosition);
            department = itemView.findViewById(R.id.textViewDepartment);
        }
    }
}
