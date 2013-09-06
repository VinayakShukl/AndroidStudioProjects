package com.android.wifitester;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    WifiManager mainWifi;
    IntentFilter filter;
    Intent startSendStat;
    List<ScanResult> scanResults;
    ArrayList<Map<String, String>> list;
    SimpleAdapter adapter;
    ListView listview;
    Button scanButton;
    Button createStat;
    boolean intentIsRegistered = false;

    public List<ScanResult> getCurrentScanResults() {
        return scanResults;
    }

    private BroadcastReceiver wifiEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("DEBUG", "Update received!");
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                scanResults = mainWifi.getScanResults();
                list = new ArrayList<Map<String, String>>();
                Collections.sort(scanResults, new Comparator<ScanResult>() {
                    @Override
                    public int compare(ScanResult lhs, ScanResult rhs) {
                        return (lhs.level > rhs.level ? -1 : (lhs.level == rhs.level ? 0 : 1));
                    }
                });
                list.clear();
                list = buildData(scanResults);
                updateList(context);
            }
        }
    };

    private void updateList(Context context) {
        adapter = new SimpleAdapter(context, list, R.layout.listitem, new String[]{"BSSID", "Strength", "SSID"}, new int[]{R.id.BSSID, R.id.strength, R.id.SSID});
        listview = (ListView) findViewById(R.id.listView);
        listview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private ArrayList<Map<String, String>> buildData(java.util.List<ScanResult> s) {
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (ScanResult result : s) {
            list.add(putData(result.BSSID, result.SSID, result.level));
        }
        return list;
    }

    private HashMap<String, String> putData(String BSSID, String SSID, int level) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("BSSID", BSSID);
        item.put("SSID", SSID);
        item.put("Strength", Integer.toString(level));
        return item;
    }

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton = (Button) findViewById(R.id.buttonScan);
        createStat = (Button) findViewById(R.id.createStat);
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.registerReceiver(wifiEventReceiver, filter);
        intentIsRegistered = true;

        if (!mainWifi.isWifiEnabled()) {
            Log.e("DEBUG", "turning on wifi");
            Toast.makeText(getApplicationContext(), "Enabling Wifi...",
                    Toast.LENGTH_LONG).show();
            mainWifi.setWifiEnabled(true);
        } else {
            Log.e("DEBUG", "wifi is on");
        }

        addListenerOnButton();
    }

    public void addListenerOnButton() {
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!mainWifi.startScan()) {
                    Log.e("Error", "Scanning could not start");
                    Toast.makeText(getApplicationContext(), "Scanning could not start", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("DEBUG", "Scanning has started...");
                }
            }
        });

        createStat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startSendStat = new Intent(MainActivity.this, SendStat.class);
                startSendStat.putExtra("scanResult", list);
                startActivity(startSendStat);
            }
        });
    }

    ;

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