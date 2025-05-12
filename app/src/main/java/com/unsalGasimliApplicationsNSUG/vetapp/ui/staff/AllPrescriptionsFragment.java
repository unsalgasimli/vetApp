// File: app/src/main/java/com/unsalGasimliApplicationsNSUG/vetapp/ui/staff/AllPrescriptionsFragment.java
package com.unsalGasimliApplicationsNSUG.vetapp.ui.staff;

import android.os.Bundle;
import android.util.Log;
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
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.PrescriptionRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Prescription;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions.PrescriptionAdapter;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions.RequestPrescriptionFragment;

import java.util.ArrayList;
import java.util.List;

public class AllPrescriptionsFragment extends Fragment {
    private static final String TAG = "AllPrescriptionsFrag";

    private RecyclerView recycler;
    private PrescriptionAdapter adapter;
    private final List<Prescription> items = new ArrayList<>();
    private FloatingActionButton fabAddPrescription;
    private final PrescriptionRepository repo = new PrescriptionRepository();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prescriptions, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recycler = view.findViewById(R.id.recyclerPrescriptions);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        // THIS MUST MATCH your XML: if your FAB id is "fabAdd", use R.id.fabAdd here.
        fabAddPrescription = view.findViewById(R.id.fabAddPrescription);

        // 1) Adapter with click-to-edit
        adapter = new PrescriptionAdapter(p -> {
            Fragment edit = RequestPrescriptionFragment.newInstance(p.getPatientId(), p.getId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.staff_fragment_container, edit)
                    .addToBackStack(null)
                    .commit();
        });
        recycler.setAdapter(adapter);

        // 2) Swipe-to-delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                /* dragDirs */ 0,
                /* swipeDirs */ ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                // we don't support drag & drop in this list
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getAdapterPosition();
                Prescription p = items.get(pos);
                repo.deletePrescription(p.getPatientId(), p.getId(), new PrescriptionRepository.Callback<Void>() {
                    @Override public void onSuccess(Void data) {
                        Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        loadData();
                    }
                    @Override public void onError(Throwable t) {
                        Toast.makeText(requireContext(),
                                "Delete failed: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                        adapter.notifyItemChanged(pos);
                    }
                });
            }
        }).attachToRecyclerView(recycler);

        // 3) FAB to add new
        fabAddPrescription.setOnClickListener(v -> {
            // pass empty patientId if doctor will choose inside the fragment
            Fragment add = RequestPrescriptionFragment.newInstance("", null);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.staff_fragment_container, add)
                    .addToBackStack(null)
                    .commit();
        });

        // 4) initial load
        loadData();
    }

    private void loadData() {
        items.clear();
        repo.fetchAllPrescriptions(new PrescriptionRepository.Callback<List<Prescription>>() {
            @Override public void onSuccess(List<Prescription> data) {
                Log.d(TAG, "Loaded " + data.size() + " prescriptions");
                items.addAll(data);
                adapter.setItems(items);
            }
            @Override public void onError(Throwable t) {
                Log.e(TAG, "Error loading prescriptions", t);
                Toast.makeText(requireContext(),
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
