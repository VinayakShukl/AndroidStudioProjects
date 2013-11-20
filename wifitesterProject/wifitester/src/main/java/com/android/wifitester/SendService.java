package com.android.wifitester;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendService extends Service {
    private final IBinder myBinder = new LocalServiceBinder();
    private String building;
    private String floor;
    private String ID;
    List<ScanResult> scanResults;
    IntentFilter filter;
    WifiManager mainWifi;
    private ArrayList<Map<String, String>> list;

    final static String MY_ACTION = "MY_ACTION";

    private BroadcastReceiver wifiEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("@Service: Update received!");
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                scanResults = mainWifi.getScanResults();
                list = new ArrayList<Map<String, String>>();
                list.clear();
                list = buildData(scanResults);
                sendData();
            }
        }
    };

    private ArrayList<Map<String, String>> buildData(java.util.List<ScanResult> s) {
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (ScanResult result : s) {
            list.add(putData(result.BSSID, result.level));
        }
        return list;
    }

    private HashMap<String, String> putData(String BSSID, int level) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("BSSID", BSSID);
        item.put("Strength", Integer.toString(level));
        return item;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        System.out.println("@Service: Bounded!");
        return myBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("@Service: Starting...!");
        building = intent.getStringExtra("building");
        floor = intent.getStringExtra("floor");
        ID = intent.getStringExtra("id");

        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.registerReceiver(wifiEventReceiver, filter);
        System.out.println("Filter registered...");

        mainWifi.startScan();
        return 0;
    }


    @Override
    public void onDestroy() {
        System.out.println("@Service: Being destroyed...");
        //unregisterReceiver(wifiEventReceiver);
    }


    public void sendData() {
        this.unregisterReceiver(wifiEventReceiver);

        System.out.println("Unregistered");
        System.out.println(list.toString());
        JSONObject json = new JSONObject();
        try {
            json = createJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (json == null) {
            System.out.println("NULL returned");
        } else {
            System.out.println(json.toString());
            new POSTAsync().execute(json);
        }

        stopSelf();
    }

    private JSONObject createJSON() throws JSONException {
        JSONObject json = new JSONObject();

        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();

        // Device tag
        JSONObject device = new JSONObject();
        device.put("Name", android.os.Build.MODEL);
        device.put("OS Build", android.os.Build.VERSION.RELEASE);
        device.put("MAC", info.getMacAddress());

        // Location tag
        JSONObject location = new JSONObject();
        location.put("Building", building);
        location.put("Floor", floor);
        location.put("ID", ID);

        // Readings tag
        String BSSID;
        String Strength;
        JSONObject jsonReading = new JSONObject();
        Map<String, String> reading;
        ArrayList<Map<String, String>> list = this.list;
        if (list == null) {
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            reading = list.get(i);
            BSSID = reading.get("BSSID");
            Strength = reading.get("Strength");
            jsonReading.put(BSSID, Strength);
            //jsonReading.put(Integer.toString(i + 1), jsonRead);
        }

        json.put("Time", System.currentTimeMillis() / 1000L);
        json.put("Device", device);
        json.put("Location", location);
        json.put("Readings", jsonReading);
        return json;
    }


    private class POSTAsync extends AsyncTask<JSONObject, Void, String> {
        @Override
        protected String doInBackground(JSONObject... json) {
            if (json[0] == null) {
                Log.e("DEBUG", "Empty JSON!");
                return null;
            }
            try {
                postData(json[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

        int code;
        String resLoc;

        public void postData(JSONObject json) throws MalformedURLException {
            HttpClient httpclient = new DefaultHttpClient();
            URL url = new URL("http://www.google.co.in/");
            url = new URL(getString(R.string.defaultURL));
            System.out.println("Sending POST to: " + url.toString());
            HttpPost httpPost = new HttpPost(url.toString());
            try {
                StringEntity s = new StringEntity(json.toString());
                s.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(s);
                HttpResponse response = httpclient.execute(httpPost);
                code = response.getStatusLine().getStatusCode();
                if (code == 200) {
                    Intent i = new Intent();
                    i.setAction(MY_ACTION);
                    sendBroadcast(i);
                }
                //resLoc = EntityUtils.toString(response.getEntity());
                Log.e("DEBUG", "Response code for POST " + Integer.toString(code));
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
            super.onPostExecute(result);
            //`Toast.makeText(getApplicationContext(), "Loc: " + resLoc, Toast.LENGTH_SHORT).show();
        }
    }

    public class LocalServiceBinder extends Binder {
        SendService getService() {
            return SendService.this;
        }
    }
}
