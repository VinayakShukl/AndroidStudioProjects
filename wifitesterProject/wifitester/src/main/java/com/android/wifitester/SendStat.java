package com.android.wifitester;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
        // Show the Up button in the action bar.
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
                //Toast.makeText(getApplicationContext(), buildingSelected, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        floorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String floorSelected = parentView.getItemAtPosition(position).toString();
                selectIDSpinner(floorSelected, buildingSpinner.getItemAtPosition((int) buildingSpinner.getSelectedItemId()).toString());
                //Toast.makeText(getApplicationContext(), buildingSelected, Toast.LENGTH_SHORT).show();
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

    public void selectIDSpinner(String floorSelected, String buildingSelected) {
        ArrayAdapter<String> adapter2;
        //Toast.makeText(getApplicationContext(), floorSelected+buildingSelected, Toast.LENGTH_SHORT).show();
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
                        postData();
                        Toast.makeText(getApplicationContext(), "POSTing...", Toast.LENGTH_SHORT).show();
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

    public void postData() {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://192.168.52.112:8000/testdata/");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        try {
            StringEntity s;
            try {
                s = new StringEntity(createJSON().toString());
                s.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            HttpResponse response = httpclient.execute(httpPost);
            //Toast.makeText(getApplicationContext(), response.getStatusLine().getStatusCode(), Toast.LENGTH_LONG).show();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
    }

    public JSONObject createJSON() throws JSONException {
        JSONObject json = new JSONObject();

        // Location tag
        JSONObject location = new JSONObject();
        location.put("Building", String.valueOf(buildingSpinner.getSelectedItem()));
        location.put("Floor", String.valueOf(floorSpinner.getSelectedItem()));
        location.put("ID", String.valueOf(IDSpinner.getSelectedItem()));

        // Readings tag
        String BSSID = new String();
        String SSID = new String();
        String Strength = new String();
        JSONObject jsonRead;
        JSONObject jsonReading = new JSONObject();
        Map<String, String> reading;
        ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) getIntent().getSerializableExtra("scanResult");
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

        json.put("Location", location);
        json.put("Readings", jsonReading);
        return json;
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
