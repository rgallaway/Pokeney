package me.ryangallaway.pokeney;

import java.text.SimpleDateFormat;

/**
 * Created by Ryan Gallaway on 2/4/2018.
 */

public class Poke {
    public String user;
    public String timestamp;

    public Poke() {
        // default constructor
    }

    public Poke(String user, String time) {
        this.user = user;
        this.timestamp = time;
    }

    @Override
    public String toString() {
        return this.user + " (" + timestamp + ")";
    }
}
