package com.unsalGasimliApplicationsNSUG.vetapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // activity_main.xml must contain a container with the ID "fragment_container"
        setContentView(R.layout.activity_main);
        AndroidThreeTen.init(this);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Fragment initialFragment;
        if (currentUser == null) {
            // No user signed in: show the LoginFragment
            initialFragment = new LoginFragment();
        } else {
            // User is signed in: show the main content fragment
            initialFragment = new LoginFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, initialFragment)
                .commit();


    }
}

