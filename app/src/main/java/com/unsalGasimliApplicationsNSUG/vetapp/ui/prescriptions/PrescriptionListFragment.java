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

import com.google.firebase.auth.FirebaseAuth;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Prescription;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.PrescriptionRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentPrescriptionsBinding;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionListFragment extends Fragment {
    private FragmentPrescriptionsBinding binding;
    private PrescriptionAdapter adapter;
    private final PrescriptionRepository repo = new PrescriptionRepository();
    private final List<Prescription> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPrescriptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        binding.recyclerPrescriptions.setLayoutManager(new LinearLayoutManager(requireContext()));
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
        binding.recyclerPrescriptions.setAdapter(adapter);


        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            repo.getFirestore()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String role = doc.getString("role");
                        if ("patient".equals(role)) {
                            binding.fabAddPrescription.setVisibility(View.GONE);
                        }
                    });
        }

        ItemTouchHelper.SimpleCallback swipeCallback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView rv,
                                          @NonNull RecyclerView.ViewHolder vh,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                        int pos = vh.getBindingAdapterPosition();
                        if (pos == RecyclerView.NO_POSITION) return;

                        Prescription toDelete = items.get(pos);
                        repo.deletePrescription(
                                toDelete.getPatientId(),
                                toDelete.getId(),
                                new PrescriptionRepository.Callback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        Toast.makeText(requireContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
                                        loadPrescriptions();
                                    }
                                    @Override
                                    public void onError(Throwable t) {
                                        Toast.makeText(
                                                requireContext(),
                                                getString(R.string.delete_failed, t.getMessage()),
                                                Toast.LENGTH_LONG
                                        ).show();
                                        adapter.notifyItemChanged(pos);
                                    }
                                });
                    }
                };
        new ItemTouchHelper(swipeCallback)
                .attachToRecyclerView(binding.recyclerPrescriptions);

        loadPrescriptions();
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
                Toast.makeText(requireContext(), getString(R.string.load_error, t.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
