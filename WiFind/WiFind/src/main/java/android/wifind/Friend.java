package android.wifind;

import java.util.ArrayList;

/**
 * Created by Romil on 19/11/13.
 */
public class Friend {
    public String name;
    public String location;
    public String time;
    public String date;

    public Friend(String name, String loc){
        this.name = name;
        this.location = loc;
        this.time = "";
        this.date = "";
    }

    public Friend(String name, String loc, String time, String date){
        this.name = name;
        this.location = loc;
        this.time = time;
        this.date = date;
    }

    public void updateTimeDate(String time, String date){
        this.time = time;
        this.date = date;
    }

    public static ArrayList<Friend> CreateFriendArray (String[] names, String[] locs, String[] times, String[] dates){
        ArrayList<Friend> friends = new ArrayList<Friend>();
        for (int i=0; i<names.length; i++){
            friends.add(new Friend(names[i], locs[i], times[i], dates[i]));
        }
        return friends;
    }
}
