package com.unsalGasimliApplicationsNSUG.vetapp.ui.staff;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
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

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.AppointmentRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.databinding.FragmentAppointmentsBinding;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments.AppointmentAdapter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AllAppointmentsFragment extends Fragment {
    private static final String TAG = "AllAppointmentsFrag";

    private FragmentAppointmentsBinding binding;
    private final AppointmentRepository repo = new AppointmentRepository();
    private AppointmentAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAppointmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.fabAddAppointment.setVisibility(View.GONE);

        adapter = new AppointmentAdapter(this::showDecisionDialog);
        binding.recyclerAppointments.setAdapter(adapter);

        repo.fetchAllAppointments(new AppointmentRepository.Callback<List<Appointment>>() {
            @Override
            public void onSuccess(List<Appointment> data) {
                adapter.setItems(data);
            }
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Error loading appointments", t);
                Toast.makeText(requireContext(),
                        getString(R.string.error_loading_appointments, t.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDecisionDialog(Appointment appt) {
        Date ts = appt.getDateTime() != null ? appt.getDateTime().toDate() : new Date();
        String formatted = DateFormat.format("yyyy-MM-dd HH:mm", ts).toString();
        String msg = getString(R.string.appointment_request_message,
                appt.getPatientName(), formatted);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.appointment_request_title)
                .setMessage(msg)
                .setPositiveButton(R.string.accept, (d, w) -> updateStatus(appt, "accepted"))
                .setNegativeButton(R.string.reject, (d, w) -> updateStatus(appt, "rejected"))
                .setNeutralButton(R.string.reschedule, (d, w) -> showRescheduleDialogs(appt))
                .show();
    }

    private void showRescheduleDialogs(Appointment appt) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (dp, y, m, d) -> {
            int year = y, month = m, day = d;
            new TimePickerDialog(requireContext(), (tp, h, mm) -> {
                Calendar c2 = Calendar.getInstance();
                c2.set(year, month, day, h, mm);
                Timestamp newTs = new Timestamp(c2.getTime());
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(appt.getPatientId())
                        .collection("appointments")
                        .document(appt.getId())
                        .update(
                                "dateTime", newTs,
                                "status", "rescheduled"
                        )
                        .addOnSuccessListener(__ -> {
                            String newFmt = DateFormat.format("yyyy-MM-dd HH:mm", newTs.toDate()).toString();
                            Toast.makeText(requireContext(),
                                    getString(R.string.rescheduled_to, newFmt),
                                    Toast.LENGTH_SHORT).show();
                            refreshAppointments();
                        })
                        .addOnFailureListener(t ->
                                Toast.makeText(requireContext(),
                                        getString(R.string.reschedule_failed, t.getMessage()),
                                        Toast.LENGTH_LONG).show()
                        );
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateStatus(Appointment appt, String newStatus) {
        repo.updateStatus(appt.getPatientId(), appt.getId(), newStatus,
                new AppointmentRepository.Callback<Void>() {
                    @Override public void onSuccess(Void data) {
                        Toast.makeText(requireContext(),
                                getString(R.string.appointment_status_updated, newStatus),
                                Toast.LENGTH_SHORT).show();
                        refreshAppointments();
                    }
                    @Override public void onError(Throwable t) {
                        Toast.makeText(requireContext(),
                                getString(R.string.update_failed, t.getMessage()),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void refreshAppointments() {
        repo.fetchAllAppointments(new AppointmentRepository.Callback<List<Appointment>>() {
            @Override public void onSuccess(List<Appointment> data) {
                adapter.setItems(data);
            }
            @Override public void onError(Throwable t) {
                Log.e(TAG, "Error reloading appointments", t);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
