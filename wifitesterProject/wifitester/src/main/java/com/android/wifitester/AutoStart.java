package com.android.wifitester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoStart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent i) {
        Log.d("@AutoStart", "BOOT_COMPLETED broadcast received. Executing starter service");
        Intent intent = new Intent(context, StarterService.class);
        context.startService(intent);
    }
}
