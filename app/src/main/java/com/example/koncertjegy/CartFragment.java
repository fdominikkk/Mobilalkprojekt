package com.example.koncertjegy;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartFragment extends Fragment implements CartAdapter.OnCartChangedListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerViewCart;
    private CartAdapter adapter;
    private List<DocumentSnapshot> cartItems;
    private TextView textViewEmpty;
    private MaterialButton buttonPurchase;
    private ListenerRegistration cartListener;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private TextView textViewTotalPrice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI elemek inicializálása
        recyclerViewCart = view.findViewById(R.id.recyclerViewCart);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        buttonPurchase = view.findViewById(R.id.buttonPurchase);
        textViewTotalPrice = view.findViewById(R.id.textViewTotalPrice);

        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));
        cartItems = new ArrayList<>();
        adapter = new CartAdapter(cartItems, this);
        recyclerViewCart.setAdapter(adapter);

        // Vásárlás gomb
        buttonPurchase.setOnClickListener(v -> purchaseItems());

        // Értesítési engedély kérése
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(getContext(), "Értesítési engedély megtagadva!", Toast.LENGTH_SHORT).show();
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Kosár betöltése
        loadCart();

        return view;
    }

    private void loadCart() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            cartListener = db.collection("kosar")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null) {
                            Toast.makeText(getContext(), "Hiba a kosár betöltésekor: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (snapshots != null) {
                            cartItems.clear();
                            for (DocumentSnapshot document : snapshots) {
                                cartItems.add(document);
                                scheduleReminder(document);
                            }
                            adapter.notifyDataSetChanged();
                            textViewEmpty.setVisibility(cartItems.isEmpty() ? View.VISIBLE : View.GONE);
                            recyclerViewCart.setVisibility(cartItems.isEmpty() ? View.GONE : View.VISIBLE);
                        }
                    });
        }
    }

    private void scheduleReminder(DocumentSnapshot item) {
        String koncertDatum = item.getString("koncertDatum");
        if (koncertDatum == null) return;

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), KoncertReminderReceiver.class);
        intent.putExtra("koncertNev", item.getString("koncertNev"));
        intent.putExtra("koncertDatum", koncertDatum);

        // Egyedi kérelemkód az item ID alapján
        int requestCode = item.getId().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Koncert dátumának elemzése
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date koncertDate = sdf.parse(koncertDatum);
            if (koncertDate != null) {
                long reminderTime = koncertDate.getTime() - 24 * 60 * 60 * 1000; // 24 órával korábban
                if (reminderTime > System.currentTimeMillis()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    reminderTime,
                                    pendingIntent);
                        } else {
                            Toast.makeText(getContext(), "Pontos riasztások nem engedélyezettek!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                reminderTime,
                                pendingIntent);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Hiba a riasztás ütemezésekor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void purchaseItems() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "A kosár üres!", Toast.LENGTH_SHORT).show();
            return;
        }

        for (DocumentSnapshot item : new ArrayList<>(cartItems)) {
            // Vásárlás mentése a vasarlasok kollekcióba
            Map<String, Object> purchase = new HashMap<>();
            purchase.put("userId", mAuth.getCurrentUser().getUid());
            purchase.put("koncertId", item.getString("koncertId"));
            purchase.put("koncertNev", item.getString("koncertNev"));
            purchase.put("koncertDatum", item.getString("koncertDatum"));
            purchase.put("jegyAra", item.getDouble("jegyAra"));
            purchase.put("purchaseDate", com.google.firebase.Timestamp.now());

            db.collection("vasarlasok")
                    .add(purchase)
                    .addOnSuccessListener(docRef -> {
                        // Kosár elem törlése
                        db.collection("kosar").document(item.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Alarm törlése
                                    AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                                    Intent intent = new Intent(requireContext(), KoncertReminderReceiver.class);
                                    int requestCode = item.getId().hashCode();
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                            requireContext(),
                                            requestCode,
                                            intent,
                                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                                    alarmManager.cancel(pendingIntent);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Hiba a vásárlás mentésekor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
        Toast.makeText(getContext(), "Vásárlás sikeres!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCartChanged(double totalPrice) {
        if (textViewTotalPrice != null) {
            textViewTotalPrice.setText(String.format("Összesen: %.2f Ft", totalPrice));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cartListener != null) {
            cartListener.remove();
            cartListener = null;
        }
    }
}