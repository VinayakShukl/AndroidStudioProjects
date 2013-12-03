package android.wifind;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Romil on 2/12/13.
 */
/**
 * A placeholder fragment containing a simple view.
 */
public class FriendListFrag extends ListFragment {
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
    public FriendListFrag (int sectionNumber, Activity activity, ArrayList<Friend> frndarr) {
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
        this.activity=activity;
        this.frndarr=frndarr;
    }

    public FriendListFrag() {

    }

    public FriendAdapter getAdapter(){
        return frndadp;
    }


    public void updateCall(){
        frndadp.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        /** Creating an array adapter to store the list of countries **/
        frndadp = new FriendAdapter(inflater.getContext(), frndarr);

        /** Setting the list adapter for the ListFragment */
        setListAdapter(frndadp);


        return super.onCreateView(inflater, container, savedInstanceState);
    }
}