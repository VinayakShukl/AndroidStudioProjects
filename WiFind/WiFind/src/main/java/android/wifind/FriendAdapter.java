package android.wifind;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Romil on 19/11/13.
 */
public class FriendAdapter extends ArrayAdapter<Friend> {

    private final Context context;
    private final ArrayList<Friend> friends;

    public FriendAdapter(Context context, ArrayList<Friend> friends) {
        super(context, R.layout.frndrow);
        this.context = context;
        this.friends = friends;
    }

    public int getCount() {
        System.out.println(friends.size());
        return friends.size();
    }

    @Override
    //get the data of an item from a specific position
    //i represents the position of the item in the list
    public Friend getItem(int i) {
        return friends.get(i);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View vi = convertView;

        System.out.println("In adapter!");
        if (vi == null)
            vi = inflater.inflate(R.layout.frndrow, null);
        TextView Name = (TextView) vi.findViewById(R.id.frndlistname);
        Name.setText(friends.get(position).name);
        TextView loc = (TextView) vi.findViewById(R.id.frndlistloc);
        loc.setText(friends.get(position).location);
        return vi;
    }
}
