package com.example.koncertjegy;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText editTextEmail, editTextPassword, editTextFullName, editTextAdminCode;
    private CheckBox checkBoxAdmin;
    private Button buttonRegister;
    private static final String ADMIN_CODE = "admin123"; // Hardcoded admin kód, élesben biztonságosabb megoldás kell

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextAdminCode = findViewById(R.id.editTextAdminCode);
        checkBoxAdmin = findViewById(R.id.checkBoxAdmin);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Admin kód mező láthatóságának kezelése
        checkBoxAdmin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editTextAdminCode.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();
        String adminCode = editTextAdminCode.getText().toString().trim();
        boolean isAdmin = checkBoxAdmin.isChecked();

        // Validáció
        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            Toast.makeText(this, "Kérlek, töltsd ki az összes kötelező mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isAdmin && !adminCode.equals(ADMIN_CODE)) {
            Toast.makeText(this, "Érvénytelen admin kód!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("nev", fullName);
                        userData.put("email", email);
                        userData.put("isAdmin", isAdmin);

                        db.collection("felhasznalok")
                                .document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, "Regisztráció sikeres!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(RegisterActivity.this, "Hiba a felhasználó adatok mentésekor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Regisztráció sikertelen: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}