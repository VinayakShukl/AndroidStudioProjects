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

    public Friend(String name){
        this.name = name;
        this.location = "";
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

    public static ArrayList<Friend> CreateFriendArray (ArrayList<String> names, ArrayList<String>  locs, ArrayList<String> times, ArrayList<String> dates){
        ArrayList<Friend> friends = new ArrayList<Friend>();
        for (int i=0; i<names.size(); i++){
            friends.add(new Friend(names.get(i), locs.get(i), times.get(i), dates.get(i)));
        }
        return friends;
    }

    public static ArrayList<Friend> CreatePublicUsersArray (ArrayList<String> names){
        ArrayList<Friend> friends = new ArrayList<Friend>();
        for (int i=0; i<names.size(); i++){
            friends.add(new Friend(names.get(i)));
        }
        return friends;
    }
}
