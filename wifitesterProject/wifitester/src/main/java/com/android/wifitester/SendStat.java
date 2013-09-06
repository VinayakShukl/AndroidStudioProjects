package com.android.wifitester;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.Map;

public class SendStat extends Activity {

    private Spinner buildingSpinner, floorSpinner, IDSpinner;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statform);

        setupActionBar();
        addListeners();
        setListener();
    }

    public void setListener() {
        buildingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String buildingSelected = parentView.getItemAtPosition(position).toString();
                selectFloorSpinner(buildingSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        floorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String floorSelected = parentView.getItemAtPosition(position).toString();
                selectIDSpinner();
                //selectIDSpinner(floorSelected, buildingSpinner.getItemAtPosition((int) buildingSpinner.getSelectedItemId()).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    public void selectFloorSpinner(String buildingSelected) {
        ArrayAdapter<String> adapter2;
        if (buildingSelected.compareTo("Academic") == 0) {
            adapter2 = new ArrayAdapter<String>(
                    getApplicationContext(),
                    R.layout.spinner_item, Arrays.asList(
                    getResources().getStringArray(R.array.academic_floor)));
            adapter2.setDropDownViewResource(R.layout.spinner_item);
            floorSpinner.setAdapter(adapter2);
        } else if (buildingSelected.compareTo("Library") == 0) {
            adapter2 = new ArrayAdapter<String>(
                    getApplicationContext(),
                    R.layout.spinner_item, Arrays.asList(
                    getResources().getStringArray(R.array.library_floor)));
            adapter2.setDropDownViewResource(R.layout.spinner_item);
            floorSpinner.setAdapter(adapter2);
        } else {
            adapter2 = new ArrayAdapter<String>(
                    getApplicationContext(),
                    R.layout.spinner_item, Arrays.asList(
                    getResources().getStringArray(R.array.temp_vals)));
            adapter2.setDropDownViewResource(R.layout.spinner_item);
            floorSpinner.setAdapter(adapter2);
        }
    }

    public void selectIDSpinner() {
        ArrayAdapter<String> adapter2;
        adapter2 = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.spinner_item, Arrays.asList(
                getResources().getStringArray(R.array.temp_IDs)));
        adapter2.setDropDownViewResource(R.layout.spinner_item);
        IDSpinner.setAdapter(adapter2);
    }

    public void addListeners() {

        buildingSpinner = (Spinner) findViewById(R.id.buildingSpinner);
        floorSpinner = (Spinner) findViewById(R.id.floorSpinner);
        IDSpinner = (Spinner) findViewById(R.id.IDSpinner);
        sendButton = (Button) findViewById(R.id.sendButton);

        IDSpinner.setScrollContainer(true);
        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(SendStat.this);
                alert.setTitle("Confirm Submit");
                alert.setMessage("Building: " + String.valueOf(buildingSpinner.getSelectedItem()) +
                        "\nFloor: " + String.valueOf(floorSpinner.getSelectedItem()) +
                        "\nID: " + String.valueOf(IDSpinner.getSelectedItem())
                );
                alert.setPositiveButton("POST", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            new POSTAsync().execute(createJSON());
                            Toast.makeText(getApplicationContext(), "Sending POST", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                alert.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alert.show();
            }
        });
    }

    public JSONObject createJSON() throws JSONException {
        JSONObject json = new JSONObject();

        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();

        // Device tag
        JSONObject device = new JSONObject();
        device.put("Name", android.os.Build.MODEL);
        device.put("MAC", info.getMacAddress());

        // Location tag
        JSONObject location = new JSONObject();
        location.put("Building", String.valueOf(buildingSpinner.getSelectedItem()));
        location.put("Floor", String.valueOf(floorSpinner.getSelectedItem()));
        location.put("ID", String.valueOf(IDSpinner.getSelectedItem()));

        // Readings tag
        String BSSID;
        String SSID;
        String Strength;
        JSONObject jsonRead;
        JSONObject jsonReading = new JSONObject();
        Map<String, String> reading;
        ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) getIntent().getSerializableExtra("scanResult");
        assert list != null;
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

        json.put("Device", device);
        json.put("Location", location);
        json.put("Readings", jsonReading);
        return json;
    }

    private class POSTAsync extends AsyncTask<JSONObject, Void, String> {
        @Override
        protected String doInBackground(JSONObject... json) {
            postData(json[0]);
            return null;
        }

        public void postData(JSONObject json) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://prateek.no-ip.biz:8000/testdata/");
            try {
                StringEntity s = new StringEntity(json.toString());
                s.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(s);
                //HttpResponse response =
                httpclient.execute(httpPost);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_stat, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
