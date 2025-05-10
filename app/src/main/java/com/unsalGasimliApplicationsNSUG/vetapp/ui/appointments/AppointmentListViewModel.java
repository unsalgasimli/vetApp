// File: app/src/main/java/com/unsalGasimliApplicationsNSUG/vetapp/ui/appointments/AppointmentListViewModel.java
package com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.unsalGasimliApplicationsNSUG.vetapp.data.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.data.AppointmentRepository;
import java.util.List;

public class AppointmentListViewModel extends ViewModel {
    private final AppointmentRepository repo = new AppointmentRepository();
    public LiveData<List<Appointment>> getAppointments() {
        return repo.getAllAppointments();
    }
    public void addAppointment(Appointment a) {
        repo.addAppointment(a);
    }
}
