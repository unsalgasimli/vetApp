package com.unsalGasimliApplicationsNSUG.vetapp.ui.staff;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Staff;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.ItemStaffBinding;

public class StaffAdapter
        extends ListAdapter<Staff, StaffAdapter.VH> {

    public interface OnItemClickListener {
        void onClick(Staff s);
    }

    private final OnItemClickListener listener;

    public StaffAdapter(OnItemClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Staff> DIFF =
            new DiffUtil.ItemCallback<Staff>() {
                @Override
                public boolean areItemsTheSame(@NonNull Staff a, @NonNull Staff b) {
                    return a.getUniqueId().equals(b.getUniqueId());
                }
                @SuppressLint("DiffUtilEquals")
                @Override
                public boolean areContentsTheSame(@NonNull Staff a, @NonNull Staff b) {
                    return a.equals(b);
                }
            };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        ItemStaffBinding b = ItemStaffBinding.inflate(
                LayoutInflater.from(p.getContext()), p, false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Staff s = getItem(pos);
        h.binding.textViewName     .setText(s.getFirstName() + " " + s.getLastName());
        h.binding.textViewEmail    .setText(s.getEmail());
        h.binding.textViewPosition .setText(s.getPosition());
        h.binding.textViewDepartment.setText(s.getDepartment());
        h.binding.getRoot().setOnClickListener(v -> listener.onClick(s));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemStaffBinding binding;
        VH(ItemStaffBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}
