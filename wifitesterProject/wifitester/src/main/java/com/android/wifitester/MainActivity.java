package com.android.wifitester;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {

    WifiManager mainWifi;
    IntentFilter filter;
    boolean intentIsRegistered = false;

    private BroadcastReceiver wifiEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("DEBUG", "Update received!");
            if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                Log.e("DEBUG","UPDATE RECEIVED");
                //List<ScanResult> li = mainWifi.getScanResults();
                Toast.makeText(context, "New values received!", Toast.LENGTH_SHORT).show();
                /*for (int i=0; i<li.size(); i++) {
                    Log.e("DEBUG","ssid: "+li.get(i).SSID+" bssid: "+li.get(i).BSSID+" cap: "+li.get(i).capabilities+" level: "+li.get(i).level+ "chan: "+li.get(i).frequency);
                }*/
                mainWifi.startScan();
                Toast.makeText(context, "New scan started!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // Button wifiConnect = (Button)findViewById(R.id.WifiConnect);
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.registerReceiver(wifiEventReceiver, filter);
        intentIsRegistered = true;

        if(!mainWifi.isWifiEnabled()){
            Log.e("DEBUG","turning on wifi");
            Toast.makeText(getApplicationContext(), "Enabling Wifi...",
                    Toast.LENGTH_LONG).show();
            mainWifi.setWifiEnabled(true);
        } else {
            Log.e("DEBUG","wifi is on");
        }

        if (!mainWifi.startScan()) {
            Log.e("Error","Scanning could not start");
        } else {
            Log.e("DEBUG", "Scanning has started");
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Refresh");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        mainWifi.startScan();
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (intentIsRegistered) {
            unregisterReceiver(wifiEventReceiver);
            intentIsRegistered = false;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!intentIsRegistered) {
            registerReceiver(wifiEventReceiver, filter);
            intentIsRegistered = true;
        }
    }
}