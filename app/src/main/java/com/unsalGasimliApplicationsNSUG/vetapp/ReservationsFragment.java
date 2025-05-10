package com.unsalGasimliApplicationsNSUG.vetapp;

import  android.app.AlertDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.unsalGasimliApplicationsNSUG.vetapp.data.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.data.Prescription;
import com.unsalGasimliApplicationsNSUG.vetapp.data.ReservationItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ReservationsFragment extends Fragment {

    private RecyclerView recyclerReservations;
    private FloatingActionButton fabAddAppointment, fabAddPrescription;
    private FirebaseFirestore db;
    private List<ReservationItem> allReservations = new ArrayList<>();
    private ReservationAdapter adapter;
    private Spinner spinnerFilter;
    private String fullName = "";
    private String selectedFilter = "All";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        recyclerReservations = view.findViewById(R.id.recyclerReservations);
        recyclerReservations.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReservationAdapter(new ArrayList<>(), (item, position) -> {
            if (item instanceof Appointment) {
                showAppointmentDialog((Appointment) item, position);
            } else if (item instanceof Prescription) {
                showPrescriptionDialog((Prescription) item, position);
            }
        });
        recyclerReservations.setAdapter(adapter);

        spinnerFilter = view.findViewById(R.id.spinnerFilter);
        String[] filterOptions = {"All", "Appointments", "Prescriptions"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, filterOptions);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);
        spinnerFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int pos, long id) {
                selectedFilter = filterOptions[pos];
                applyFilter();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        loadReservations();

        fabAddAppointment = view.findViewById(R.id.fabAddAppointment);
        fabAddPrescription = view.findViewById(R.id.fabAddPrescription);
        fabAddAppointment.setOnClickListener(v -> showAppointmentDialog(null, -1));
        fabAddPrescription.setOnClickListener(v -> showPrescriptionDialog(null, -1));
    }

    private void loadReservations() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    String firstName = doc.getString("firstName");
                    String lastName  = doc.getString("lastName");
                    fullName = firstName + " " + lastName;
                    Log.d("GetFullName", "User's full name: " + fullName);

                    db.collection("reservations")
                            .whereEqualTo("patFullName", fullName)
                            .addSnapshotListener((qs, err) -> {
                                if (err != null || qs == null) return;
                                List<ReservationItem> items = new ArrayList<>();
                                for (DocumentSnapshot d : qs.getDocuments()) {
                                    String typeStr = d.getString("type"); // ← must match .setType()
                                    ReservationItem it = null;
                                    if ("APPOINTMENT".equals(typeStr)) {
                                        it = d.toObject(Appointment.class);
                                    } else if ("PRESCRIPTION".equals(typeStr)) {
                                        it = d.toObject(Prescription.class);
                                    }
                                    if (it != null) {
                                        it.setId(d.getId());
                                        items.add(it);
                                    }
                                }
                                Collections.sort(items, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
                                allReservations = items;
                                applyFilter();
                            });
                });
    }

    private void applyFilter() {
        List<ReservationItem> filtered = new ArrayList<>();
        for (ReservationItem it : allReservations) {
            if ("Appointments".equals(selectedFilter) && !(it instanceof Appointment)) continue;
            if ("Prescriptions".equals(selectedFilter) && !(it instanceof Prescription)) continue;
            filtered.add(it);
        }
        adapter.updateData(filtered);
    }

    private void showAppointmentDialog(@Nullable final Appointment appointment, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_appointment, null);
        builder.setView(dialogView);

        String currentDate = DateFormat.format("yyyy-MM-dd", new Date()).toString();
        ((TextView) dialogView.findViewById(R.id.tvDate)).setText("Date: " + currentDate);

        EditText etTime = dialogView.findViewById(R.id.etTime);
        etTime.setFocusable(false);
        etTime.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTitleText("Select Appointment Time")
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .build();
            picker.addOnPositiveButtonClickListener(p -> {
                etTime.setText(String.format("%02d:%02d", picker.getHour(), picker.getMinute()));
            });
            picker.show(getChildFragmentManager(), "TIME_PICKER");
        });

        // Doctor spinner
        Spinner spinnerDoctor = dialogView.findViewById(R.id.spinnerDoctor);
        List<String> staffList = new ArrayList<>();
        ArrayAdapter<String> staffAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, staffList);
        staffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoctor.setAdapter(staffAdapter);
        db.collection("users").whereEqualTo("role", "staff").get()
                .addOnSuccessListener(qs -> {
                    staffList.clear();
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        String fn = d.getString("firstName"), ln = d.getString("lastName");
                        if (fn != null && ln != null) staffList.add(fn + " " + ln);
                    }
                    staffAdapter.notifyDataSetChanged();
                    if (appointment != null && appointment.getDocFullName() != null) {
                        int idx = staffList.indexOf(appointment.getDocFullName());
                        if (idx >= 0) spinnerDoctor.setSelection(idx);
                    }
                });

        // --- PET SPINNER ---
        Spinner spinnerPet = dialogView.findViewById(R.id.spinnerPet);
        List<String> petList = new ArrayList<>();
        petList.add("Loading pets…");
        ArrayAdapter<String> petAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                petList
        );
        petAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPet.setAdapter(petAdapter);

