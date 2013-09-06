package com.android.wifitester;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Created by Vinayak on 6/9/13.
 */
public class SendActivity extends Activity {
    private Spinner buildingSpinner, floorSpinner;
    private Button sendButton;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendform);

        addListenerOnButton();
    }

    public void addListenerOnButton() {

        buildingSpinner = (Spinner) findViewById(R.id.buildingSpinner);
        floorSpinner = (Spinner) findViewById(R.id.floorSpinner);
        sendButton = (Button) findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(),
                        "OnClickListener : " +
                                "\nSpinner 1 : " + String.valueOf(buildingSpinner.getSelectedItem()) +
                                "\nSpinner 2 : " + String.valueOf(floorSpinner.getSelectedItem()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}