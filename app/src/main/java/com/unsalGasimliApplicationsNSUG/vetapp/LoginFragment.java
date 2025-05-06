package com.unsalGasimliApplicationsNSUG.vetapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment (fragment_login.xml)
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = view.findViewById(R.id.mailEditTxt);
        passwordEditText = view.findViewById(R.id.passEditTxt);
        loginButton = view.findViewById(R.id.saveLgnBtn);
        registerButton = view.findViewById(R.id.registerLgnBtn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        db.collection("users").document(user.getUid()).get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                                                            String role = task.getResult().getString("role");
                                                            if ("admin".equalsIgnoreCase(role)) {
                                                                // Open AdminActivity if user is admin
                                                                Intent intent = new Intent(getContext(), AdminActivity.class);
                                                                startActivity(intent);
                                                                // Optionally finish the current activity if needed:
                                                                getActivity().finish();
                                                            } else if ("staff".equalsIgnoreCase(role)) {
                                                                // For staff, load a fragment (or an activity) as desired
                                                                getActivity().getSupportFragmentManager().beginTransaction()
                                                                        .replace(R.id.fragment_container, new ManageStaffFragment())
                                                                        .commit();
                                                            } else {
                                                                // Default to patient fragment (main content)
                                                                startActivity(new Intent(getContext(), PatientActivity.class));
                                                                getActivity().finish();
                                                            }
                                                        } else {
                                                            Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Navigate to RegisterFragment on button click
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RegisterFragment())
                        .addToBackStack(null)  // so the user can return with the back button
                        .commit();
            }
        });

        return view;
    }
}
