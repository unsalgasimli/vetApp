package com.unsalGasimliApplicationsNSUG.vetapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unsalGasimliApplicationsNSUG.vetapp.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReservationDetailsFragment extends Fragment {
    private static final String TAG = "ReservationDetails";
    private static final String ARG_RESERVATION_ID = "argReservationId";

    private String reservationId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private EditText etDescription, etDate, etTime;
    private Button btnRequest;

    public static ReservationDetailsFragment newInstance(String reservationId) {
        ReservationDetailsFragment frag = new ReservationDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RESERVATION_ID, reservationId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reservationId = getArguments() != null
                ? getArguments().getString(ARG_RESERVATION_ID)
                : null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservation_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etDescription = view.findViewById(R.id.etDescription);
        etDate        = view.findViewById(R.id.etDate);
        etTime        = view.findViewById(R.id.etTime);
        btnRequest    = view.findViewById(R.id.btnRequest);

        // --- date picker ---
        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (DatePicker dp, int year, int month, int day) -> {
                        // month is 0-based
                        String d = String.format(Locale.getDefault(),
                                "%04d-%02d-%02d", year, month+1, day);
                        etDate.setText(d);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // --- time picker ---
        etTime.setFocusable(false);
        etTime.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTitleText("Select Time")
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .build();
            picker.addOnPositiveButtonClickListener(pickerInstance -> {
                int h = picker.getHour(), m = picker.getMinute();
                etTime.setText(String.format(Locale.getDefault(),
                        "%02d:%02d", h, m));
            });
            picker.show(getParentFragmentManager(), "TIME_PICKER");
        });

        btnRequest.setOnClickListener(v -> sendReservationRequest());
    }

    private void sendReservationRequest() {
        String desc = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();

        // basic validation
        if (TextUtils.isEmpty(desc) ||
                TextUtils.isEmpty(date) ||
                TextUtils.isEmpty(time)) {
            Toast.makeText(requireContext(),
                    "Please fill out description, date, and time.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(),
                    "You must be logged in to make a request.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // build payload
        Map<String,Object> req = new HashMap<>();
        req.put("reservationId",   reservationId);
        req.put("userId",          user.getUid());
        req.put("description",     desc);
        req.put("date",            date);
        req.put("time",            time);
        req.put("status",          "pending");
        req.put("createdAt",       FieldValue.serverTimestamp());

        db.collection("reservation_requests")
                .add(req)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(requireContext(),
                            "Reservation request sent!",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Request created with ID: " + doc.getId());
                    // go back
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to send request: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error creating request", e);
                });
    }
}
