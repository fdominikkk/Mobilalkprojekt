package com.example.koncertjegy;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditKoncertActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private EditText editTextNev, editTextHelyszin, editTextDatum, editTextJegyAra, editTextElerhetoJegyek, editTextLeiras, editTextKepUrl, editTextLatitude, editTextLongitude;
    private Button buttonSave;
    private String koncertId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_koncert);

        db = FirebaseFirestore.getInstance();

        // UI elemek inicializálása
        editTextNev = findViewById(R.id.editTextNev);
        editTextHelyszin = findViewById(R.id.editTextHelyszin);
        editTextDatum = findViewById(R.id.editTextDatum);
        editTextJegyAra = findViewById(R.id.editTextJegyAra);
        editTextElerhetoJegyek = findViewById(R.id.editTextElerhetoJegyek);
        editTextLeiras = findViewById(R.id.editTextLeiras);
        editTextKepUrl = findViewById(R.id.editTextKepUrl);
        editTextLatitude = findViewById(R.id.editTextLatitude);
        editTextLongitude = findViewById(R.id.editTextLongitude);
        buttonSave = findViewById(R.id.buttonSave);

        // Koncert ID lekérése
        koncertId = getIntent().getStringExtra("koncertId");
        if (koncertId == null) {
            Toast.makeText(this, "Hiba: Koncert ID hiányzik!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Koncert adatainak betöltése
        loadKoncertData();

        // Mentés gomb eseménykezelője
        buttonSave.setOnClickListener(v -> saveKoncert());
    }

    private void loadKoncertData() {
        db.collection("koncertek").document(koncertId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        editTextNev.setText(document.getString("nev"));
                        editTextHelyszin.setText(document.getString("helyszin"));
                        editTextDatum.setText(document.getString("datum"));
                        editTextJegyAra.setText(String.valueOf(document.getDouble("jegyAra")));
                        editTextElerhetoJegyek.setText(String.valueOf(document.getLong("elerhetoJegyek")));
                        editTextLeiras.setText(document.getString("leiras"));
                        editTextKepUrl.setText(document.getString("kepUrl"));
                        editTextLatitude.setText(String.valueOf(document.getDouble("latitude")));
                        editTextLongitude.setText(String.valueOf(document.getDouble("longitude")));
                    } else {
                        Toast.makeText(this, "Hiba: A koncert nem található!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba az adatok betöltésekor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void saveKoncert() {
        String nev = editTextNev.getText().toString().trim();
        String helyszin = editTextHelyszin.getText().toString().trim();
        String datum = editTextDatum.getText().toString().trim();
        String jegyAraStr = editTextJegyAra.getText().toString().trim();
        String elerhetoJegyekStr = editTextElerhetoJegyek.getText().toString().trim();
        String leiras = editTextLeiras.getText().toString().trim();
        String kepUrl = editTextKepUrl.getText().toString().trim();
        String latitudeStr = editTextLatitude.getText().toString().trim();
        String longitudeStr = editTextLongitude.getText().toString().trim();

        // Validáció
        if (nev.isEmpty() || helyszin.isEmpty() || datum.isEmpty() || jegyAraStr.isEmpty() || elerhetoJegyekStr.isEmpty() || latitudeStr.isEmpty() || longitudeStr.isEmpty()) {
            Toast.makeText(this, "Kérlek, töltsd ki az összes kötelező mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }

        double jegyAra, latitude, longitude;
        int elerhetoJegyek;
        try {
            jegyAra = Double.parseDouble(jegyAraStr);
            elerhetoJegyek = Integer.parseInt(elerhetoJegyekStr);
            latitude = Double.parseDouble(latitudeStr);
            longitude = Double.parseDouble(longitudeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Érvénytelen számformátum!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Koncert adatainak előkészítése
        Map<String, Object> koncert = new HashMap<>();
        koncert.put("nev", nev);
        koncert.put("helyszin", helyszin);
        koncert.put("datum", datum);
        koncert.put("jegyAra", jegyAra);
        koncert.put("elerhetoJegyek", elerhetoJegyek);
        koncert.put("leiras", leiras);
        koncert.put("kepUrl", kepUrl);
        koncert.put("latitude", latitude);
        koncert.put("longitude", longitude);

        // Koncert frissítése
        db.collection("koncertek").document(koncertId)
                .set(koncert)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Koncert sikeresen frissítve!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba a koncert frissítésekor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}