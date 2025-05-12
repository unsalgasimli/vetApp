package com.unsalGasimliApplicationsNSUG.vetapp.ui.staff;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Staff;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentManageStaffBinding;

public class ManageStaffFragment extends Fragment {
    private FragmentManageStaffBinding binding;
    private ManageStaffViewModel       vm;
    private StaffAdapter               adapter;
    private String                     selectedId;

    public ManageStaffFragment() {
        super(R.layout.fragment_manage_staff);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageStaffBinding.bind(view);
        vm      = new ViewModelProvider(this).get(ManageStaffViewModel.class);

        adapter = new StaffAdapter(s -> {
            selectedId = s.getUniqueId();
            binding.etStaffFirst .setText(s.getFirstName());
            binding.etStaffLast  .setText(s.getLastName());
            binding.etStaffEmail .setText(s.getEmail());
            binding.etStaffPassword.setText("");
            binding.etStaffPhone .setText(s.getPhone());
            binding.etStaffPosition .setText(s.getPosition());
            binding.etStaffDept  .setText(s.getDepartment());
            binding.btnStaffAction.setText(R.string.action_update_staff);
            binding.btnStaffDelete.setVisibility(View.VISIBLE);
        });
        binding.rvStaff.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvStaff.setAdapter(adapter);

        vm.getStaffList().observe(getViewLifecycleOwner(), list -> {
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

        binding.btnStaffAction.setOnClickListener(v -> {
            Staff s = new Staff();
            s.setUniqueId(selectedId);
            s.setFirstName(binding.etStaffFirst.getText().toString().trim());
            s.setLastName(binding.etStaffLast.getText().toString().trim());
            s.setEmail(binding.etStaffEmail.getText().toString().trim());
            s.setPhone(binding.etStaffPhone.getText().toString().trim());
            s.setPosition(binding.etStaffPosition.getText().toString().trim());
            s.setDepartment(binding.etStaffDept.getText().toString().trim());
            if (selectedId == null) {
                vm.createStaff(s, binding.etStaffPassword.getText().toString().trim());
            } else {
                vm.updateStaff(s);
            }
        });
        binding.btnStaffDelete.setOnClickListener(v -> {
            if (selectedId != null) vm.deleteStaff(selectedId);
        });

        resetForm();
    }

    private void resetForm() {
        selectedId = null;
        binding.etStaffFirst   .getText().clear();
        binding.etStaffLast    .getText().clear();
        binding.etStaffEmail   .getText().clear();
        binding.etStaffPassword.getText().clear();
        binding.etStaffPhone   .getText().clear();
        binding.etStaffPosition.getText().clear();
        binding.etStaffDept    .getText().clear();

        binding.btnStaffAction.setText(R.string.action_add_staff);
        binding.btnStaffDelete.setVisibility(View.GONE);
    }

    private String getStringResource(String key, String arg) {
        int id = getResources().getIdentifier(key, "string", requireContext().getPackageName());
        return id == 0 ? key : getString(id, arg);
    }
}
