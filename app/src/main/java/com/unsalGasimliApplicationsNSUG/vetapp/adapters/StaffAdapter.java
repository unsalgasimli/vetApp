package com.unsalGasimliApplicationsNSUG.vetapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.models.Staff;
import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Staff staff);
    }

    private List<Staff> staffList;
    private OnItemClickListener listener;

    public StaffAdapter(List<Staff> staffList) {
        this.staffList = staffList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setStaff(List<Staff> newList) {
        this.staffList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int pos) {
        Staff s = staffList.get(pos);
        holder.name      .setText(s.getFirstName() + " " + s.getLastName());
        holder.email     .setText(s.getEmail());
        holder.position  .setText(s.getPosition());
        holder.department.setText(s.getDepartment());
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, position, department;

        StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            name       = itemView.findViewById(R.id.textViewName);
            email      = itemView.findViewById(R.id.textViewEmail);
            position   = itemView.findViewById(R.id.textViewPosition);
            department = itemView.findViewById(R.id.textViewDepartment);

            itemView.setOnClickListener(v -> {
                int idx = getAdapterPosition();
                if (listener != null && idx != RecyclerView.NO_POSITION) {
                    listener.onItemClick(staffList.get(idx));
                }
            });
        }
    }
}
