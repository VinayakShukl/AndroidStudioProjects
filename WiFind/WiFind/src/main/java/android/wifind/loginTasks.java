package android.wifind;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
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

public class loginTasks {

    static SharedPreferences pref;
    static SharedPreferences.Editor editor;
    public static URL loginURL, againURL;

    private static void initializeVar(Context ctx) {
        pref = ctx.getSharedPreferences("sessionPref", 0);
        editor = pref.edit();

        try {
            loginURL = new URL("http://192.168.52.112:8000/login/");
            againURL = new URL("http://192.168.52.112:8000/test_login/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static String readSessionID(Context ctx) {
        initializeVar(ctx);
        return pref.getString("sessionID", null);
    }

    public static void storeSessionID(String args) {
        editor.putString("sessionID", args);
        editor.commit();
    }

    private static String readUserName(Context ctx) {
        initializeVar(ctx);
        return pref.getString("username", null);
    }

    private static String readMAC(Context ctx) {
        initializeVar(ctx);
        return pref.getString("password", null);
    }

    public static void storeEmailMAC(Context ctx, String[] args) {
        initializeVar(ctx);
        editor.putString("username", args[0]);
        editor.putString("password", args[1]);
        editor.commit();
    }


    public static abstract class WiFindAsync extends AsyncTask<Void, Void, Boolean> {
        protected HttpClient httpClient;
        protected URL url;
        protected Context ctx;
        protected HttpGet httpGet;
        protected HttpResponse httpRes;

        public WiFindAsync(String url, Context ctx) {
            try {
                this.url = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            this.httpClient = new DefaultHttpClient();
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            initializeVar(ctx);
            httpRes = null;
            httpGet = new HttpGet(url.toString());
            String sessionID = readSessionID(ctx);
            httpGet.setHeader("Cookie", "sessionid=" + sessionID);
            System.out.println("Header set as: " + sessionID);
        }
    }

    public static class loginTask extends AsyncTask<Void, Void, Void> {

        private Context ctx;

        public loginTask(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            initializeVar(ctx);
            List<NameValuePair> params = new ArrayList<NameValuePair>();


            String userName = readUserName(ctx);
            String mac_address = readMAC(ctx);
            //"a@bc.com", "30:17:c8:e9:3d:19"
            params.add(new BasicNameValuePair("username", userName));
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
            HttpClient httpClient = new DefaultHttpClient();

            try {
                httres = httpClient.execute(httppost, localContext);
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert httres != null;
            HttpEntity httpent = httres.getEntity();

            if (httres.getStatusLine().getStatusCode() == 403) {
                System.out.println("ERROR");
            } else if (ent != null) {
                List<Cookie> cookies = cookieStore.getCookies();
                storeSessionID(cookies.get(0).getValue());
            }

            try {
                System.out.println(EntityUtils.toString(httpent));
                String sessionID = httres.getFirstHeader("Set-Cookie").getValue();
                System.out.println(sessionID);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

//    private class tryGet extends WiFindAsync {
//
//        public tryGet(String url, Context ctx) {
//            super(url, ctx);
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... voids) {
//            try {
//                httpRes = httpClient.execute(httpGet);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            assert httpRes != null;
//            HttpEntity httpReply = httpRes.getEntity();
//            if (httpRes.getStatusLine().getStatusCode() == 403) {
//                // call loginTask in onPostExecute()
//                System.out.println("Session expired");
//                return false;
//            }
//
//            try {
//                //  do something with returned data
//                System.out.println("Returned data: " + EntityUtils.toString(httpReply));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean successfulLogin) {
//            super.onPostExecute(successfulLogin);
//            if (!successfulLogin)
//                new loginTask(ctx).execute();
//        }
//    }
}
