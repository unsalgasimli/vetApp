package com.unsalGasimliApplicationsNSUG.vetapp.ui.appointments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Appointment;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.AppointmentRepository;

import java.util.List;

public class AppointmentViewModel extends ViewModel {
    private final AppointmentRepository repo = new AppointmentRepository();

    private final MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>();
    private final MutableLiveData<String>            message      = new MutableLiveData<>();

    public LiveData<List<Appointment>> getAppointments() { return appointments; }
    public LiveData<String>           getMessage()      { return message;      }

    public void clearMessage() {
        message.postValue(null);
    }

    public void loadForPatient(String patientId) {
        repo.fetchForPatient(patientId, new AppointmentRepository.Callback<List<Appointment>>() {
            @Override public void onSuccess(List<Appointment> data) {
                appointments.postValue(data);
            }
            @Override public void onError(Throwable t) {
                message.postValue("load_error:" + t.getMessage());
            }
        });
    }

    public void loadAllAppointments() {
        repo.fetchAllAppointments(new AppointmentRepository.Callback<List<Appointment>>() {
            @Override public void onSuccess(List<Appointment> data) {
                appointments.postValue(data);
            }
            @Override public void onError(Throwable t) {
                message.postValue("load_error:" + t.getMessage());
            }
        });
    }

    public void createAppointment(Appointment a) {
        repo.createAppointment(a, new AppointmentRepository.Callback<Void>() {
            @Override public void onSuccess(Void unused) {
                message.postValue("appointment_created");
                loadForPatient(a.getPatientId());
            }
            @Override public void onError(Throwable t) {
                message.postValue("create_error:" + t.getMessage());
            }
        });
    }

    public void updateAppointment(Appointment a) {
        repo.updateAppointment(a, new AppointmentRepository.Callback<Void>() {
            @Override public void onSuccess(Void unused) {
                message.postValue("appointment_updated");
                loadAllAppointments();
            }
            @Override public void onError(Throwable t) {
                message.postValue("update_error:" + t.getMessage());
            }
        });
    }

    public void deleteAppointment(String patientId, String apptId) {
        repo.deleteAppointment(patientId, apptId, new AppointmentRepository.Callback<Void>() {
            @Override public void onSuccess(Void unused) {
                message.postValue("appointment_deleted");
                loadAllAppointments();
            }
            @Override public void onError(Throwable t) {
                message.postValue("delete_error:" + t.getMessage());
            }
        });
    }
}
