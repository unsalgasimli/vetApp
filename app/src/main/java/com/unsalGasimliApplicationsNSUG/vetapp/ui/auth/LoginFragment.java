package com.unsalGasimliApplicationsNSUG.vetapp.ui.auth;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unsalGasimliApplicationsNSUG.vetapp.R;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.main.AdminActivity;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.staff.StaffActivity;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.patient.PatientActivity;
import com.unsalGasimliApplicationsNSUG.vetapp.ui.staff.StaffActivity;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        emailEditText    = view.findViewById(R.id.emailInput);
        passwordEditText = view.findViewById(R.id.passwordInput);
        loginButton      = view.findViewById(R.id.btnLogin);
        registerButton   = view.findViewById(R.id.btnRegister);

        loginButton.setOnClickListener(v -> attemptLogin());
        registerButton.setOnClickListener(v -> {
            // delegate to AuthActivity to swap in the RegisterFragment
            if (getActivity() instanceof com.unsalGasimliApplicationsNSUG.vetapp.ui.main.AuthActivity) {
                ((com.unsalGasimliApplicationsNSUG.vetapp.ui.main.AuthActivity) getActivity()).showRegister();
            }
        });

        return view;
    }

    private void attemptLogin() {
        String email    = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(),
                    "Please enter email and password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        fetchUserRoleAndNavigate(user);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Authentication failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void fetchUserRoleAndNavigate(@NonNull FirebaseUser user) {
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(this::handleUserDocument)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to load user data: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void handleUserDocument(@NonNull DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(getContext(),
                    "User record not found",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String role = doc.getString("role");
        Intent intent;
        if ("admin".equalsIgnoreCase(role)) {
            intent = new Intent(requireContext(), AdminActivity.class);
        } else if ("staff".equalsIgnoreCase(role)) {
            intent = new Intent(requireContext(), StaffActivity.class);
        } else {
            intent = new Intent(requireContext(), PatientActivity.class);
        }

        startActivity(intent);
        requireActivity().finish();
    }
}
