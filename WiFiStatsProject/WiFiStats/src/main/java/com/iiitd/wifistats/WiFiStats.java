package com.iiitd.wifistats;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WiFiStats extends Activity {


    WifiManager wifi;
    List<ScanResult> scanResults;
    ArrayList<Map<String, String>> list;
    SimpleAdapter adapter;
    ListView listview;
    ToggleButton toggleButton;
    Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        list = new ArrayList<Map<String, String>>();

        if(!wifi.isWifiEnabled()){
            Log.e("DEBUG", "Turning on wifi...");
            Toast.makeText(getApplicationContext(), "Enabling Wifi...",
                    Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        } else {
            Log.e("DEBUG", "Wifi is on...");
        }
        carryON();
    }

    private void carryON(){
        scanButton = (Button)findViewById(R.id.buttonScan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wifi = (WifiManager) v.getContext().getSystemService(Context.WIFI_SERVICE);
                if (!wifi.startScan()) {
                    Log.e("Error","Scanning could not start");
                } else {
                    Log.e("DEBUG", "Scanning has started...");
                    Toast.makeText(getApplicationContext(), "Scanning...", Toast.LENGTH_SHORT).show();
                }
                scanResults = wifi.getScanResults();
                Collections.sort(scanResults, new Comparator<ScanResult>() {
                    @Override
                    public int compare(ScanResult lhs, ScanResult rhs) {
                        return (lhs.level > rhs.level ? -1 : (lhs.level == rhs.level ? 0 : 1));
                    }
                });
                list.clear();
                list = buildData(scanResults);
                adapter = new SimpleAdapter(v.getContext(), list, R.layout.listitem, new String[]{"BSSID", "strength", "SSID"}, new int[] {R.id.BSSID, R.id.strength, R.id.SSID});
                listview = (ListView) findViewById(R.id.list);
                listview.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });

        toggleButton = (ToggleButton)findViewById(R.id.toggle);
        toggleButton.setChecked(wifi.isWifiEnabled());

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Log.e("DEBUG", "Turning WiFi on...");
                    Toast.makeText(getApplicationContext(), "Turning WiFi on...", Toast.LENGTH_SHORT).show();
                }else{
                    Log.e("DEBUG", "Turning WiFi off...");
                    Toast.makeText(getApplicationContext(), "Turning WiFi off...", Toast.LENGTH_SHORT).show();
                }
                wifi.setWifiEnabled(isChecked);
            }
        });
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
        item.put("strength", Integer.toString(level));
        return item;
    }
}
