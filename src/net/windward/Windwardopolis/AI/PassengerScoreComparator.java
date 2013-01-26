package net.windward.Windwardopolis.AI;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Zongyi
 * Date: 1/26/13
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class PassengerScoreComparator implements Comparator<PassengerWithScore> {

    @Override
    public int compare(PassengerWithScore p1, PassengerWithScore p2) {
        if (p1.score > p2.score)
            return -1;
        if (p1.score < p2.score)
            return 1;

        return 0;
    }
}
