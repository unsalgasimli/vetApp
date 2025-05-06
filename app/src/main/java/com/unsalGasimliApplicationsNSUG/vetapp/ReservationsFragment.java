package com.unsalGasimliApplicationsNSUG.vetapp;

import android.app.AlertDialog;
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

public class ReservationsFragment extends Fragment {

    private RecyclerView recyclerReservations;
    private FloatingActionButton fabAddAppointment, fabAddPrescription;
    private FirebaseFirestore db;
    // This list will hold all reservations retrieved from Firebase.
    private List<ReservationItem> allReservations = new ArrayList<>();
    private ReservationAdapter adapter;
    private Spinner spinnerFilter;
    private String fullName = "";
    // This variable stores the currently selected filter option.
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

        // Initialize RecyclerView and its adapter.
        recyclerReservations = view.findViewById(R.id.recyclerReservations);
        recyclerReservations.setLayoutManager(new LinearLayoutManager(getContext()));
        // Use instanceof to decide which dialog to open.
        adapter = new ReservationAdapter(new ArrayList<>(), (ReservationItem item, int position) -> {
            if (item instanceof Appointment) {
                showAppointmentDialog((Appointment) item, position);
            } else if (item instanceof Prescription) {
                showPrescriptionDialog((Prescription) item, position);
            }
        });
        recyclerReservations.setAdapter(adapter);

