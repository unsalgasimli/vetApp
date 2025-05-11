package com.unsalGasimliApplicationsNSUG.vetapp.ui.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.auth.LoginFragment;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.auth.RegisterFragment;


public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Show LoginFragment by default
        replaceFragment(new LoginFragment());
    }

    public void showLogin() {
        replaceFragment(new LoginFragment());
    }

    public void showRegister() {
        replaceFragment(new RegisterFragment());
    }



    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_fragment_container, fragment)
                .commit();
    }
}