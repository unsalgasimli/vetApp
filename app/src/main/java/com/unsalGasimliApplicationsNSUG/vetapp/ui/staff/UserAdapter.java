package com.unsalGasimliApplicationsNSUG.vetapp.ui.staff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.User;

import java.util.List;

/**
 * Adapter for displaying User items (patients) in a RecyclerView.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {
    public interface OnItemClickListener {
        void onItemClick(@NonNull User user);
    }

    private final List<User> users;
    private final OnItemClickListener listener;

    public UserAdapter(@NonNull List<User> users,
                       @NonNull OnItemClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        User u = users.get(position);
        holder.name.setText(u.getFirstName() + " " + u.getLastName());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(u));
    }

    @Override public int getItemCount() {
        return users.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name;
        VH(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
        }
    }
}
