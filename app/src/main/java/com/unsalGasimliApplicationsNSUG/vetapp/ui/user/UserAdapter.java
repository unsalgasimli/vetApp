package com.unsalGasimliApplicationsNSUG.vetapp.ui.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    private List<User> users;
    private OnItemClickListener listener;

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    /** Call to refresh data */
    public void setUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    /** Optional: let the fragment or activity listen to taps */
    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    @NonNull @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // simple two-line item; see item_user.xml below
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int pos) {
        User u = users.get(pos);
        holder.name.setText(u.getDisplayName());
        holder.email.setText(u.getEmail());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(u);
        });
    }

    @Override public int getItemCount() { return users == null ? 0 : users.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name  = itemView.findViewById(R.id.textViewName);
            email = itemView.findViewById(R.id.textViewEmail);
        }
    }
}
