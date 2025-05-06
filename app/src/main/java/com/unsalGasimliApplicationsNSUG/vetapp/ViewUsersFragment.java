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
import java.util.ArrayList;
import java.util.List;

public class ViewUsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private FirebaseFirestore db;

    public ViewUsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_users, container, false);
        recyclerView = view.findViewById(R.id.usersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        // Query Firestore. Change collection name to "staff" or "patients" as needed.
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> users = new ArrayList<>();
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null) {
                            for (QueryDocumentSnapshot document : snapshot) {
                                User user = document.toObject(User.class);
                                users.add(user);
                            }
                        }
                        adapter.setUsers(users);
                    } else {
                        Log.e("ViewUsersFragment", "Error getting documents: ", task.getException());
                    }
                });
        return view;
    }
}
