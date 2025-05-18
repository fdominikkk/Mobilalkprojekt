package com.example.koncertjegy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.annotation.OptIn;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;

public class ScanQRActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private BarcodeScanner barcodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // PreviewView létrehozása programozottan
        previewView = new PreviewView(this);
        previewView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(previewView);

        // ML Kit Barcode Scanner inicializálása
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);

        // Engedélykérés inicializálása
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startCamera();
                    } else {
                        Toast.makeText(this, "Kamera engedély megtagadva!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

        // Kamera engedély kérése
        requestCameraPermission();
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview beállítása
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Kamera kiválasztása (hátsó kamera)
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Képfeldolgozás QR-kód szkenneléshez
                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
                    InputImage image;
                    try {
                        image = InputImage.fromMediaImage(
                                imageProxy.getImage(),
                                imageProxy.getImageInfo().getRotationDegrees());
                    } catch (Exception e) {
                        imageProxy.close();
                        return;
                    }

                    barcodeScanner.process(image)
                            .addOnSuccessListener(barcodes -> {
                                for (Barcode barcode : barcodes) {
                                    String qrContent = barcode.getRawValue();
                                    if (qrContent != null) {
                                        Toast.makeText(this, "QR kód tartalma: " + qrContent, Toast.LENGTH_LONG).show();
                                        // Ide jöhet a jegy érvényesítési logika
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Hiba a QR-kód olvasásakor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnCompleteListener(task -> imageProxy.close());
                });

                // Kamera indítása
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Hiba a kamera indításakor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        barcodeScanner.close();
    }
}