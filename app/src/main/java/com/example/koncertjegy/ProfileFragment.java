package com.example.koncertjegy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView textViewName, textViewEmail;
    private RecyclerView recyclerViewPurchasedTickets;
    private PurchasedTicketsAdapter adapter;
    private List<DocumentSnapshot> purchasedTickets;
    private ListenerRegistration userListener, purchasesListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI elemek inicializálása
        textViewName = view.findViewById(R.id.textViewName);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        recyclerViewPurchasedTickets = view.findViewById(R.id.recyclerViewPurchasedTickets);
        MaterialButton buttonScanQR = view.findViewById(R.id.buttonScanQR);
        MaterialButton buttonLogout = view.findViewById(R.id.buttonLogout);

        // RecyclerView beállítása
        recyclerViewPurchasedTickets.setLayoutManager(new LinearLayoutManager(getContext()));
        purchasedTickets = new ArrayList<>();
        adapter = new PurchasedTicketsAdapter(purchasedTickets);
        recyclerViewPurchasedTickets.setAdapter(adapter);

        // QR-kód szkennelés
        buttonScanQR.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ScanQRActivity.class);
            startActivity(intent);
        });

        // Kijelentkezés
        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        // Felhasználói adatok és vásárlások betöltése
        loadUserData();
        loadPurchasedTickets();

        return view;
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            userListener = db.collection("felhasznalok").document(userId)
                    .addSnapshotListener((document, error) -> {
                        if (error != null) {
                            Toast.makeText(getContext(), "Hiba a felhasználói adatok betöltésekor: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (document != null && document.exists()) {
                            String name = document.getString("nev");
                            String email = document.getString("email");
                            textViewName.setText(name != null ? name : "Név nincs megadva");
                            textViewEmail.setText(email != null ? email : "E-mail nincs megadva");
                        } else {
                            textViewName.setText("Név nincs megadva");
                            textViewEmail.setText(mAuth.getCurrentUser().getEmail());
                        }
                    });
        }
    }

    private void loadPurchasedTickets() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            purchasesListener = db.collection("vasarlasok")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null) {
                            Toast.makeText(getContext(), "Hiba a vásárlások betöltésekor: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (snapshots != null) {
                            purchasedTickets.clear();
                            purchasedTickets.addAll(snapshots.getDocuments());
                            adapter.notifyDataSetChanged();
                            recyclerViewPurchasedTickets.setVisibility(purchasedTickets.isEmpty() ? View.GONE : View.VISIBLE);
                        }
                    });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
        if (purchasesListener != null) {
            purchasesListener.remove();
            purchasesListener = null;
        }
    }
}