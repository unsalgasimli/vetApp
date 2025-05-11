// File: app/src/main/java/com/unsalGasimliApplicationsNSUG/vetapp/ui/staff/AllAppointmentsFragment.java
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.AppointmentRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments.AppointmentAdapter;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments.RequestAppointmentFragment;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions.RequestPrescriptionFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AllAppointmentsFragment extends Fragment {
    private static final String TAG = "AllAppointmentsFrag";

    private RecyclerView recycler;
    private AppointmentAdapter adapter;

    private FloatingActionButton fabAddAppointment;
    private final AppointmentRepository repo = new AppointmentRepository();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_appointments, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recycler = view.findViewById(R.id.recyclerAppointments);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        fabAddAppointment = view.findViewById(R.id.fabAddAppointment);
        fabAddAppointment.setVisibility(View.GONE);
        adapter = new AppointmentAdapter(this::showDecisionDialog);
        recycler.setAdapter(adapter);



        repo.fetchAllAppointments(new AppointmentRepository.Callback<List<Appointment>>() {
            @Override public void onSuccess(List<Appointment> data) {
                adapter.setItems(data);
            }
            @Override public void onError(Throwable t) {
                Log.e(TAG, "Error loading appointments", t);
                Toast.makeText(requireContext(),
                        "Error loading appointments: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDecisionDialog(Appointment appt) {
        Date ts = (appt.getDateTime() != null) ? appt.getDateTime().toDate() : new Date();
        String formatted = DateFormat.format("yyyy-MM-dd HH:mm", ts).toString();
        String msg = "Patient: " + appt.getPatientName() +
                "\nDate: " + formatted;

        new AlertDialog.Builder(requireContext())
                .setTitle("Appointment Request")
                .setMessage(msg)
                .setPositiveButton("Accept", (d,w) -> updateStatus(appt, "accepted"))
                .setNegativeButton("Reject", (d,w) -> updateStatus(appt, "rejected"))
                .setNeutralButton("Reschedule", (d,w) -> showRescheduleDialogs(appt))
                .show();
    }

    private void showRescheduleDialogs(Appointment appt) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (dp, y, m, d) -> {
            int year=y, month=m, day=d;
            new TimePickerDialog(requireContext(), (tp, h, mm) -> {
                // build new timestamp
                Calendar c2 = Calendar.getInstance();
                c2.set(year, month, day, h, mm);
                // Convert Java Date to Firestore Timestamp
                Date date = c2.getTime();
                Timestamp newTs = new Timestamp(date);
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
                                    "Rescheduled to " + newFmt,
                                    Toast.LENGTH_SHORT).show();
                            refreshAppointments();
                        })
                        .addOnFailureListener(t ->
                                Toast.makeText(requireContext(),
                                        "Reschedule failed: " + t.getMessage(),
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
                                "Appointment " + newStatus,
                                Toast.LENGTH_SHORT).show();
                        refreshAppointments();
                    }
                    @Override public void onError(Throwable t) {
                        Toast.makeText(requireContext(),
                                "Update failed: " + t.getMessage(),
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
}
