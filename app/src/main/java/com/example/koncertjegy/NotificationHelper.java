package com.example.koncertjegy;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "KoncertReminderChannel";
    private static final String CHANNEL_NAME = "Koncert Emlékeztetők";
    private static final String CHANNEL_DESC = "Értesítések közelgő koncertekről";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static void sendNotification(Context context, String koncertNev, String koncertDatum) {
        // Intent a CartFragment megnyitásához
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("navigateTo", "cart");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Értesítés építése
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Koncert emlékeztető: " + koncertNev)
                .setContentText("A koncert " + koncertDatum + "-kor kezdődik! Véglegesítsd a vásárlást!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Értesítés küldése
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                try {
                    manager.notify((int) System.currentTimeMillis(), builder.build());
                } catch (SecurityException e) {
                    Toast.makeText(context, "Értesítési engedély hiányzik!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Értesítési engedély szükséges!", Toast.LENGTH_SHORT).show();
            }
        } else {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}