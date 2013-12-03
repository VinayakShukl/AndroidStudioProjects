package android.wifind;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class AddFriend extends ActionBarActivity implements SearchView.OnQueryTextListener,
        SearchView.OnCloseListener {

    ArrayList<Friend> frndarr;
    private ListView myList;
    private SearchView searchView;
    private SearchHelper mDbHelper;
    private SearchAdapter defaultAdapter;
    private ArrayList<String> nameList;


    private class getUserList extends loginTasks.WiFindAsync {

        String JSONStr;

        public getUserList(String url, Context ctx) {
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
            if (!successfulLogin) {
                new loginTasks.loginTask(ctx).execute();

            } else {

                try {
                    JSONObject jsonObject = new JSONObject(JSONStr);
                    JSONArray jsonNameArray = jsonObject.getJSONArray("usernames");
                    System.out.println(jsonNameArray.toString());
                    ArrayList<String> names, locs, times, dates;
                    names = new ArrayList<String>();
                    for (int i = 0; i < jsonNameArray.length(); i++) {
                        names.add((String) jsonNameArray.get(i).toString());
                    }
                    frndarr = Friend.CreatePublicUsersArray(names);
                    nameList.clear();
                    for (Friend frnd : frndarr) {
                        nameList.add(frnd.name);
                    }
                    defaultAdapter.notifyDataSetChanged();
                    mDbHelper = new SearchHelper(ctx);
                    mDbHelper.open();

                    //Clear all names
                    mDbHelper.deleteAllNames();

                    // Create the list of names which will be displayed on search

                    for (String name : nameList) {
                        mDbHelper.createList(name);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class addFriendReq extends loginTasks.WiFindAsync {

        String JSONStr;
        int retcode;

        public addFriendReq(String url, Context ctx) {
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
            retcode = httpRes.getStatusLine().getStatusCode();
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
            if (!successfulLogin) {
                new loginTasks.loginTask(ctx).execute();

            } else {
                System.out.println(retcode);
                if (retcode == 403) {
                    Toast toast = Toast.makeText(AddFriend.this, "Friend request from the user is pending. Please check your friend requests.", Toast.LENGTH_LONG);
                    toast.show();
                } else if (retcode == 400) {
                    Toast toast = Toast.makeText(AddFriend.this, "A request to the user has already been pending.", Toast.LENGTH_LONG);
                    toast.show();
                } else if (retcode == 200) {
                    Toast toast = Toast.makeText(AddFriend.this, "Friend Request Sent.", Toast.LENGTH_SHORT);
                    toast.show();
                } else if (retcode == 405) {
                    Toast toast = Toast.makeText(AddFriend.this, "Your Friendship already holds.", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(AddFriend.this, retcode, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    }

    public void sendReq(String name) {
        String url = "http://192.168.52.112:8000/friend_request/?username=" + name;
        addFriendReq frndReq = new addFriendReq(url, AddFriend.this);
        frndReq.execute();
    }

    public void addConfirmation(final String name) {
        String s = "Do you want to add " + name + " as a friend?";
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        sendReq(name);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //Toast.makeText(AddFriend.this,"Not Sent!", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(AddFriend.this);
        builder.setMessage(s).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    public void getAllUsers() {
        String url = "http://192.168.52.112:8000/all_users/";
        getUserList userListReq = new getUserList(url, AddFriend.this);
        userListReq.execute();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add Friend");
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        frndarr = new ArrayList<Friend>();

        nameList = new ArrayList<String>();
        for (Friend frnd : frndarr) {
            nameList.add(frnd.name);
        }

        //for simplicity we will add the same name for 20 times to populate the nameList view
        /*for (int i = 0; i < 20; i++) {
            nameList.add("Diana" + i);
        }*/

        //relate the listView from java to the one created in xml
        myList = (ListView) findViewById(R.id.addlist);

        //show the ListView on the screen
        // The adapter SearchAdapter is responsible for maintaining the data backing this nameList and for producing
        // a view to represent an item in that data set.
        defaultAdapter = new SearchAdapter(AddFriend.this, nameList);
        myList.setAdapter(defaultAdapter);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                addConfirmation(nameList.get(position));

            }
        });


        //prepare the SearchView
        //searchView = (SearchView) menu.findItem(R.id.frndsearch).getActionView();

        //Sets the default or resting state of the search field. If true, a single search icon is shown by default and
        // expands to show the text field and other buttons when pressed. Also, if the default state is iconified, then it
        // collapses to that state when the close button is pressed. Changes to this property will take effect immediately.
        //The default value is true.

        mDbHelper = new SearchHelper(this);
        mDbHelper.open();

        //Clear all names
        mDbHelper.deleteAllNames();

        // Create the list of names which will be displayed on search

        for (String name : nameList) {
            mDbHelper.createList(name);
        }

        getAllUsers();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    @Override
    public boolean onClose() {
        myList.setAdapter(defaultAdapter);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        displayResults(query + "*");
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!(newText.length() == 0)) {
            displayResults(newText + "*");
        } else {
            myList.setAdapter(defaultAdapter);
        }

        return false;
    }

    private void displayResults(String query) {

        Cursor cursor = mDbHelper.searchByInputText((query != null ? query : "@@@@"));

        if (cursor != null) {

            String[] from = new String[]{SearchHelper.COLUMN_NAME};

            // Specify the view where we want the results to go
            int[] to = new int[]{R.id.frndlistname};

            // Create a simple cursor adapter to keep the search data
            SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.frndrow, cursor, from, to);
            myList.setAdapter(cursorAdapter);

            // Click listener for the searched item that was selected
            myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    // Get the cursor, positioned to the corresponding row in the result set
                    Cursor cursor = (Cursor) myList.getItemAtPosition(position);

                    // Get the state's capital from this row in the database.
                    String selectedName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    System.out.println("Selected " + selectedName);
                    Toast.makeText(AddFriend.this, selectedName, 0).show();

                    addConfirmation(selectedName);

                    // Set the default adapter
                    myList.setAdapter(defaultAdapter);

                    // Find the position for the original list by the selected name from search
                    for (int pos = 0; pos < nameList.size(); pos++) {
                        if (nameList.get(pos).equals(selectedName)) {
                            position = pos;
                            break;
                        }
                    }

                    // Create a handler. This is necessary because the adapter has just been set on the list again and
                    // the list might not be finished setting the adapter by the time we perform setSelection.
                    Handler handler = new Handler();
                    final int finalPosition = position;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            myList.setSelection(finalPosition);
                        }
                    });

                    searchView.setQuery("", true);
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_friend, menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Get the SearchView and set the searchable configuration
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView = (SearchView) menu.findItem(R.id.frndsearch).getActionView();
            // Assumes current activity is the searchable activity
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
            searchView.setOnQueryTextListener(this);
            searchView.setOnCloseListener(this);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        System.out.println("ADDFRIENDOPTIONS");
        switch (item.getItemId()) {
            case R.id.frndsearch:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
