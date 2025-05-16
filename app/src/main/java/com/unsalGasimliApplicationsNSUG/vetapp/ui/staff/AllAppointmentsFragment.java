package com.unsalGasimliApplicationsNSUG.vetapp.ui.staff;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.Timestamp;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentAppointmentsBinding;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments.AppointmentAdapter;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments.AppointmentViewModel;

import java.util.Calendar;
import java.util.Date;

public class AllAppointmentsFragment extends Fragment {
    private FragmentAppointmentsBinding binding;
    private AppointmentViewModel vm;
    private AppointmentAdapter adapter;

    public AllAppointmentsFragment() {
        super(R.layout.fragment_appointments);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        binding = FragmentAppointmentsBinding.bind(view);
        vm = new ViewModelProvider(this).get(AppointmentViewModel.class);

        binding.fabAddAppointment.setVisibility(View.GONE);

        adapter = new AppointmentAdapter(this::showDecisionDialog);
        binding.recyclerAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAppointments.setAdapter(adapter);

        vm.getAppointments().observe(getViewLifecycleOwner(), list -> {
            adapter.setItems(list);
        });
        vm.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                String[] parts = msg.split(":",2);
                Toast.makeText(
                        requireContext(),
                        getStringResource(parts[0], parts.length>1?parts[1]:""),
                        Toast.LENGTH_SHORT
                ).show();
                vm.clearMessage();
            }
        });

        vm.loadAllAppointments();
    }

    private void showDecisionDialog(Appointment appt) {
        Date dt = appt.getDateTime().toDate();
        String when = DateFormat.format("yyyy-MM-dd HH:mm", dt).toString();
        String msg = "Patient: " + appt.getPatientName() + "\nDate: " + when;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.appointment_request_title)
                .setMessage(msg)
                .setPositiveButton(R.string.accept, (d,w)-> {
                    appt.setStatus("accepted");
                    vm.updateAppointment(appt);
                })
                .setNegativeButton(R.string.reject, (d,w)-> {
                    appt.setStatus("rejected");
                    vm.updateAppointment(appt);
                })
                .setNeutralButton(R.string.reschedule, (d,w)-> showRescheduleDialog(appt))
                .show();
    }

    private void showRescheduleDialog(Appointment appt) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (dp,y,m,d)-> {
                    new TimePickerDialog(requireContext(),
                            (tp,h,mm)-> {
                                Calendar c2 = Calendar.getInstance();
                                c2.set(y, m, d, h, mm);
                                appt.setDateTime(new Timestamp(c2.getTime()));
                                appt.setStatus("rescheduled");
                                vm.updateAppointment(appt);
                            },
                            c.get(Calendar.HOUR_OF_DAY),
                            c.get(Calendar.MINUTE),
                            true
                    ).show();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private String getStringResource(String key, String arg) {
        int id = getResources().getIdentifier(key, "string", requireContext().getPackageName());
        return id==0?key:getString(id, arg);
    }
}
