package android.wifind;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Romil on 2/12/13.
 */

/**
 * A placeholder fragment containing a simple view.
 */
public class AddFriendListFrag extends ListFragment implements AdapterView.OnItemClickListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
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
    public AddFriendListFrag(int sectionNumber, Activity activity, ArrayList<Friend> frndarr) {
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        this.activity=activity;
        this.frndarr=frndarr;
    }

    public AddFriendListFrag() {

    }

    public FriendAdapter getAdapter(){
        return frndadp;
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String s = "Do you want to add "+ frndarr.get(i).name + " as a friend?";
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Toast.makeText(getActivity().getBaseContext(),"Sent!", Toast.LENGTH_LONG).show();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(getActivity().getBaseContext(),"Not Sent!", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(s).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}