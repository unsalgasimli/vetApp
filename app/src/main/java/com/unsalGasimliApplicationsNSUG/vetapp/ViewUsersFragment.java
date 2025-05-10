package com.unsalGasimliApplicationsNSUG.vetapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.adapters.UserAdapter;
import com.unsalGasimliApplicationsNSUG.vetapp.models.User;

import java.util.ArrayList;
import java.util.List;

public class ViewUsersFragment extends Fragment {
    private static final String ARG_ROLE = "role";

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private FirebaseFirestore db;
    private String roleFilter;

    public ViewUsersFragment() {
        // Required empty public constructor
    }

    /** Create a new instance filtered by role ("patient" or "staff") */
    public static ViewUsersFragment newInstance(String role) {
        ViewUsersFragment frag = new ViewUsersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROLE, role);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // pull the role from arguments (default to "patient" if none)
        roleFilter = "patient";
        if (getArguments() != null) {
            roleFilter = getArguments().getString(ARG_ROLE, "patient");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_users, container, false);

        recyclerView = view.findViewById(R.id.usersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadUsersByRole();

        return view;
    }

    private void loadUsersByRole() {
        db.collection("users")
                .whereEqualTo("role", roleFilter)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> users = new ArrayList<>();
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null) {
                            for (QueryDocumentSnapshot doc : snapshot) {
                                users.add(doc.toObject(User.class));
                            }
                        }
                        adapter.setUsers(users);
                    } else {
                        Log.e("ViewUsersFragment", "Error loading " + roleFilter, task.getException());
                    }
                });
    }
}
