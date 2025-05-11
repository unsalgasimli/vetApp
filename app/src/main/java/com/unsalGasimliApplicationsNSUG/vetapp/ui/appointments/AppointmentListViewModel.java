package com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.AppointmentRepository;
import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import java.util.List;

public class AppointmentListViewModel extends ViewModel {
    private final MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>();

    public AppointmentListViewModel() {
        // TODO: replace with real patient ID
        String patientId = "CURRENT_PATIENT_ID";
        new AppointmentRepository()
                .fetchAppointments(patientId, new AppointmentRepository.Callback<List<Appointment>>() {
                    @Override public void onSuccess(List<Appointment> data) {
                        appointments.postValue(data);
                    }
                    @Override public void onError(Throwable error) {
                        // TODO: error handling
                    }
                });
    }

    public LiveData<List<Appointment>> getAppointments() {
        return appointments;
    }
}
