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

import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Prescription;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.PrescriptionRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentPrescriptionsBinding;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions.PrescriptionAdapter;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions.RequestPrescriptionFragment;

import java.util.ArrayList;
import java.util.List;

public class AllPrescriptionsFragment extends Fragment {
    private static final String TAG = "AllPrescriptionsFrag";

    private FragmentPrescriptionsBinding binding;
    private final List<Prescription> items = new ArrayList<>();
    private final PrescriptionRepository repo = new PrescriptionRepository();
    private PrescriptionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPrescriptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerPrescriptions.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new PrescriptionAdapter(p -> {
            Fragment edit = RequestPrescriptionFragment.newInstance(p.getPatientId(), p.getId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.staff_fragment_container, edit)
                    .addToBackStack(null)
                    .commit();
        });
        binding.recyclerPrescriptions.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView,
                                  @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder,
                                  @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                Prescription p = items.get(pos);
                repo.deletePrescription(p.getPatientId(), p.getId(), new PrescriptionRepository.Callback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        Toast.makeText(requireContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
                        loadData();
                    }

                    @Override
                    public void onError(Throwable t) {
                        Toast.makeText(requireContext(), getString(R.string.delete_failed, t.getMessage()), Toast.LENGTH_LONG).show();
                        adapter.notifyItemChanged(pos);
                    }
                });
            }
        }).attachToRecyclerView(binding.recyclerPrescriptions);

        binding.fabAddPrescription.setOnClickListener(v -> {
            Fragment add = RequestPrescriptionFragment.newInstance("", null);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.staff_fragment_container, add)
                    .addToBackStack(null)
                    .commit();
        });

        loadData();
    }

    private void loadData() {
        items.clear();
        repo.fetchAllPrescriptions(new PrescriptionRepository.Callback<List<Prescription>>() {
            @Override
            public void onSuccess(List<Prescription> data) {
                Log.d(TAG, "Loaded " + data.size() + " prescriptions");
                items.addAll(data);
                adapter.setItems(items);
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Error loading prescriptions", t);
                Toast.makeText(requireContext(), getString(R.string.error_loading_prescriptions, t.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
