package com.iiitd.wifind;

import android.app.Activity;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends ActionBarActivity {


    ArrayList<Friend> frndarr = new ArrayList<Friend>();
    FriendAdapter frndadp;
    FriendListFrag frndfrag;

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

        frndarr.add(new Friend("Romil Bhardwaj", "Acad Block 3rd Floor"));
        frndarr.add(new Friend("Jatin Sindhu", "Library 1st Floor"));
        frndarr.add(new Friend("Ankit Agarwal", "Library 1st Floor"));
        frndarr.add(new Friend("Vinayak Shukl", "Acad Block CCD"));
        frndarr.add(new Friend("Prateek Malhotra", "Acad Block 1st Floor"));

        frndfrag= new FriendListFrag(0, this, frndarr);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, frndfrag)
                    .commit();
        }

        frndadp=frndfrag.getAdapter();
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
        frndadp=frndfrag.getAdapter();
        switch (item.getItemId()) {
            case R.id.action_refresh:
                System.out.println("Refreshing");
                return true;
            case R.id.sortmenu_sortname:
                System.out.println("NameSort");
                Collections.sort(frndarr, new Comparator<Friend>() {
                    @Override
                    public int compare(Friend friend, Friend friend2) {
                        return friend.name.compareTo(friend2.name);
                    }
                });
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
                frndadp.notifyDataSetChanged();
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
            case R.id.sortmenu_sorttime:
                System.out.println("Sort by Time");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
