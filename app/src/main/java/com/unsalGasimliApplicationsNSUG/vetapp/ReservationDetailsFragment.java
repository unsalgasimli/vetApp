package com.unsalGasimliApplicationsNSUG.vetapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ReservationDetailsFragment extends Fragment {
    // Keys for passing data in arguments
    private static final String ARG_RESERVATION_ID = "argReservationId";

    public static ReservationDetailsFragment newInstance(String reservationId) {
        ReservationDetailsFragment fragment = new ReservationDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RESERVATION_ID, reservationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservation_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Example UI references
        EditText etDescription = view.findViewById(R.id.etDescription);
        EditText etDate = view.findViewById(R.id.etDate);
        EditText etTime = view.findViewById(R.id.etTime);
        Button btnRequest = view.findViewById(R.id.btnRequest);
        // etc.

        // Retrieve the passed reservation ID
        String reservationId = getArguments() != null
                ? getArguments().getString(ARG_RESERVATION_ID)
                : null;

        // TODO: Load reservation details from your data source (e.g., Firestore) using reservationId
        // Populate the fields (etDescription, etDate, etTime, etc.)

        // Example button action
        btnRequest.setOnClickListener(v -> {
            // TODO: Save changes or create new reservation
        });
    }
}
