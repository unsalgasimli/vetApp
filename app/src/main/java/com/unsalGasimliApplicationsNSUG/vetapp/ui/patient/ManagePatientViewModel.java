// File: app/src/main/java/com/unsalGasimliApplicationsNSUG/vetapp/ui/patient/ManagePatientViewModel.java
package com.unsalGasimliApplicationsNSUG.vetapp.ui.patient;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Patient;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.PatientRepository;

import java.util.List;

public class ManagePatientViewModel extends ViewModel {
    private final PatientRepository repo = new PatientRepository();

    private final MutableLiveData<List<Patient>> patients = new MutableLiveData<>();
    public LiveData<List<Patient>> getPatients() { return patients; }

    private final MutableLiveData<String> message = new MutableLiveData<>();
    public LiveData<String> getMessage() { return message; }

    public ManagePatientViewModel() {
        loadAll();
    }

    public void loadAll() {
        repo.fetchAll(new PatientRepository.Callback<List<Patient>>() {
            @Override public void onSuccess(List<Patient> data) {
                patients.postValue(data);
            }
            @Override public void onError(Exception e) {
                message.postValue("firestore_failed:" + e.getMessage());
            }
        });
    }

    public void createPatient(Patient p, String password) {
        repo.create(p, password, new PatientRepository.Callback<Void>() {
            @Override public void onSuccess(Void v) {
                message.postValue("patient_added");
                loadAll();
            }
            @Override public void onError(Exception e) {
                message.postValue("auth_failed:" + e.getMessage());
            }
        });
    }

    public void updatePatient(Patient p) {
        repo.update(p, new PatientRepository.Callback<Void>() {
            @Override public void onSuccess(Void v) {
                message.postValue("patient_updated");
                loadAll();
            }
            @Override public void onError(Exception e) {
                message.postValue("update_failed:" + e.getMessage());
            }
        });
    }

    public void deletePatient(String uid) {
        repo.delete(uid, new PatientRepository.Callback<Void>() {
            @Override public void onSuccess(Void v) {
                message.postValue("patient_deleted");
                loadAll();
            }
            @Override public void onError(Exception e) {
                message.postValue("delete_failed:" + e.getMessage());
            }
        });
    }

    public void clearMessage() {
        message.postValue(null);
    }
}