        // Set up filter spinner.
        spinnerFilter = view.findViewById(R.id.spinnerFilter);
        String[] filterOptions = {"All", "Appointments", "Prescriptions"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, filterOptions);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);
        spinnerFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View spinnerView, int position, long id) {
                selectedFilter = filterOptions[position];
                applyFilter();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // Fetch reservations from Firebase.
        loadReservations();

        // Set up FABs.
        fabAddAppointment = view.findViewById(R.id.fabAddAppointment);
        fabAddPrescription = view.findViewById(R.id.fabAddPrescription);
        fabAddAppointment.setOnClickListener(v -> showAppointmentDialog(null, -1));
        fabAddPrescription.setOnClickListener(v -> showPrescriptionDialog(null, -1));
    }

    // Fetch the reservations for the current user.
    private void loadReservations() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference userDocRef = db.collection("users").document(uid);
            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String firstName = documentSnapshot.getString("firstName");
                    String lastName = documentSnapshot.getString("lastName");
                    fullName = firstName + " " + lastName;
                    Log.d("GetFullName", "User's full name: " + fullName);
                    db.collection("reservations")
                            .whereEqualTo("patFullName", fullName)
                            .addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                                if (error != null || querySnapshot == null) return;
                                List<ReservationItem> items = new ArrayList<>();
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
                                        items.add(item);
                                    }
                                }
                                Collections.sort(items, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
                                allReservations = items;
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
    }

    // Filter reservations based on the selected filter option.
    private void applyFilter() {
        List<ReservationItem> filtered = new ArrayList<>();
        for (ReservationItem item : allReservations) {
            if ("Appointments".equals(selectedFilter) && !(item instanceof Appointment)) {
                continue;
            }
            if ("Prescriptions".equals(selectedFilter) && !(item instanceof Prescription)) {
                continue;
            }
            filtered.add(item);
        }
        adapter.updateData(filtered);
    }

    // Appointment dialog.
    private void showAppointmentDialog(@Nullable final Appointment appointment, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_appointment, null);
        builder.setView(dialogView);

        String currentDate = DateFormat.format("yyyy-MM-dd", new Date()).toString();
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        tvDate.setText("Date: " + currentDate);

        // Time picker.
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

        // Doctor spinner.
        Spinner spinnerDoctor = dialogView.findViewById(R.id.spinnerDoctor);
        List<String> staffList = new ArrayList<>();
        ArrayAdapter<String> staffAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, staffList);
        staffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoctor.setAdapter(staffAdapter);
        db.collection("users")
                .whereEqualTo("role", "staff")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    staffList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String firstName = doc.getString("firstName");
                        String lastName = doc.getString("lastName");
                        if (firstName != null && lastName != null) {
                            staffList.add(firstName + " " + lastName);
                        }
                    }
                    staffAdapter.notifyDataSetChanged();
                    if (appointment != null && appointment.getDocFullName() != null) {
                        int index = staffList.indexOf(appointment.getDocFullName());
                        if (index >= 0) spinnerDoctor.setSelection(index);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error.
                });

        // Appointment type spinner (the appointment subtype/category).
        Spinner spinnerAppType = dialogView.findViewById(R.id.spinnerAppointmentType);
        String[] appTypes = {"Consultation", "Vaccination", "Surgery", "Follow-up",
                "Dental Cleaning", "Emergency", "Routine Check-up", "Diagnostic Test"};
        ArrayAdapter<String> appTypeAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, appTypes);
        appTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAppType.setAdapter(appTypeAdapter);

        // Info field.
        EditText etInfo = dialogView.findViewById(R.id.etInfo);

        if (appointment != null) {
            etTime.setText(appointment.getTime());
            // Here we assume getAppointmentType() returns the appointment category.
            for (int i = 0; i < appTypes.length; i++) {
                if (appTypes[i].equals(appointment.getAppointmentType())) {
                    spinnerAppType.setSelection(i);
                    break;
                }
            }
            etInfo.setText(appointment.getInfo());
            builder.setTitle("Update Appointment");
        } else {
            builder.setTitle("Add Appointment");
        }

        builder.setPositiveButton(appointment != null ? "Update" : "Add", (dialog, which) -> {
            String time = etTime.getText().toString().trim();
            String appType = spinnerAppType.getSelectedItem().toString();
            String info = etInfo.getText().toString().trim();
            String status = "Requested";
            String selectedDoc = (String) spinnerDoctor.getSelectedItem();

            try {
                String[] timeParts = time.split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                String[] dateParts = currentDate.split("-");
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]) - 1;
                int day = Integer.parseInt(dateParts[2]);
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, day, hour, minute);
                Date finalDate = cal.getTime();
                Timestamp ts = new Timestamp(finalDate);

                if (appointment == null) {
                    Appointment newApp = new Appointment();
                    newApp.setDocFullName(selectedDoc);
                    newApp.setPatFullName(fullName);
                    newApp.setPetName("Max"); // Adjust as needed.
                    newApp.setDate(currentDate);
                    newApp.setTime(time);
                    newApp.setAppointmentCategory(appType);
                    newApp.setInfo(info);
                    newApp.setStatus(status);
                    newApp.setTimestamp(ts);
                    newApp.setType("APPOINTMENT");
                    db.collection("reservations")
                            .add(newApp)
                            .addOnSuccessListener(documentReference -> newApp.setId(documentReference.getId()))
                            .addOnFailureListener(e -> {
                                // Handle error.
                            });
                } else {
                    appointment.setDocFullName(selectedDoc);
                    appointment.setDate(currentDate);
                    appointment.setTime(time);
                    appointment.setAppointmentCategory(appType);
                    appointment.setInfo(info);
                    appointment.setStatus(status);
                    appointment.setTimestamp(ts);
                    db.collection("reservations").document(appointment.getId())
                            .set(appointment)
                            .addOnSuccessListener(aVoid -> {
                                // Updated successfully.
                            })
                            .addOnFailureListener(e -> {
                                // Handle error.
                            });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        if (appointment != null) {
            builder.setNeutralButton("Cancel Appointment", (dialog, which) -> {
                appointment.setStatus("Cancelled");
                db.collection("reservations").document(appointment.getId())
                        .set(appointment)
                        .addOnSuccessListener(aVoid -> {
                            // Cancelled successfully.
                        })
                        .addOnFailureListener(e -> {
                            // Handle error.
                        });
            });
        }

        builder.setNegativeButton("Dismiss", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Prescription dialog.
    private void showPrescriptionDialog(@Nullable final Prescription prescription, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_prescription, null);
        builder.setView(dialogView);

        String currentDate = DateFormat.format("yyyy-MM-dd", new Date()).toString();
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        tvDate.setText("From: " + currentDate);

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
            String dateTo = etDateTo.getText().toString().trim();
            String frequency = etFrequency.getText().toString().trim();
            String info = etInfo.getText().toString().trim();
            Timestamp ts = new Timestamp(new Date());

            if (prescription == null) {
                Prescription newPre = new Prescription();
                newPre.setDocFullName("Dr. Smith"); // Adjust as needed.
                newPre.setPatFullName("John Doe");
                newPre.setPetName("Max");
                newPre.setDateFrom(currentDate);
                newPre.setDateTo(dateTo);
                newPre.setFrequency(frequency);
                newPre.setInfo(info);
                newPre.setTimestamp(ts);
                newPre.setType("PRESCRIPTION");
                db.collection("reservations")
                        .add(newPre)
                        .addOnSuccessListener(documentReference -> newPre.setId(documentReference.getId()))
                        .addOnFailureListener(e -> {
                            // Handle error.
                        });
            } else {
                prescription.setDateTo(dateTo);
                prescription.setFrequency(frequency);
                prescription.setInfo(info);
                prescription.setTimestamp(ts);
                db.collection("reservations").document(prescription.getId())
                        .set(prescription)
                        .addOnSuccessListener(aVoid -> {
                            // Updated successfully.
                        })
                        .addOnFailureListener(e -> {
                            // Handle error.
                        });
            }
        });

        if (prescription != null) {
            builder.setNeutralButton("Cancel Prescription", (dialog, which) -> {
                prescription.setInfo("Cancelled");
                db.collection("reservations").document(prescription.getId())
                        .set(prescription)
                        .addOnSuccessListener(aVoid -> {
                            // Cancelled successfully.
                        })
                        .addOnFailureListener(e -> {
                            // Handle error.
                        });
            });
        }

        builder.setNegativeButton("Dismiss", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
