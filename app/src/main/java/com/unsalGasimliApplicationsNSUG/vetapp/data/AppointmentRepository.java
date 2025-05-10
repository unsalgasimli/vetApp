// File: app/src/main/java/com/unsalGasimliApplicationsNSUG/vetapp/data/AppointmentRepository.java
package com.unsalGasimliApplicationsNSUG.vetapp.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.database.*;

import java.util.*;

public class AppointmentRepository {
    private final DatabaseReference apptRef =
            FirebaseDatabase.getInstance().getReference("appointments");

    private final MutableLiveData<List<Appointment>> apptsLiveData = new MutableLiveData<>();

    public AppointmentRepository() {
        apptRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                List<Appointment> list = new ArrayList<>();
                for (DataSnapshot cs : snap.getChildren()) {
                    Appointment a = cs.getValue(Appointment.class);
                    if (a != null) {
                        a.setId(cs.getKey());
                        list.add(a);
                    }
                }
                apptsLiveData.postValue(list);
            }
            @Override public void onCancelled(DatabaseError e) {}
        });
    }

    public LiveData<List<Appointment>> getAllAppointments() {
        return apptsLiveData;
    }
    public void addAppointment(Appointment appt) {
        apptRef.push().setValue(appt);
    }
    // TODO: update, delete…
}
