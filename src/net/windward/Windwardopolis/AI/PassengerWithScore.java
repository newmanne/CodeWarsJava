package net.windward.Windwardopolis.AI;

import java.awt.Point;

import net.windward.Windwardopolis.api.Company;
import net.windward.Windwardopolis.api.Passenger;

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
    public Company company;

    public PassengerWithScore(Passenger in_passenger, java.util.ArrayList<Point> in_path, double in_score, Company company)
    {
        passenger = in_passenger; score = in_score; path = in_path;this.company=company;
    }
}
