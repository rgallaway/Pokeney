package me.ryangallaway.pokeney;

import java.util.Random;

/**
 * Created by Ryan Gallaway on 2/4/2018.
 */

public class Poke {
    public String user;
    public String timestamp;
    public int id;

    public Poke() {
        // default constructor
    }

    public Poke(String user, String time) {
        this.user = user;
        this.timestamp = time;
        Random r = new Random();
        id = r.nextInt();
    }

    @Override
    public String toString() {
        return this.user + " (" + timestamp + ")";
    }
}
