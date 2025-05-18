package com.example.koncertjegy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class KoncertReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String koncertNev = intent.getStringExtra("koncertNev");
        String koncertDatum = intent.getStringExtra("koncertDatum");
        NotificationHelper.sendNotification(context, koncertNev, koncertDatum);
    }
}