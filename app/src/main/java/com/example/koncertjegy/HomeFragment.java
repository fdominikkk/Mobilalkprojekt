package com.example.koncertjegy;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.koncertjegy.KoncertAdapter;
import com.example.koncertjegy.Koncert;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerViewKoncertek;
    private KoncertAdapter adapter;
    private List<Koncert> koncertek;
    private TextView textViewEmpty;
    private ListenerRegistration koncertekListener;
    private FloatingActionButton fabAddKoncert;
    private Button buttonFilterNearby;
    private boolean isAdmin;
    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation;
    private boolean isFilteringNearby;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getUserLocation();
                } else {
                    Toast.makeText(getContext(), "Helymeghatározási engedély megtagadva!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // RecyclerView és üres üzenet beállítása
        recyclerViewKoncertek = view.findViewById(R.id.recyclerViewKoncertek);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        recyclerViewKoncertek.setLayoutManager(new LinearLayoutManager(getContext()));
        koncertek = new ArrayList<>();
        adapter = new KoncertAdapter(koncertek);
        recyclerViewKoncertek.setAdapter(adapter);

        // Üdvözlő üzenet és szűrő gomb
        MaterialCardView cardWelcome = view.findViewById(R.id.cardWelcome);
        buttonFilterNearby = view.findViewById(R.id.buttonFilterNearby);
        TextView textViewWelcome = view.findViewById(R.id.textViewWelcome);

        String fullName = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getDisplayName() : null;
        if (fullName != null && !fullName.isEmpty()) {
            textViewWelcome.setText("Üdvözöljük, " + fullName + "!");
        } else {
            textViewWelcome.setText("Üdvözöljük a Koncertjegy App-ban!");
        }

        // Szűrő gomb eseménykezelője
        buttonFilterNearby.setOnClickListener(v -> {
            if (isFilteringNearby) {
                isFilteringNearby = false;
                buttonFilterNearby.setText("Közeli koncertek");
                loadAllKoncertek();
            } else {
                requestLocationPermission();
            }
        });

        // FAB beállítása koncert hozzáadásához
        fabAddKoncert = view.findViewById(R.id.fabAddKoncert);
        fabAddKoncert.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddKoncertActivity.class);
            startActivity(intent);
        });

        // Ellenőrizzük, hogy a felhasználó admin-e
        checkAdminStatus();

        animateCard(cardWelcome, 0);
        animateCard(view.findViewById(R.id.cardContent), 200);

        return view;
    }

    private void checkAdminStatus() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("felhasznalok")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        isAdmin = document.exists() && document.contains("isAdmin") && document.getBoolean("isAdmin");
                        fabAddKoncert.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                        adapter.setAdmin(isAdmin);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HomeFragment", "Hiba az admin státusz ellenőrzésekor: " + e.getMessage());
                        Toast.makeText(getContext(), "Hiba az admin státusz ellenőrzésekor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        fabAddKoncert.setVisibility(View.GONE);
                        adapter.setAdmin(false);
                    });
        } else {
            fabAddKoncert.setVisibility(View.GONE);
            adapter.setAdmin(false);
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            userLocation = location;
                            isFilteringNearby = true;
                            buttonFilterNearby.setText("Összes koncert");
                            filterNearbyKoncertek();
                        } else {
                            Toast.makeText(getContext(), "Nem sikerült lekérni a helyet!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Hiba a hely lekérésekor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void filterNearbyKoncertek() {
        if (userLocation == null) {
            loadAllKoncertek();
            return;
        }

        koncertekListener = db.collection("koncertek")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Hiba a koncertek betöltésekor: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        koncertek.clear();
                        for (QueryDocumentSnapshot document : snapshots) {
                            try {
                                Koncert koncert = document.toObject(Koncert.class);
                                koncert.setId(document.getId());
                                double distance = calculateDistance(
                                        userLocation.getLatitude(), userLocation.getLongitude(),
                                        koncert.getLatitude(), koncert.getLongitude());
                                if (distance <= 50) { // 50 km-es sugár
                                    koncertek.add(koncert);
                                }
                            } catch (Exception e) {
                                Log.e("HomeFragment", "Hiba a koncert deszerializálásakor: " + e.getMessage());
                            }
                        }
                        adapter.notifyDataSetChanged();
                        textViewEmpty.setVisibility(koncertek.isEmpty() ? View.VISIBLE : View.GONE);
                        recyclerViewKoncertek.setVisibility(koncertek.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                });
    }

    private void loadAllKoncertek() {
        koncertekListener = db.collection("koncertek")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Hiba a koncertek betöltésekor: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        koncertek.clear();
                        for (QueryDocumentSnapshot document : snapshots) {
                            try {
                                Koncert koncert = document.toObject(Koncert.class);
                                koncert.setId(document.getId());
                                koncertek.add(koncert);
                            } catch (Exception e) {
                                Log.e("HomeFragment", "Hiba a koncert deszerializálásakor: " + e.getMessage());
                            }
                        }
                        adapter.notifyDataSetChanged();
                        textViewEmpty.setVisibility(koncertek.isEmpty() ? View.VISIBLE : View.GONE);
                        recyclerViewKoncertek.setVisibility(koncertek.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                });
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0] / 1000; // Távolság km-ben
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isFilteringNearby) {
            loadAllKoncertek();
        } else {
            filterNearbyKoncertek();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (koncertekListener != null) {
            koncertekListener.remove();
            koncertekListener = null;
        }
    }

    private void animateCard(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(100f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).setDuration(500),
                ObjectAnimator.ofFloat(view, "translationY", 100f, 0f).setDuration(500)
        );
        animatorSet.setStartDelay(delay);
        animatorSet.start();
    }
}