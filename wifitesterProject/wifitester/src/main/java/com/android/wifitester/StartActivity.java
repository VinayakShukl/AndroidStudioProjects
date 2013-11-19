package com.android.wifitester;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.wifitester.SendService.LocalServiceBinder;

import java.util.Arrays;

public class StartActivity extends Activity {

    private Spinner buildingSpinner, floorSpinner, IDSpinner;
    private Button startButton, stopButton;
    private String buildingSelected = "";
    private String floorSelected = "";
    private SendService myService;
    private boolean isBound;
    private Intent intent;

    private ServiceConnection myConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            System.out.println("Connected\n");
            LocalServiceBinder binder = (LocalServiceBinder) service;
            myService = binder.getService();
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            System.out.println("Disconnected\n");
            isBound = false;
        }

    };

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


    public void addListeners() {
        buildingSpinner = (Spinner) findViewById(R.id.buildingSpinner);
        floorSpinner = (Spinner) findViewById(R.id.floorSpinner);
        IDSpinner = (Spinner) findViewById(R.id.IDSpinner);
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        IDSpinner.setScrollContainer(true);

        System.out.println("MAJOR TOGGLE");
        stopButton.setEnabled(false);
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
                intent.putExtra("building", String.valueOf(buildingSpinner.getSelectedItem()));
                intent.putExtra("floor", String.valueOf(floorSpinner.getSelectedItem()));
                intent.putExtra("id", String.valueOf(IDSpinner.getSelectedItem()));
                if (!isBound) {
                    System.out.println("Binding...Wait for bounded.\n");
                    bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
                }
                startService(intent);
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });


        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    System.out.println("Stopping\n");
                    isBound = false;

                    Toast.makeText(StartActivity.this, "POST's sent: " + Integer.toString(myService.getCount()), Toast.LENGTH_LONG).show();

                    unbindService(myConnection);
                    stopService(intent);
                    stopButton.setEnabled(false);
                    startButton.setEnabled(true);
                }
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