// Query the top-level "pets" collection by ownerId
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("pets")
                .whereEqualTo("ownerId", uid)
                .get()
                .addOnSuccessListener(qs -> {
                    petList.clear();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        String name = doc.getString("name");
                        if (name != null) petList.add(name);
                    }
                    if (petList.isEmpty()) petList.add("No pets found");
                    petAdapter.notifyDataSetChanged();

                    // if we're editing, pre-select the existing pet name
                    if (appointment != null && appointment.getPetName() != null) {
                        int idx = petList.indexOf(appointment.getPetName());
                        if (idx >= 0) spinnerPet.setSelection(idx);
                    }
                })
                .addOnFailureListener(e -> {
                    petList.clear();
                    petList.add("Error loading pets");
                    petAdapter.notifyDataSetChanged();
                });

        // Appointment type spinner
        Spinner spinnerAppType = dialogView.findViewById(R.id.spinnerAppointmentType);
        String[] appTypes = {"Consultation","Vaccination","Surgery","Follow-up",
                "Dental Cleaning","Emergency","Routine Check-up","Diagnostic Test"};
        ArrayAdapter<String> appTypeAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, appTypes);
        appTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAppType.setAdapter(appTypeAdapter);

        EditText etInfo = dialogView.findViewById(R.id.etInfo);

        if (appointment != null) {
            etTime.setText(appointment.getTime());
            etInfo.setText(appointment.getInfo());
            for (int i = 0; i < appTypes.length; i++) {
                if (appTypes[i].equals(appointment.getAppointmentCategory())) {
                    spinnerAppType.setSelection(i);
                    break;
                }
            }
            builder.setTitle("Update Appointment");
        } else {
            builder.setTitle("Add Appointment");
        }

        builder.setPositiveButton(appointment != null ? "Update" : "Add", (dlg, which) -> {
            String time = etTime.getText().toString().trim();
            String appType = spinnerAppType.getSelectedItem().toString();
            String info = etInfo.getText().toString().trim();
            String status = "Requested";
            String selectedDoc = (String) spinnerDoctor.getSelectedItem();
            String selectedPet = (String) spinnerPet.getSelectedItem();

            try {
                String[] tparts = time.split(":");
                String[] dparts = currentDate.split("-");
                Calendar cal = Calendar.getInstance();
                cal.set(Integer.parseInt(dparts[0]),
                        Integer.parseInt(dparts[1]) - 1,
                        Integer.parseInt(dparts[2]),
                        Integer.parseInt(tparts[0]),
                        Integer.parseInt(tparts[1]));
                Timestamp ts = new Timestamp(cal.getTime());

                if (appointment == null) {
                    Appointment newApp = new Appointment();
                    newApp.setDocFullName(selectedDoc);
                    newApp.setPatFullName(fullName);
                    newApp.setPetName(selectedPet);
                    newApp.setDate(currentDate);
                    newApp.setTime(time);
                    newApp.setAppointmentCategory(appType);
                    newApp.setInfo(info);
                    newApp.setStatus(status);
                    newApp.setTimestamp(ts);
                    newApp.setType("APPOINTMENT");
                    db.collection("reservations")
                            .add(newApp)
                            .addOnSuccessListener(ref -> newApp.setId(ref.getId()));
                } else {
                    appointment.setDocFullName(selectedDoc);
                    appointment.setPetName(selectedPet);
                    appointment.setDate(currentDate);
                    appointment.setTime(time);
                    appointment.setAppointmentCategory(appType);
                    appointment.setInfo(info);
                    appointment.setStatus(status);
                    appointment.setTimestamp(ts);
                    db.collection("reservations")
                            .document(appointment.getId())
                            .set(appointment);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        if (appointment != null) {
            builder.setNeutralButton("Cancel Appointment", (dlg, which) -> {
                appointment.setStatus("Cancelled");
                db.collection("reservations")
                        .document(appointment.getId())
                        .set(appointment);
            });
        }

        builder.setNegativeButton("Dismiss", (dlg, which) -> dlg.dismiss());
        builder.show();
    }

    private void showPrescriptionDialog(@Nullable final Prescription prescription, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_prescription, null);
        builder.setView(dialogView);

        String currentDate = DateFormat.format("yyyy-MM-dd", new Date()).toString();
        ((TextView) dialogView.findViewById(R.id.tvDate)).setText("From: " + currentDate);

        EditText etDateTo   = dialogView.findViewById(R.id.etDateTo);
        EditText etFrequency= dialogView.findViewById(R.id.etFrequency);
        EditText etInfo     = dialogView.findViewById(R.id.etInfo);

        if (prescription != null) {
            etDateTo.setText(prescription.getDateTo());
            etFrequency.setText(prescription.getFrequency());
            etInfo.setText(prescription.getInfo());
            builder.setTitle("Update Prescription");
        } else {
            builder.setTitle("Add Prescription");
        }

        builder.setPositiveButton(prescription != null ? "Update" : "Add", (dlg, which) -> {
            String dateTo   = etDateTo.getText().toString().trim();
            String freq     = etFrequency.getText().toString().trim();
            String info     = etInfo.getText().toString().trim();
            Timestamp ts    = new Timestamp(new Date());

            if (prescription == null) {
                Prescription newPre = new Prescription();
                newPre.setDocFullName("Dr. Smith");
                newPre.setPatFullName(fullName);
                newPre.setPetName("Max");
                newPre.setDateFrom(currentDate);
                newPre.setDateTo(dateTo);
                newPre.setFrequency(freq);
                newPre.setInfo(info);
                newPre.setTimestamp(ts);
                newPre.setType("PRESCRIPTION");
                db.collection("reservations")
                        .add(newPre)
                        .addOnSuccessListener(ref -> newPre.setId(ref.getId()));
            } else {
                prescription.setDateTo(dateTo);
                prescription.setFrequency(freq);
                prescription.setInfo(info);
                prescription.setTimestamp(ts);
                db.collection("reservations")
                        .document(prescription.getId())
                        .set(prescription);
            }
        });

        if (prescription != null) {
            builder.setNeutralButton("Cancel Prescription", (dlg, which) -> {
                prescription.setInfo("Cancelled");
                db.collection("reservations")
                        .document(prescription.getId())
                        .set(prescription);
            });
        }

        builder.setNegativeButton("Dismiss", (dlg, which) -> dlg.dismiss());
        builder.show();
    }
}
