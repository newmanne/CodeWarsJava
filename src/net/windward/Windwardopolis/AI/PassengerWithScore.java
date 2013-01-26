package net.windward.Windwardopolis.AI;

import net.windward.Windwardopolis.api.Passenger;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Zongyi
 * Date: 1/26/13
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class PassengerWithScore {

    public Passenger passenger;
    java.util.ArrayList<Point> path;
    public double score;

    public PassengerWithScore(Passenger in_passenger, java.util.ArrayList<Point> in_path, double in_score)
    {
        passenger = in_passenger; score = in_score; path = in_path;
    }
}
