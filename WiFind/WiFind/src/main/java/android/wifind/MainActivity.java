package android.wifind;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends ActionBarActivity {


    ArrayList<Friend> frndarr = new ArrayList<Friend>();
    FriendAdapter frndadp;
    FriendListFrag frndfrag;


    private class getFriendsAsync extends loginTasks.WiFindAsync {

        String JSONStr;

        public getFriendsAsync(String url, Context ctx) {
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
                    System.out.println("Creating JSON Obj " + JSONStr);
                    JSONObject jsonObject = new JSONObject(JSONStr);
                    JSONArray jsonNameArray = jsonObject.getJSONArray("usernames");
                    JSONArray jsonLocArray = jsonObject.getJSONArray("locations");
                    JSONArray jsonTimeArray = jsonObject.getJSONArray("times");
                    JSONArray jsonDateArray = jsonObject.getJSONArray("dates");
                    ArrayList<String> names, locs, times, dates;
                    names = new ArrayList<String>();
                    locs = new ArrayList<String>();
                    times = new ArrayList<String>();
                    dates = new ArrayList<String>();
                    for(int i=0;i<jsonNameArray.length();i++)
                    {
                        System.out.println((String) jsonNameArray.get(i).toString());
                        names.add((String) jsonNameArray.get(i).toString());
                        locs.add((String) jsonLocArray.get(i).toString());
                        times.add((String) jsonTimeArray.get(i).toString());
                        dates.add((String) jsonDateArray.get(i).toString());
                    }
                    frndarr = Friend.CreateFriendArray(names, locs, times, dates);

                    //HACK
                    /*frndarr.add(new Friend("vinayak11118", "Academic - 2 - C21", "17:39", "02/12"));
                    frndarr.add(new Friend("utkarsh11117", "Library - 2", "19:31", "02/12"));
                    frndarr.add(new Friend("prakhar11074", "Mess - 1", "20:32", "02/12"));
                    frndarr.add(new Friend("ramon11074", "Academic - 1 - CCD", "16:52", "02/12"));
                    */
                    //HACK
                    frndadp= new FriendAdapter(MainActivity.this, frndarr);
                    ListView lv = frndfrag.getListView();
                    lv.setAdapter(frndadp);
                    frndadp.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

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

        /*frndarr.add(new Friend("Romil Bhardwaj", "Acad Block 3rd Floor"));
        frndarr.add(new Friend("Jatin Sindhu", "Library 1st Floor"));
        frndarr.add(new Friend("Ankit Agarwal", "Library 1st Floor"));
        frndarr.add(new Friend("Vinayak Shukl", "Acad Block CCD"));
        frndarr.add(new Friend("Prateek Malhotra", "Acad Block 1st Floor"));*/


        frndfrag= new FriendListFrag(0, this, frndarr);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, frndfrag)
                    .commit();
        }

        frndadp=frndfrag.getAdapter();

        getFriends();

    }

    public void getFriends(){
        getFriendsAsync getfrndsreq = new getFriendsAsync("http://192.168.52.112:8000/get_friends/", MainActivity.this);
        getfrndsreq.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        System.out.println("Inflating");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        System.out.println(item.getItemId());
        System.out.println("Onoptions");
        ListView lv = frndfrag.getListView();
        frndadp=frndfrag.getAdapter();
        switch (item.getItemId()) {
            case R.id.action_refresh:
                System.out.println("Refreshing");
                getFriends();
                return true;
            case R.id.sortmenu_sortname:
                System.out.println("NameSort");
                Collections.sort(frndarr, new Comparator<Friend>() {
                    @Override
                    public int compare(Friend friend, Friend friend2) {
                        return friend.name.compareTo(friend2.name);
                    }
                });
                frndadp= new FriendAdapter(MainActivity.this, frndarr);
                lv.setAdapter(frndadp);
                frndadp.notifyDataSetChanged();
                return true;
            case R.id.sortmenu_sortloc:
                System.out.println("LocSort");
                Collections.sort(frndarr, new Comparator<Friend>() {
                    @Override
                    public int compare(Friend friend, Friend friend2) {
                        return friend.location.compareTo(friend2.location);
                    }
                });
                frndadp= new FriendAdapter(MainActivity.this, frndarr);
                lv.setAdapter(frndadp);
                frndadp.notifyDataSetChanged();
                return true;
            case R.id.sortmenu_sorttime:
                System.out.println("LocSort");
                getFriends();
                return true;
            case R.id.action_addfriend:
                System.out.println("AddFrnd");
                Intent addfriendint = new Intent(this, AddFriend.class);
                startActivity(addfriendint);
                return true;
            case R.id.action_pendingrequests:
                System.out.println("PendingFrnds");
                Intent pendingfriendint = new Intent(this, PendingRequests.class);
                startActivity(pendingfriendint);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
