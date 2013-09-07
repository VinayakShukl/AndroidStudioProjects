package com.android.wifitester;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static android.preference.Preference.OnPreferenceClickListener;


public class SettingsActivity extends PreferenceActivity {

    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        testButtonListner();
        resetButtonListner();
    }

    public void testButtonListner() {
        Preference testButton = (Preference) getPreferenceManager().findPreference("testURL");
        if (testButton != null) {
            final SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            testButton.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                String urlString = sharedPrefs.getString("prefBackend", "NULL");

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
                    alert.setTitle("Test");
                    Boolean isReachable;
                    isReachable = null;
                    try {
                        isReachable = new TestAsync().execute(urlString).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    if (isReachable) {
                        alert.setMessage(urlString + " is reachable!");
                        alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        alert.show();
                    } else {
                        alert.setMessage(urlString + " is not reachable");
                        alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        alert.setPositiveButton("Reset Default", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PreferenceManager
                                        .getDefaultSharedPreferences(getApplicationContext())
                                        .edit()
                                        .clear()
                                        .commit();
                                PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings, true);
                                String URL = sharedPrefs.getString("prefBackend", "NULL");
                                Log.e("DEBUG", "URL set to: " + URL);
                            }
                        });
                        alert.show();
                    }
                    return true;
                }
            });
        }
    }

    public void resetButtonListner() {
        Preference resetButton = (Preference) getPreferenceManager().findPreference("resetURL");
        if (resetButton != null) {
            final SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            resetButton.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .clear()
                            .commit();
                    PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings, true);
                    AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
                    alert.setTitle("Reset");
                    alert.setMessage("URL set to " + sharedPrefs.getString("prefBackend", "NULL"));
                    alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                        }
                    });
                    alert.show();
                    return true;
                }
            });
        } else {
            Log.e("DEBUG", "resetButton not set!");
        }
    }

    public class TestAsync extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... urlString) {
            return URLIsReachable(urlString[0]);
        }

        public boolean URLIsReachable(String urlString) {
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url
                        .openConnection();
                urlConnection.setConnectTimeout(1000);
                int responseCode = urlConnection.getResponseCode();
                urlConnection.disconnect();
                Log.e("DEBUG", "Trying connection to " + url + "Response " + Integer.toString(responseCode));
                return responseCode == 200;
            } catch (java.net.SocketTimeoutException e) {
                return false;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
