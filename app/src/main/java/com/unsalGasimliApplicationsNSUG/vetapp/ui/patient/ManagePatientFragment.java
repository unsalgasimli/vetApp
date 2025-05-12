// File: app/src/main/java/com/unsalGasimliApplicationsNSUG/vetapp/ui/patient/ManagePatientFragment.java
package com.unsalGasimliApplicationsNSUG.vetapp.ui.patient;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Patient;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentManagePatientBinding;

public class ManagePatientFragment extends Fragment {
    private FragmentManagePatientBinding binding;
    private ManagePatientViewModel      vm;
    private PatientAdapter              adapter;
    private String                      selectedId;

    public ManagePatientFragment() {
        super(R.layout.fragment_manage_patient);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagePatientBinding.bind(view);
        vm      = new ViewModelProvider(this).get(ManagePatientViewModel.class);

        adapter = new PatientAdapter(p -> {
            selectedId = p.getUniqueId();
            binding.etFirstName .setText(p.getFirstName());
            binding.etLastName  .setText(p.getLastName());
            binding.etEmail     .setText(p.getEmail());
            binding.etPassword  .setText("");
            binding.etPhone     .setText(p.getPhone());
            binding.etDOB       .setText(p.getDob());
            binding.btnAction   .setText(R.string.action_update_patient);
            binding.btnDeletePatient   .setVisibility(View.VISIBLE);
        });
        binding.rvPatients.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPatients.setAdapter(adapter);

        vm.getPatients().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
        });
        vm.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                String[] parts = msg.split(":", 2);
                String key     = parts[0];
                String arg     = parts.length>1 ? parts[1] : "";
                Toast.makeText(
                        requireContext(),
                        getStringResource(key, arg),
                        Toast.LENGTH_SHORT
                ).show();
                vm.clearMessage();
            }
        });



        binding.btnAction.setOnClickListener(v -> {
            Patient p = new Patient();
            p.setUniqueId(selectedId);
            p.setFirstName(binding.etFirstName.getText().toString().trim());
            p.setLastName(binding.etLastName.getText().toString().trim());
            p.setEmail(binding.etEmail.getText().toString().trim());
            p.setPhone(binding.etPhone.getText().toString().trim());
            p.setDob(binding.etDOB.getText().toString().trim());
            if (selectedId == null) {
                vm.createPatient(p, binding.etPassword.getText().toString().trim());
            } else {
                vm.updatePatient(p);
            }
        });
        binding.btnDeletePatient.setOnClickListener(v -> {
            if (selectedId != null) vm.deletePatient(selectedId);
        });

        resetForm();
    }

    private void resetForm() {
        selectedId = null;
        binding.etFirstName .getText().clear();
        binding.etLastName  .getText().clear();
        binding.etEmail     .getText().clear();
        binding.etPassword  .getText().clear();
        binding.etPhone     .getText().clear();
        binding.etDOB       .getText().clear();
        binding.btnAction   .setText(R.string.action_add_patient);
        binding.btnDeletePatient   .setVisibility(View.GONE);
    }

    private String getStringResource(String key, String arg) {
        int id = getResources().getIdentifier(key, "string", requireContext().getPackageName());
        return id==0 ? key : getString(id, arg);
    }
}
