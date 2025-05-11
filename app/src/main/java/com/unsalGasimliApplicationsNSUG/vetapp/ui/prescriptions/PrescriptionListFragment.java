package com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.PrescriptionRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Prescription;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.User;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions.RequestPrescriptionFragment;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionListFragment extends Fragment {
    private RecyclerView recycler;
    private PrescriptionAdapter adapter;
    private final PrescriptionRepository repo = new PrescriptionRepository();
    private final List<Prescription> items = new ArrayList<>();
    private FloatingActionButton fab;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prescriptions, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        recycler = v.findViewById(R.id.recyclerPrescriptions);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        fab = v.findViewById(R.id.fabAddPrescription);

        // Hide FAB if current user is a patient
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String role = doc.getString("role");
                        if ("patient".equals(role)) {
                            fab.setVisibility(View.GONE);
                        }
                    });
        }

        // 1) Set up adapter with click-to-edit
        adapter = new PrescriptionAdapter(p -> {
            RequestPrescriptionFragment editFrag = RequestPrescriptionFragment.newInstance(p.getPatientId());
            Bundle args = editFrag.getArguments();
            if (args != null) {
                args.putString("ARG_PRESCRIPTION_ID", p.getId());
            }
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, editFrag)
                    .addToBackStack(null)
                    .commit();
        });
        recycler.setAdapter(adapter);

        // 2) Load prescriptions
        loadPrescriptions();

        // 3) Attach swipe-to-delete
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView rv,
                                            @NonNull RecyclerView.ViewHolder vh,
                                            @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getAdapterPosition();
                Prescription toDelete = items.get(pos);
                repo.deletePrescription(toDelete.getPatientId(), toDelete.getId(), new PrescriptionRepository.Callback<Void>() {
                    @Override public void onSuccess(Void data) {
                        Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        loadPrescriptions();
                    }
                    @Override public void onError(Throwable t) {
                        Toast.makeText(requireContext(), "Delete failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        adapter.notifyItemChanged(pos);
                    }
                });
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recycler);
    }

    private void loadPrescriptions() {
        items.clear();
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        repo.fetchForPatient(uid, new PrescriptionRepository.Callback<List<Prescription>>() {
            @Override public void onSuccess(List<Prescription> data) {
                items.addAll(data);
                adapter.setItems(items);
            }
            @Override public void onError(Throwable t) {
                Toast.makeText(requireContext(), "Load error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
