package com.unsalGasimliApplicationsNSUG.vetapp.ui.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.unsalGasimliApplicationsNSUG.vetapp.data.model.Staff;
import com.unsalGasimliApplicationsNSUG.vetapp.data.repository.StaffRepository;

import java.util.List;

public class ManageStaffViewModel extends ViewModel {
    private final StaffRepository repo = new StaffRepository();

    private final MutableLiveData<List<Staff>> staffList = new MutableLiveData<>();
    public LiveData<List<Staff>> getStaffList() { return staffList; }

    private final MutableLiveData<String> message = new MutableLiveData<>();
    public LiveData<String> getMessage() { return message; }

    public ManageStaffViewModel() {
        loadAll();
    }

    public void loadAll() {
        repo.fetchAll(new StaffRepository.Callback<List<Staff>>() {
            @Override public void onSuccess(List<Staff> data) {
                staffList.postValue(data);
            }
            @Override public void onError(Exception e) {
                message.postValue("firestore_failed:" + e.getMessage());
            }
        });
    }

    public void createStaff(Staff s, String password) {
        repo.create(s, password, new StaffRepository.Callback<Void>() {
            @Override public void onSuccess(Void v) {
                message.postValue("staff_added");
                loadAll();
            }
            @Override public void onError(Exception e) {
                message.postValue("auth_failed:" + e.getMessage());
            }
        });
    }

    public void updateStaff(Staff s) {
        repo.update(s, new StaffRepository.Callback<Void>() {
            @Override public void onSuccess(Void v) {
                message.postValue("staff_updated");
                loadAll();
            }
            @Override public void onError(Exception e) {
                message.postValue("update_failed:" + e.getMessage());
            }
        });
    }

    public void deleteStaff(String uid) {
        repo.delete(uid, new StaffRepository.Callback<Void>() {
            @Override public void onSuccess(Void v) {
                message.postValue("staff_deleted");
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
