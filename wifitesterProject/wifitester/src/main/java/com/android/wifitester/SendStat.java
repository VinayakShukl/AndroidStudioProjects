package com.android.wifitester;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class SendStat extends Activity {

    private static final int RESULT_SETTINGS = 1;
    private Spinner crowdSpinner, buildingSpinner, floorSpinner, IDSpinner;
    private Button sendButton;
    private String buildingSelected = new String();
    private String floorSelected = new String();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statform);

        addListeners();
        setListener();

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
    }

    public void addListeners() {

        crowdSpinner = (Spinner) findViewById(R.id.crowdSpinner);
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
                alert.setMessage("Crowd Strength: " + String.valueOf(crowdSpinner.getSelectedItem()) +
                        "\nBuilding: " + String.valueOf(buildingSpinner.getSelectedItem()) +
                        "\nFloor: " + String.valueOf(floorSpinner.getSelectedItem()) +
                        "\nID: " + String.valueOf(IDSpinner.getSelectedItem())
                );
                alert.setPositiveButton("POST", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            new POSTAsync().execute(createJSON());
                            //Toast.makeText(getApplicationContext(), "Sending POST", Toast.LENGTH_SHORT).show();
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

    public void setListener() {
        buildingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                buildingSelected = parentView.getItemAtPosition(position).toString();
                selectFloorSpinner(buildingSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // define custom style on building and crowd spinners
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.spinner_item, Arrays.asList(
                getResources().getStringArray(R.array.crowd_strength)));
        adapter.setDropDownViewResource(R.layout.spinner_item);
        crowdSpinner.setAdapter(adapter);

        adapter = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.spinner_item, Arrays.asList(
                getResources().getStringArray(R.array.building_array)));
        adapter.setDropDownViewResource(R.layout.spinner_item);
        buildingSpinner.setAdapter(adapter);

        floorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                floorSelected = parentView.getItemAtPosition(position).toString();
                selectIDSpinner(buildingSelected, floorSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    public void selectFloorSpinner(String buildingSelected) {
        ArrayAdapter<String> adapter2;
        String[] buildingString = new String[25];
        if (buildingSelected.compareTo("Academic") == 0) {
            buildingString = getResources().getStringArray(R.array.academic_floor);
        } else if (buildingSelected.compareTo("Library") == 0) {
            buildingString = getResources().getStringArray(R.array.library_floor);
        } else if (buildingSelected.compareTo("Mess") == 0) {
            buildingString = getResources().getStringArray(R.array.mess_floor);
        } else if (buildingSelected.compareTo("Boys Hostel") == 0) {
            buildingString = getResources().getStringArray(R.array.boyshostel_floor);
        } else if (buildingSelected.compareTo("Girls Hostel") == 0) {
            buildingString = getResources().getStringArray(R.array.girlshostel_floor);
        }
        adapter2 = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.spinner_item, Arrays.asList(buildingString));
        adapter2.setDropDownViewResource(R.layout.spinner_item);
        floorSpinner.setAdapter(adapter2);
    }

    public void selectIDSpinner(String buildingSelected, String floorSelected) {    //Floor reserved for future
        ArrayAdapter<String> adapter2;
        String[] IDString = new String[25];

        if (buildingSelected.compareTo("Academic") == 0) {
            if (floorSelected.compareTo("Ground") == 0) {
                IDString = getResources().getStringArray(R.array.Academic_Ground_IDs);
            } else if (floorSelected.compareTo("1st") == 0) {
                IDString = getResources().getStringArray(R.array.Academic_First_IDs);
            } else if (floorSelected.compareTo("2nd") == 0) {
                IDString = getResources().getStringArray(R.array.Academic_Second_IDs);
            } else if (floorSelected.compareTo("3rd") == 0) {
                IDString = getResources().getStringArray(R.array.Academic_Third_IDs);
            } else if (floorSelected.compareTo("4th") == 0) {
                IDString = getResources().getStringArray(R.array.Academic_Fourth_IDs);
            } else if (floorSelected.compareTo("5th") == 0) {
                IDString = getResources().getStringArray(R.array.Academic_Fifth_IDs);
            }
        } else if (buildingSelected.compareTo("Library") == 0) {
            IDString = getResources().getStringArray(R.array.Library_IDs);
        } else if (buildingSelected.compareTo("Mess") == 0) {
            IDString = getResources().getStringArray(R.array.Mess_IDs);
        } else if (buildingSelected.compareTo("Girls Hostel") == 0) {
            IDString = getResources().getStringArray(R.array.Hostel_IDs);
        } else if (buildingSelected.compareTo("Boys Hostel") == 0) {
            IDString = getResources().getStringArray(R.array.Hostel_IDs);
        }
        adapter2 = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.spinner_item, Arrays.asList(
                IDString));
        adapter2.setDropDownViewResource(R.layout.spinner_item);
        IDSpinner.setAdapter(adapter2);
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

        // Location tag
        JSONObject location = new JSONObject();
        location.put("Building", String.valueOf(buildingSpinner.getSelectedItem()));
        location.put("Floor", String.valueOf(floorSpinner.getSelectedItem()));
        location.put("ID", String.valueOf(IDSpinner.getSelectedItem()));
        location.put("Occupancy", String.valueOf(crowdSpinner.getSelectedItem()));

        // Readings tag
        String BSSID;
        String SSID;
        String Strength;
        JSONObject jsonRead;
        JSONObject jsonReading = new JSONObject();
        Map<String, String> reading;
        ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) getIntent().getSerializableExtra("scanResult");
        if (list == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(SendStat.this);
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
            postData(json[0]);
            return null;
        }

        int code;

        public void postData(JSONObject json) {
            HttpClient httpclient = new DefaultHttpClient();
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
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
                //Toast.makeText(getApplicationContext(), Integer.toString(code), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), "Return code: " + Integer.toString(code), Toast.LENGTH_SHORT).show();
        }
    }
}
