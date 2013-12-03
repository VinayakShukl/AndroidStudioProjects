package android.wifind;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ListView;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class PendingRequests extends ActionBarActivity {

    private class getPendingRequests extends loginTasks.WiFindAsync {

        String JSONStr;

        public getPendingRequests(String url, Context ctx) {
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
                //System.out.println("Returned data: " + EntityUtils.toString(httpReply));
                JSONStr = EntityUtils.toString(httpReply);
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
            else{

                try {
                    JSONObject jsonObject = new JSONObject(JSONStr);
                    System.out.print("JSON IS " + JSONStr);
                    JSONArray jsonNameArray = jsonObject.getJSONArray("usernames");
                    ArrayList<String> names, locs, times, dates;
                    names = new ArrayList<String>();
                    for(int i=0;i<jsonNameArray.length();i++)
                    {
                        names.add((String) jsonNameArray.get(i).toString());
                    }
                    frndarr = Friend.CreatePublicUsersArray(names);
                    frndadp= new FriendAdapter(PendingRequests.this, frndarr);
                    ListView lv = frndfrag.getListView();
                    lv.setAdapter(frndadp);
                    frndfrag.updateFrag(frndarr,frndadp);
                    frndadp.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void getPendingReqs(){
        String url = "http://192.168.52.112:8000/pending_requests/";
        getPendingRequests PendingList = new getPendingRequests(url, PendingRequests.this);
        PendingList.execute();
    }


    ArrayList<Friend> frndarr = new ArrayList<Friend>();
    FriendAdapter frndadp;
    PendingFriendListFrag frndfrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Pending Requests");
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        //frndarr.add(new Friend("Jatin Sindhu", ""));
        //frndarr.add(new Friend("Akshay Sharma", ""));
        //frndarr.add(new Friend("Amol Verma", ""));

        frndfrag= new PendingFriendListFrag(0, this, frndarr);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, frndfrag)
                    .commit();
        }
        frndadp=frndfrag.getAdapter();

        getPendingReqs();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pending_requests, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }
}
