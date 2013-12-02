package android.wifind;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.wifind.util.SystemUiHider;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {

    protected BroadcastReceiver wifiEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("DEBUG", "In onReceive");
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Log.e("DEBUG", "Network state change detected!");
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                assert networkInfo != null;
                if (networkInfo.isConnected()) {
                    System.out.println(networkInfo.getDetailedState());
                    Log.e("DEBUG", "Now connected to Wifi");
                    new SendCodeTask().execute();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);


        //Check if Wifi is enabled
        if (!wifi.isWifiEnabled()) {


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setTitle("Your Wifi is disabled");
            alertDialogBuilder.setMessage("Press Yes to enable the Wifi or Press No to exit the app");
            alertDialogBuilder.setCancelable(false);


            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    IntentFilter filter;
                    filter = new IntentFilter();
                    filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                    FullscreenActivity.this.registerReceiver(wifiEventReceiver, filter);
                    wifi.setWifiEnabled(true);
                }
            });

            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    FullscreenActivity.this.finish();
                    //dialog.cancel();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();

            alertDialog.show();
        } else {
            System.out.println("Calling tryGet...");
            //new tryGet("http://192.168.52.112:8000/test_login/", this).execute();
            new SendCodeTask().execute();
        }

    }

    /*
        private class tryGet extends loginTasks.WiFindAsync {

            public tryGet(String url, Context ctx) {
                super(url, ctx);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    System.out.println("Executing HTTP GET...");
                    httpRes = httpClient.execute(httpGet);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                assert httpRes != null;
                HttpEntity httpReply = httpRes.getEntity();
                if (httpRes.getStatusLine().getStatusCode() == 403) {
                    // call loginTask in onPostExecute()
                    System.out.println("Session expired");
                    return false;
                }

                try {
                    //  do something with returned data
                    System.out.println("Returned data: " + EntityUtils.toString(httpReply));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean successfulLogin) {
                super.onPostExecute(successfulLogin);
                if (!successfulLogin)
                    new loginTasks.loginTask(ctx).execute();
            }
        }
    */
    String reply;

    class CheckMac extends AsyncTask<Void, Void, Boolean> {

        int code;
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String mac = info.getMacAddress();

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                HttpGet sendMAC = new HttpGet("http://192.168.52.112:8000/?=" + mac);
                System.out.println("Sending GET");
                HttpResponse response = new DefaultHttpClient().execute(sendMAC);
                System.out.println("Response Received");
                code = response.getStatusLine().getStatusCode();
                reply = EntityUtils.toString(response.getEntity());
                if (code == 200) {
                    if (reply.equals("True"))
                        return true;
                }
            } catch (Exception ignored) {

            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean a) {
            System.out.println("2In postExecute");
            if (a) {
                //Registered User. Go to Homepage.
                Toast.makeText(FullscreenActivity.this, "Registered", Toast.LENGTH_LONG).show();
            } else {
                //Unregistered. Go register.
                Intent intent = new Intent(FullscreenActivity.this, UserCheck.class);
                startActivity(intent);
            }

        }
    }

    class SendCodeTask extends AsyncTask<Void, Void, Boolean> {

        int code;

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                HttpGet getStories = new HttpGet("http://192.168.52.112:8000/alive/");
                System.out.println("Sending GET");
                HttpResponse response = new DefaultHttpClient().execute(getStories);
                code = response.getStatusLine().getStatusCode();
                System.out.println("Response Received");

                if (code == 200) {
                    return true;
                }

            } catch (Exception ignored) {

            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean a) {
            System.out.println("1In postExecute");
            if (a) {
                //NOW IT IS CONFIRMED THAT THE APP IS CONNECTED TO PRATEEK'S SERVER.
                //NOW YOU NEED TO CHECK IF THE USER IS NEW OR NOT.
                System.out.println("1Code 200");
                new CheckMac().execute();
            } else {
                System.out.println("1Not 200");
                badConnection();
            }

        }
    }

    public void badConnection() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Error");
        alertDialogBuilder.setMessage("You are not connected to the local IIITD Network");
        alertDialogBuilder.setCancelable(false);


        alertDialogBuilder.setPositiveButton("Quit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                FullscreenActivity.this.finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();


    }

}

