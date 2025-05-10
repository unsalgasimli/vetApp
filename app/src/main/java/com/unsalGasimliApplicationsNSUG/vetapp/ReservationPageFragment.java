package com.unsalGasimliApplicationsNSUG.vetapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.unsalGasimliApplicationsNSUG.vetapp.data.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.data.Prescription;
import com.unsalGasimliApplicationsNSUG.vetapp.data.ReservationItem;
import com.unsalGasimliApplicationsNSUG.vetapp.data.ReservationType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ReservationPageFragment extends Fragment {

    // Filtering UI
    private Spinner spinnerFilter, spinnerPetFilter;
    private EditText etFromDate, etToDate;

    // Main list UI
    private RecyclerView recyclerReservations;
    private FloatingActionButton fabAddAppointment, fabAddPrescription;

    // Firestore and adapter
    private FirebaseFirestore db;
    private List<ReservationItem> allItems = new ArrayList<>();
    private ReservationAdapter adapter;

    // Filter variables
    private String filterType = "All";
    private String filterFromDate = "";
    private String filterToDate = "";
    private String filterPet = "All";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservation_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Initialize filtering UI components
        spinnerFilter    = view.findViewById(R.id.spinnerFilter);
        spinnerPetFilter = view.findViewById(R.id.spinnerPetFilter);
        etFromDate       = view.findViewById(R.id.etFromDate);
        etToDate         = view.findViewById(R.id.etToDate);

        // RecyclerView + Adapter
        recyclerReservations = view.findViewById(R.id.recyclerReservations);
        recyclerReservations.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReservationAdapter(new ArrayList<>(), (item, position) -> {
            if (item.getReservationType() == ReservationType.APPOINTMENT) {
                showAppointmentDialog((Appointment) item, position);
            } else {
                showPrescriptionDialog((Prescription) item, position);
            }
        });
        recyclerReservations.setAdapter(adapter);

        // ---- filter by type ----
        String[] filterOptions = {"All", "APPOINTMENT", "PRESCRIPTION"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, filterOptions);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(typeAdapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                filterType = filterOptions[pos];
                applyFilter();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        // ---- (optional) filter by pet ----
        // you can populate spinnerPetFilter similarly by querying "users/{uid}/pets"

        // ---- date pickers (you can wire up DatePickerDialogs to fill etFromDate & etToDate) ----
        // etFromDate.setOnClickListener(...) etc.

        // Fetch current user's reservations
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) return;
                        String fullName = doc.getString("firstName") + " " + doc.getString("lastName");
                        db.collection("reservations")
                                .whereEqualTo("patFullName", fullName)
                                .addSnapshotListener((qs, err) -> {
                                    if (err != null || qs == null) return;
                                    allItems.clear();
                                    for (DocumentSnapshot d : qs.getDocuments()) {
                                        String typeStr = d.getString("type");               // ← read "type"
                                        ReservationItem it = null;
                                        if ("APPOINTMENT".equals(typeStr)) {
                                            it = d.toObject(Appointment.class);
                                        } else if ("PRESCRIPTION".equals(typeStr)) {
                                            it = d.toObject(Prescription.class);
                                        }
                                        if (it != null) {
                                            it.setId(d.getId());
                                            allItems.add(it);
                                        }
                                    }
                                    // sort by timestamp
                                    Collections.sort(allItems, (a, b) ->
                                            a.getTimestamp().compareTo(b.getTimestamp()));
                                    applyFilter();
                                });
                    })
                    .addOnFailureListener(e -> Log.e("ReservationPage", "Failed to load user", e));
        }

        // FAB listeners
        fabAddAppointment   = view.findViewById(R.id.fabAddAppointment);
        fabAddPrescription  = view.findViewById(R.id.fabAddPrescription);
        fabAddAppointment.setOnClickListener(v -> showAppointmentDialog(null, -1));
        fabAddPrescription.setOnClickListener(v -> showPrescriptionDialog(null, -1));
    }

    private void applyFilter() {
        List<ReservationItem> filtered = new ArrayList<>();
        for (ReservationItem item : allItems) {
            // type filter
            if (!"All".equals(filterType)) {
                ReservationType rt = item.getReservationType();
                if ("APPOINTMENT".equals(filterType) && rt != ReservationType.APPOINTMENT) continue;
                if ("PRESCRIPTION".equals(filterType) && rt != ReservationType.PRESCRIPTION) continue;
            }
            // pet filter
            if (!"All".equals(filterPet)) {
                if (item.getPetName() == null || !item.getPetName().equals(filterPet)) continue;
            }
            // date filter
            String itemDate = DateFormat.format("yyyy-MM-dd",
                    item.getTimestamp().toDate()).toString();
            if (!filterFromDate.isEmpty() && itemDate.compareTo(filterFromDate) < 0) continue;
            if (!filterToDate.isEmpty()   && itemDate.compareTo(filterToDate)   > 0) continue;

            filtered.add(item);
        }
        adapter.updateData(filtered);
    }

    private void showAppointmentDialog(@Nullable final Appointment appointment, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_appointment, null);
        builder.setView(dialogView);

        // --- DATE & TIME SETUP (unchanged) ---
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

        // --- DOCTOR SPINNER (keep your existing code) ---
        Spinner spinnerDoctor = dialogView.findViewById(R.id.spinnerDoctor);
        // … your code to populate spinnerDoctor …

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

        // Fetch from Firestore
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users")
                .document(uid)
                .collection("pets")           // make sure your collection is really named "pets"
                .get()
                .addOnSuccessListener(qs -> {
                    petList.clear();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        String name = doc.getString("name");
                        if (name != null) petList.add(name);
                    }
                    if (petList.isEmpty()) {
                        petList.add("No pets found");
                    }
                    petAdapter.notifyDataSetChanged();

                    // If editing an existing appointment, select its pet:
                    if (appointment != null && appointment.getPetName() != null) {
                        int idx = petList.indexOf(appointment.getPetName());
                        if (idx >= 0) spinnerPet.setSelection(idx);
                    }
                    Log.d("Appointments", "Loaded pets: " + petList);
                })
                .addOnFailureListener(e -> {
                    Log.e("Appointments", "Error loading pets", e);
                    petList.clear();
                    petList.add("Error loading pets");
                    petAdapter.notifyDataSetChanged();
                });

        // --- APPOINTMENT TYPE SPINNER & INFO (unchanged) ---
        Spinner spinnerAppType = dialogView.findViewById(R.id.spinnerAppointmentType);
        // … your existing code …
        EditText etInfo = dialogView.findViewById(R.id.etInfo);

        if (appointment != null) {
            etTime.setText(appointment.getTime());
            etInfo.setText(appointment.getInfo());
            builder.setTitle("Update Appointment");
        } else {
            builder.setTitle("Add Appointment");
        }

        builder.setPositiveButton(appointment != null ? "Update" : "Add", (d, w) -> {
            // Gather selectedPet here:
            String selectedPet = (String) spinnerPet.getSelectedItem();
            // … then proceed to build/update your Appointment object …
        });

        // … rest of your dialog buttons (Cancel, Dismiss) …
        builder.setNegativeButton("Dismiss", (d, w) -> d.dismiss());
        builder.show();
    }

    private void showPrescriptionDialog(@Nullable Prescription prescription, int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dlg = getLayoutInflater().inflate(R.layout.dialog_prescription, null);
        builder.setView(dlg);

        String today = DateFormat.format("yyyy-MM-dd", new Date()).toString();
        ((TextView) dlg.findViewById(R.id.tvDate)).setText("From: " + today);

        EditText etDateTo   = dlg.findViewById(R.id.etDateTo);
        EditText etFreq     = dlg.findViewById(R.id.etFrequency);
        EditText etInfo     = dlg.findViewById(R.id.etInfo);

        if (prescription != null) {
            etDateTo.setText(prescription.getDateTo());
            etFreq.setText(prescription.getFrequency());
            etInfo.setText(prescription.getInfo());
            builder.setTitle("Update Prescription");
        } else {
            builder.setTitle("Add Prescription");
        }

        builder.setPositiveButton(prescription != null ? "Update" : "Add", (d, w) -> {
            // TODO: parse fields, build or update Prescription, set .setType("PRESCRIPTION"), and save
        });

        if (prescription != null) {
            builder.setNeutralButton("Cancel Prescription", (d, w) -> {
                prescription.setInfo("Cancelled");
                db.collection("reservations")
                        .document(prescription.getId())
                        .set(prescription);
            });
        }

        builder.setNegativeButton("Dismiss", (d, w) -> d.dismiss());
        builder.show();
    }
}
