package com.unsalGasimliApplicationsNSUG.vetapp.ui.prescriptions;

import androidx.lifecycle.*;

import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Prescription;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.PrescriptionRepository;

import java.util.List;

public class PrescriptionViewModel extends ViewModel {
    private final PrescriptionRepository repo = new PrescriptionRepository();

    private final MutableLiveData<Prescription>   prescription = new MutableLiveData<>();
    private final MutableLiveData<List<Prescription>> prescriptions = new MutableLiveData<>();
    private final MutableLiveData<String>        message      = new MutableLiveData<>();

    public LiveData<Prescription> getPrescription()      { return prescription;   }
    public LiveData<List<Prescription>> getPrescriptions() { return prescriptions;  }
    public LiveData<String> getMessage()                { return message;       }

    public void clearMessage() {
        message.postValue(null);
    }

    public void fetchPrescriptionById(String patientId, String prescId) {
        repo.fetchById(patientId, prescId, new PrescriptionRepository.Callback<Prescription>() {
            @Override public void onSuccess(Prescription p) {
                prescription.postValue(p);
            }
            @Override public void onError(Throwable t) {
                message.postValue("load_error:" + t.getMessage());
            }
        });
    }

    public void loadForPatient(String patientId) {
        repo.fetchForPatient(patientId, new PrescriptionRepository.Callback<List<Prescription>>() {
            @Override public void onSuccess(List<Prescription> list) {
                prescriptions.postValue(list);
            }
            @Override public void onError(Throwable t) {
                message.postValue("load_error:" + t.getMessage());
            }
        });
    }

    public void createPrescription(Prescription p) {
        repo.create(p, new PrescriptionRepository.Callback<Void>() {
            @Override public void onSuccess(Void unused) {
                message.postValue("prescription_saved");
                loadForPatient(p.getPatientId());
            }
            @Override public void onError(Throwable t) {
                message.postValue("prescription_error:" + t.getMessage());
            }
        });
    }

    public void updatePrescription(Prescription p) {
        repo.updatePrescription(p, new PrescriptionRepository.Callback<Void>() {
            @Override public void onSuccess(Void unused) {
                message.postValue("prescription_updated");
                loadForPatient(p.getPatientId());
            }
            @Override public void onError(Throwable t) {
                message.postValue("prescription_error:" + t.getMessage());
            }
        });
    }

    public void deletePrescription(String patientId, String prescId) {
        repo.deletePrescription(patientId, prescId, new PrescriptionRepository.Callback<Void>() {
            @Override public void onSuccess(Void unused) {
                message.postValue("prescription_deleted");
                loadForPatient(patientId);
            }
            @Override public void onError(Throwable t) {
                message.postValue("prescription_error:" + t.getMessage());
            }
        });
    }
}
