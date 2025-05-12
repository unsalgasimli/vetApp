// File: app/src/main/java/com/unsalGasimliApplicationsNSUG/vetapp/ui/prescriptions/PrescriptionListFragment.java
package com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Prescription;


public class PrescriptionListFragment extends Fragment {
    private PrescriptionViewModel vm;
    private PrescriptionAdapter   adapter;

    public PrescriptionListFragment() {
        super(R.layout.fragment_prescriptions);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recycler = view.findViewById(R.id.recyclerPrescriptions);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PrescriptionAdapter(p -> {

            RequestPrescriptionFragment edit =
                    RequestPrescriptionFragment.newInstance(p.getPatientId(), p.getId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, edit)
                    .addToBackStack(null)
                    .commit();
        });
        recycler.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fabAddPrescription);
        if (fab != null) {
             fab.setVisibility(View.GONE);
        }



        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView rv,
                                            @NonNull RecyclerView.ViewHolder vh,
                                            @NonNull RecyclerView.ViewHolder t) {
                return false;
            }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getAdapterPosition();
                Prescription p = adapter.getCurrentList().get(pos);
                vm.deletePrescription(p.getPatientId(), p.getId());
            }
        }).attachToRecyclerView(recycler);


        vm = new ViewModelProvider(this).get(PrescriptionViewModel.class);


        vm.getPrescriptions().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
        });

        vm.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(),
                        getStringResource(msg), Toast.LENGTH_SHORT).show();
                vm.clearMessage();
            }
        });


        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            vm.loadForPatient(uid);
        } else {
            Toast.makeText(requireContext(),
                    R.string.not_logged_in, Toast.LENGTH_LONG).show();
        }
    }

    private String getStringResource(String key) {

        int id = getResources().getIdentifier(key, "string", requireContext().getPackageName());
        return id != 0 ? getString(id) : key;
    }
}
