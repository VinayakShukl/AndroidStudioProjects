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
import android.view.Window;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_fullscreen);

        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

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
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();

            alertDialog.show();
        } else {
            new SendCodeTask().execute();
        }

    }

        /*
        new getEmailAsync(url, Activity.this).execute()

        private class getEmailAsync extends loginTasks.WiFindAsync {

            public getEmailAsync(String url, Context ctx) {
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
                if (httpRes.getStatusLine().getStatusCode() == 302) {
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
                if (!successfulLogin){
                    new loginTasks.loginTask(ctx).execute();

                }
            }
        }*/


    public static class getEmailAsync extends AsyncTask<Void, Void, String> {
        protected HttpClient httpClient;
        protected URL url;
        protected Context ctx;
        protected HttpGet httpGet;
        protected HttpResponse httpRes;

        public static String userName;

        public getEmailAsync(String url, Context ctx) {
            try {
                this.url = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            this.httpClient = new DefaultHttpClient();
            this.ctx = ctx;
            this.httpGet = new HttpGet(url);
            userName = null;
        }


        @Override
        protected String doInBackground(Void... voids) {
            try {
                System.out.println("Executing HTTP GET...");
                httpRes = httpClient.execute(httpGet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert httpRes != null;
            HttpEntity httpReply = httpRes.getEntity();
            userName = null;

            try {
                userName = EntityUtils.toString(httpReply);
                System.out.println(userName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return userName;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    class CheckMac extends AsyncTask<Void, Void, Integer> {

        int code;
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String mac = info.getMacAddress();
        String userName;

        final Integer SUCCESS = 0;
        final Integer NO_EMAIL = 1;
        final Integer NO_MAC = 2;

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                HttpGet sendMAC = new HttpGet("http://192.168.52.112:8000/existing_mac/?mac=" + mac);
                HttpResponse response = new DefaultHttpClient().execute(sendMAC);
                code = response.getStatusLine().getStatusCode();

                if (code == 200) {
                    userName = loginTasks.readUserName(FullscreenActivity.this);
                    if (userName == null) {

                        HttpClient httpClient;
                        URL url = null;
                        try {
                            url = new URL("http://192.168.52.112:8000/get_email/?mac=" + mac);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                        HttpGet httpGet;
                        httpClient = new DefaultHttpClient();
                        httpGet = new HttpGet(String.valueOf(url));
                        HttpResponse httpRes = httpClient.execute(httpGet);

                        assert httpRes != null;
                        HttpEntity httpReply = httpRes.getEntity();

                        try {
                            userName = EntityUtils.toString(httpReply);
                            System.out.println(userName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String[] args = {userName, mac};
                        loginTasks.storeEmailMAC(FullscreenActivity.this, args);
                        loginTasks.loginTaskHome myTask2 = new loginTasks.loginTaskHome(FullscreenActivity.this);
                        myTask2.execute();
                        return NO_EMAIL;
                    }
                    return SUCCESS;
                }
            } catch (Exception ignored) {
            }
            return NO_MAC;
        }

        @Override
        protected void onPostExecute(Integer status) {
            if (status.equals(NO_EMAIL)) {
               return;
            } else if (status.equals(SUCCESS)) {
                Thread timer = new Thread() {
                    public void run() {
                        try {
                            sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            Intent intent = new Intent(FullscreenActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                };
                timer.start();
            } else if (status.equals(NO_MAC)) {
                Thread timer = new Thread() {
                    public void run() {
                        try {
                            sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            Intent intent = new Intent(FullscreenActivity.this, UserCheck.class);
                            startActivity(intent);
                        }
                    }
                };
                timer.start();
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
                new CheckMac().execute();
            } else {
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

