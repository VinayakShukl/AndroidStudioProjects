package com.example.logintester;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private Button loginButton, againButton;
    public static DefaultHttpClient httpClient;
    public static URL loginURL, againURL;
    public String sessionID;
    SharedPreferences pref;
    SharedPreferences.Editor editor;// = pref.edit();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            loginURL = new URL("http://192.168.52.112:8000/login/");
            againURL = new URL("http://192.168.52.112:8000/test_login/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        MainActivity.httpClient = new DefaultHttpClient();
        pref = getApplicationContext().getSharedPreferences("sessionPref", 0);
        editor = pref.edit();
        editor.putString("email", "a@bc.com");

        addListeners();
        setListeners();

    }

    public void addListeners() {
        loginButton = (Button) findViewById(R.id.loginButton);
        againButton = (Button) findViewById(R.id.againButton);
    }

    public void setListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new loginTask().execute();
            }

        });

        againButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new againTask().execute();
            }

        });
    }

    public void storeSessionInfo(String args) {
        editor.putString("sessionID", args);
        editor.commit();
    }


    private class againTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            httpClient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(againURL.toString());
            String sessionID = pref.getString("sessionID", null);
            httpget.setHeader("Cookie", "sessionid=" + sessionID);
            HttpResponse httres = null;

            try {
                httres = httpClient.execute(httpget);
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert httres != null;
            HttpEntity httpent = httres.getEntity();

            if (httres.getStatusLine().getStatusCode() == 403) {
                System.out.println("Session expired");
                return false;
            }
            try {
                System.out.println(EntityUtils.toString(httpent));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (!aBoolean)
                new loginTask().execute();
        }
    }


    private class loginTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", "a@bc.com"));
            WifiManager manager = (WifiManager) getSystemService(getApplicationContext().WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            String mac_address = info.getMacAddress();
            params.add(new BasicNameValuePair("password", mac_address));
            UrlEncodedFormEntity ent = null;

            try {
                ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            HttpPost httppost = new HttpPost(loginURL.toString());
            httppost.setEntity(ent);
            HttpResponse httres = null;

            CookieStore cookieStore = new BasicCookieStore();
            HttpContext localContext = new BasicHttpContext();
            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            try {
                httres = httpClient.execute(httppost, localContext);
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert httres != null;
            HttpEntity httpent = httres.getEntity();

            if (httres.getStatusLine().getStatusCode() == 403) {
                System.out.println("ERROR");
                finish();
            } else if (ent != null) {
                List<Cookie> cookies = cookieStore.getCookies();
                storeSessionInfo(cookies.get(0).getValue());
            }

            try {
                System.out.println(EntityUtils.toString(httpent));
                sessionID = httres.getFirstHeader("Set-Cookie").getValue();
                System.out.println(sessionID);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
