package android.wifind;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Romil on 2/12/13.
 */

/**
 * A placeholder fragment containing a simple view.
 */
public class PendingFriendListFrag extends ListFragment implements AdapterView.OnItemClickListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

    private class acceptPendingRequest extends loginTasks.WiFindAsync {

        String JSONStr;

        public acceptPendingRequest(String url, Context ctx) {
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
                Toast toast = Toast.makeText(getActivity().getBaseContext(), "Friend Request Accepted", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }


    private class declinePendingRequest extends loginTasks.WiFindAsync {

        String JSONStr;

        public declinePendingRequest(String url, Context ctx) {
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
                Toast toast = Toast.makeText(getActivity().getBaseContext(), "Friend Request Declined", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private Activity activity;
    private static final String ARG_SECTION_NUMBER = "section_number";
    ArrayList<Friend> frndarr = new ArrayList<Friend>();
    FriendAdapter frndadp;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setOnItemClickListener(this);
    }
    public PendingFriendListFrag(int sectionNumber, Activity activity, ArrayList<Friend> frndarr) {
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        this.activity=activity;
        this.frndarr=frndarr;
    }

    public void updateFrag(ArrayList<Friend> frndarr, FriendAdapter frndadp){
        this.frndarr=frndarr;
        this.frndadp = frndadp;
    }

    public PendingFriendListFrag() {

    }

    public FriendAdapter getAdapter(){
        return frndadp;
    }

    public void acceptRequest(String name){
        String url = "http://192.168.52.112:8000/accept_request/?username="+name;
        acceptPendingRequest acceptpending = new acceptPendingRequest(url, getActivity().getBaseContext());
        acceptpending.execute();
    }

    public void declineRequest(String name){
        String url = "http://192.168.52.112:8000/decline_request/?username="+name;
        declinePendingRequest declinepending = new declinePendingRequest(url, getActivity().getBaseContext());
        declinepending.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        /** Creating an array adapter to store the list of countries **/
        frndadp = new FriendAdapter(inflater.getContext(), frndarr);

        /** Setting the list adapter for the ListFragment */
        setListAdapter(frndadp);


        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
        String s = "Do you want to confirm "+ frndarr.get(i).name + " as a friend?";
        System.out.println("HelloWorld");
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        acceptRequest(frndarr.get(i).name);
                        frndarr.remove(i);
                        frndadp.notifyDataSetChanged();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        declineRequest(frndarr.get(i).name);
                        frndarr.remove(i);
                        frndadp.notifyDataSetChanged();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(s).setPositiveButton("Accept", dialogClickListener)
                .setNegativeButton("Reject", dialogClickListener).show();
    }
}