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
    // This list holds all reservations fetched from Firebase.
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
        spinnerFilter = view.findViewById(R.id.spinnerFilter);
        spinnerPetFilter = view.findViewById(R.id.spinnerPetFilter);
        etFromDate = view.findViewById(R.id.etFromDate);
        etToDate = view.findViewById(R.id.etToDate);

        // Set up RecyclerView and adapter
        recyclerReservations = view.findViewById(R.id.recyclerReservations);
        recyclerReservations.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReservationAdapter(new ArrayList<>(), (ReservationItem item, int position) -> {
            // Use the enum from the base class to decide which dialog to show.
            if (item.getAppointmentType() == ReservationType.APPOINTMENT) {
                showAppointmentDialog((Appointment) item, position);
            } else if (item.getAppointmentType() == ReservationType.PRESCRIPTION) {
                showPrescriptionDialog((Prescription) item, position);
            }
        });
        recyclerReservations.setAdapter(adapter);

        // Set up spinnerFilter adapter with options "All", "APPOINTMENT", "PRESCRIPTION"
        String[] filterOptions = {"All", "APPOINTMENT", "PRESCRIPTION"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, filterOptions);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View spinnerView, int position, long id) {
                filterType = filterOptions[position];
                applyFilter();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // (Optional) Set up spinnerPetFilter if you wish to filter by pet.

        // Fetch reservations for the current user from Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference userDocRef = db.collection("users").document(uid);
            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String firstName = documentSnapshot.getString("firstName");
                    String lastName = documentSnapshot.getString("lastName");
                    String fullName = firstName + " " + lastName;
                    Log.d("GetFullName", "User's full name: " + fullName);

                    db.collection("reservations")
                            .whereEqualTo("patFullName", fullName)
                            .addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                                if (error != null || querySnapshot == null) return;
                                // Update the global list (avoid shadowing)
                                allItems = new ArrayList<>();
                                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                    String typeStr = doc.getString("reservationType");
                                    ReservationItem item = null;
                                    if ("APPOINTMENT".equals(typeStr)) {
                                        item = doc.toObject(Appointment.class);
                                    } else if ("PRESCRIPTION".equals(typeStr)) {
                                        item = doc.toObject(Prescription.class);
                                    }
                                    if (item != null) {
                                        item.setId(doc.getId());
                                        allItems.add(item);
                                    }
                                }
                                // Optionally sort by timestamp (nearest first)
                                Collections.sort(allItems, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
                                applyFilter();
                            });
                } else {
                    Log.d("GetFullName", "No user document found for UID: " + uid);
                }
            }).addOnFailureListener(e -> {
                Log.e("GetFullName", "Error fetching user document", e);
            });
        } else {
            Log.d("GetFullName", "No user is currently logged in.");
        }

        // Set up FAB listeners to add new appointments or prescriptions
        fabAddAppointment = view.findViewById(R.id.fabAddAppointment);
        fabAddPrescription = view.findViewById(R.id.fabAddPrescription);
        fabAddAppointment.setOnClickListener(v -> showAppointmentDialog(null, -1));
        fabAddPrescription.setOnClickListener(v -> showPrescriptionDialog(null, -1));
    }

    // Filter the list of reservations based on the selected filterType, pet, and date range
    private void applyFilter() {
        List<ReservationItem> filtered = new ArrayList<>();
        for (ReservationItem item : allItems) {
            // Filter by type using enum comparisons
            if (!"All".equals(filterType)) {
                if ("APPOINTMENT".equals(filterType) && item.getAppointmentType() != ReservationType.APPOINTMENT)
                    continue;
                if ("PRESCRIPTION".equals(filterType) && item.getAppointmentType() != ReservationType.PRESCRIPTION)
                    continue;
            }
            // Filter by pet (if implemented)
            if (!"All".equals(filterPet)) {
                if (item.getPetName() == null || !item.getPetName().equals(filterPet))
                    continue;
            }
            // Filter by date range: compare the item's timestamp (formatted as "yyyy-MM-dd")
            String itemDate = DateFormat.format("yyyy-MM-dd", item.getTimestamp().toDate()).toString();
            if (filterFromDate != null && !filterFromDate.isEmpty() && itemDate.compareTo(filterFromDate) < 0)
                continue;
            if (filterToDate != null && !filterToDate.isEmpty() && itemDate.compareTo(filterToDate) > 0)
                continue;
            filtered.add(item);
        }
        adapter.updateData(filtered);
    }

    // --- Dialog Methods ---
    // Appointment dialog (includes doctor spinner, appointment category spinner, and MaterialTimePicker)
    private void showAppointmentDialog(@Nullable final Appointment appointment, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_appointment, null);
        builder.setView(dialogView);

        String currentDate = DateFormat.format("yyyy-MM-dd", new Date()).toString();
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        tvDate.setText("Date: " + currentDate);

        // Time selection using MaterialTimePicker
        EditText etTime = dialogView.findViewById(R.id.etTime);
        etTime.setFocusable(false);
        etTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTitleText("Select Appointment Time")
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .build();
            timePicker.addOnPositiveButtonClickListener(picker -> {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String timeString = String.format("%02d:%02d", hour, minute);
                etTime.setText(timeString);
            });
            timePicker.show(getChildFragmentManager(), "TIME_PICKER");
        });

        // (Your code to set up doctor spinner and appointment category spinner goes here.)

        // Info EditText
        EditText etInfo = dialogView.findViewById(R.id.etInfo);

        if (appointment != null) {
            etTime.setText(appointment.getTime());
            etInfo.setText(appointment.getInfo());
            builder.setTitle("Update Appointment");
        } else {
            builder.setTitle("Add Appointment");
        }

        builder.setPositiveButton(appointment != null ? "Update" : "Add", (dialog, which) -> {
            // Parse time, create/update Appointment, and update Firebase
            // (Your existing appointment creation/update code goes here.)
        });

        if (appointment != null) {
            builder.setNeutralButton("Cancel Appointment", (dialog, which) -> {
                appointment.setStatus("Cancelled");
                db.collection("reservations").document(appointment.getId())
                        .set(appointment)
                        .addOnSuccessListener(aVoid -> { /* Appointment cancelled */ })
                        .addOnFailureListener(e -> { /* Handle error */ });
            });
        }

        builder.setNegativeButton("Dismiss", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Prescription dialog
    private void showPrescriptionDialog(@Nullable final Prescription prescription, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_prescription, null);
        builder.setView(dialogView);

        String currentDate = DateFormat.format("yyyy-MM-dd", new Date()).toString();
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        tvDate.setText("From: " + currentDate);

        // Set up dialog UI for prescription (date, frequency, info)
        EditText etDateTo = dialogView.findViewById(R.id.etDateTo);
        EditText etFrequency = dialogView.findViewById(R.id.etFrequency);
        EditText etInfo = dialogView.findViewById(R.id.etInfo);

        if (prescription != null) {
            etDateTo.setText(prescription.getDateTo());
            etFrequency.setText(prescription.getFrequency());
            etInfo.setText(prescription.getInfo());
            builder.setTitle("Update Prescription");
        } else {
            builder.setTitle("Add Prescription");
        }

        builder.setPositiveButton(prescription != null ? "Update" : "Add", (dialog, which) -> {
            // Parse input and create/update Prescription, then update Firebase
            // (Your existing prescription creation/update code goes here.)
        });

        if (prescription != null) {
            builder.setNeutralButton("Cancel Prescription", (dialog, which) -> {
                prescription.setInfo("Cancelled");
                db.collection("reservations").document(prescription.getId())
                        .set(prescription)
                        .addOnSuccessListener(aVoid -> { /* Prescription cancelled */ })
                        .addOnFailureListener(e -> { /* Handle error */ });
            });
        }

        builder.setNegativeButton("Dismiss", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
