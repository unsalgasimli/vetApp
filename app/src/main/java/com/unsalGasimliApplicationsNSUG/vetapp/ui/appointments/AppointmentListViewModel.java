// app/src/main/java/com/unsalGasimliApplicationsNSUG/vetapp/ui/appointments/AppointmentListViewModel.java
package com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments;

import androidx.lifecycle.*;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.AppointmentRepository;

import java.util.List;

public class AppointmentListViewModel extends ViewModel {
    private final AppointmentRepository repo = new AppointmentRepository();
    private final MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<Appointment>> getAppointments() { return appointments; }
    public LiveData<String> getError()            { return error;        }

    public void loadAppointments(String patientId) {
        repo.fetchForPatient(patientId, new AppointmentRepository.Callback<List<Appointment>>() {
            @Override public void onSuccess(List<Appointment> data) {
                appointments.postValue(data);
            }
            @Override public void onError(Throwable t) {
                error.postValue(t.getMessage());
            }
        });
    }
}
