package com.android.wifitester;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;

public class StartActivity extends Activity {

    private Spinner buildingSpinner, floorSpinner, IDSpinner;
    private Button startButton, stopButton;
    private String buildingSelected = "";
    private String floorSelected = "";

    private Calendar cal;
    private Intent intent;
    private PendingIntent pintent;
    private static AlarmManager alarm;

    private ServiceReceiver myRecv;

    private static int postCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        addListeners();
        setListeners();
    }


    @Override
    protected void onStart() {
        super.onStart();
        intent = new Intent(StartActivity.this, SendService.class);
        //bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(myRecv);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: Receiver was already unregistered...\n");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(myRecv);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: Receiver was already unregistered...\n");
        }
    }

    public void addListeners() {
        buildingSpinner = (Spinner) findViewById(R.id.buildingSpinner);
        floorSpinner = (Spinner) findViewById(R.id.floorSpinner);
        IDSpinner = (Spinner) findViewById(R.id.IDSpinner);
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        IDSpinner.setScrollContainer(true);

        System.out.println("MAJOR TOGGLE");
        //stopButton.setEnabled(false);
    }


    private class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            postCount++;
            System.out.println("Count updated: " + postCount);
        }
    }

    public void setListeners() {
        // define custom style on building and crowd spinners
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                StartActivity.this,
                R.layout.spinner_item, Arrays.asList(
                getResources().getStringArray(R.array.building_array)));
        adapter.setDropDownViewResource(R.layout.spinner_item);
        buildingSpinner.setAdapter(adapter);

        // building spinner listener
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

        // floor spinner listener
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

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                postCount = 0;

                cal = Calendar.getInstance();
                intent = new Intent(StartActivity.this, SendService.class);
                intent.putExtra("building", String.valueOf(buildingSpinner.getSelectedItem()));
                intent.putExtra("floor", String.valueOf(floorSpinner.getSelectedItem()));
                intent.putExtra("id", String.valueOf(IDSpinner.getSelectedItem()));
                pintent = PendingIntent.getService(StartActivity.this, 0, intent, 0);

                myRecv = new ServiceReceiver();
                IntentFilter iFilter = new IntentFilter();
                iFilter.addAction(SendService.MY_ACTION);
                registerReceiver(myRecv, iFilter);

                alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 5 * 1000, pintent);
                Toast.makeText(StartActivity.this, "Service Started!", Toast.LENGTH_SHORT).show();
                //startButton.setEnabled(false);
                //stopButton.setEnabled(true);
            }
        });


        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Stopping\n");

                intent = new Intent(StartActivity.this, SendService.class);
                intent.putExtra("building", String.valueOf(buildingSpinner.getSelectedItem()));
                intent.putExtra("floor", String.valueOf(floorSpinner.getSelectedItem()));
                intent.putExtra("id", String.valueOf(IDSpinner.getSelectedItem()));
                pintent = PendingIntent.getService(StartActivity.this, 0, intent, 0);

                alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarm.cancel(pintent);
                pintent.cancel();

                try {
                    unregisterReceiver(myRecv);
                    Toast.makeText(StartActivity.this, "Service stopped! Results sent: " + postCount, Toast.LENGTH_LONG).show();
                } catch (IllegalArgumentException e) {
                    Toast.makeText(StartActivity.this, "Already stopped!", Toast.LENGTH_SHORT).show();
                    System.out.println("fERROR: REceiver was already unregistered...\n");
                }

                //stopButton.setEnabled(false);
                //startButton.setEnabled(true);
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
                StartActivity.this,
                R.layout.spinner_item, Arrays.asList(buildingString));
        adapter2.setDropDownViewResource(R.layout.spinner_item);
        floorSpinner.setAdapter(adapter2);
    }

    public void selectIDSpinner(String buildingSelected, String floorSelected) {    //Floor reserved for future
        ArrayAdapter<String> adapter2;
        String[] IDString = new String[25];

        if (buildingSelected.compareTo("Academic") == 0) {
            if (floorSelected.compareTo("0") == 0) {
                IDString = getResources().getStringArray(R.array.Academic_Ground_IDs);
            } else if (floorSelected.compareTo("1") == 0) {
                IDString = getResources().getStringArray(R.array.Academic_First_IDs);
            } else if (floorSelected.compareTo("2") == 0) {
                IDString = getResources().getStringArray(R.array.Academic_Second_IDs);
            } else if (floorSelected.compareTo("3") == 0) {
                IDString = getResources().getStringArray(R.array.Academic_Third_IDs);
            } else if (floorSelected.compareTo("4") == 0) {
                IDString = getResources().getStringArray(R.array.Academic_Fourth_IDs);
            } else if (floorSelected.compareTo("5") == 0) {
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
                StartActivity.this,
                R.layout.spinner_item, Arrays.asList(
                IDString));
        adapter2.setDropDownViewResource(R.layout.spinner_item);
        IDSpinner.setAdapter(adapter2);
    }
}
