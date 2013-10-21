package com.iiitd.wifind;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WiFindService extends Service {


    List<ScanResult> scanResults;
    WifiManager mainWifi;
    ArrayList<Map<String, String>> list;
    IntentFilter filter;

    protected BroadcastReceiver wifiEventReceiver = new BroadcastReceiver() {
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

                //Try to send data once we get the results
                try {
                    new POSTAsync().execute(createJSON());
                    //Toast.makeText(WiFindService.this, "Sending POST", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //updateList(context);
            }
        }
    };

    protected ArrayList<Map<String, String>> buildData(java.util.List<ScanResult> s) {
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (ScanResult result : s) {
            list.add(putData(result.BSSID, result.SSID, result.level));
        }
        return list;
    }

    protected HashMap<String, String> putData(String BSSID, String SSID, int level) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("BSSID", BSSID);
        item.put("SSID", SSID);
        item.put("Strength", Integer.toString(level));
        return item;
    }

    protected class POSTAsync extends AsyncTask<JSONObject, Void, String> {
        @Override
        protected String doInBackground(JSONObject... json) {
            if (json[0] == null) {
                Log.e("DEBUG", "Empty JSON!");
                return null;
            }
            postData(json[0]);
            return null;
        }

        int code;

        public void postData(JSONObject json) {
            HttpClient httpclient = new DefaultHttpClient();

            Toast.makeText(WiFindService.this, "Sending Data Now", Toast.LENGTH_SHORT).show();
            Log.e("DEBUG", "Sending data");
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(WiFindService.this);
            String URL = sharedPrefs.getString("prefBackend", "NULL");
            if (URL.equals("NULL")) {
                URL = String.valueOf(R.string.defaultURL);
            }
            HttpPost httpPost = new HttpPost(URL);
            try {
                StringEntity s = new StringEntity(json.toString());
                s.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(s);
                HttpResponse response = httpclient.execute(httpPost);
                code = response.getStatusLine().getStatusCode();
                //Toast.makeText(WiFindService.this, Integer.toString(code), Toast.LENGTH_SHORT).show();
                Log.e("DEBUG", "Response for POST " + Integer.toString(code));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            Toast.makeText(WiFindService.this, "Return code: " + Integer.toString(code), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendWifiInfo(){
        Toast.makeText(WiFindService.this, "Service running: Trying to send data!", Toast.LENGTH_SHORT).show();
        if (!mainWifi.startScan()) {
            Log.e("Error", "Scanning could not start");
            Toast.makeText(WiFindService.this, "Scanning could not start", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("DEBUG", "Scanning has started...");
        }
    }


    public JSONObject createJSON() throws JSONException {
        JSONObject json = new JSONObject();

        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();

        //  Misc tag
        //JSONObject misc = new JSONObject();
        json.put("Time", System.currentTimeMillis() / 1000L);

        // Device tag
        JSONObject device = new JSONObject();
        device.put("Name", android.os.Build.MODEL);
        device.put("OS Build", android.os.Build.VERSION.RELEASE);
        device.put("MAC", info.getMacAddress());

        // Readings tag
        String BSSID;
        String SSID;
        String Strength;
        JSONObject jsonRead;
        JSONObject jsonReading = new JSONObject();
        Map<String, String> reading;
        if (list == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(WiFindService.this);
            alert.setTitle("Oops!");
            alert.setMessage("The scan list is empty!");
            alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alert.show();
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            jsonRead = new JSONObject();
            reading = list.get(i);
            BSSID = reading.get("BSSID");
            SSID = reading.get("SSID");
            Strength = reading.get("Strength");
            jsonRead.put("BSSID", BSSID);
            jsonRead.put("SSID", SSID);
            jsonRead.put("Strength", Strength);
            jsonReading.put(Integer.toString(i + 1), jsonRead);
        }

        //json.put("Time", misc);
        json.put("Device", device);
        json.put("Readings", jsonReading);
        return json;
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, startId, startId);

        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.registerReceiver(wifiEventReceiver, filter);
        if (!mainWifi.isWifiEnabled()) {
            Log.e("DEBUG", "turning on wifi");
            Toast.makeText(WiFindService.this, "Enabling Wifi...",
                    Toast.LENGTH_LONG).show();
            mainWifi.setWifiEnabled(true);
        } else {
            Log.e("DEBUG", "wifi is on");
        }
        sendWifiInfo();
        return START_STICKY;

    }
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void onDestroy() {
        this.unregisterReceiver(wifiEventReceiver);
        super.onDestroy();
    }
}
