package net.windward.Windwardopolis.AI;


// Created by Windward Studios, Inc. (www.windward.net). No copyright claimed - do anything you want with this code.


import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.windward.Windwardopolis.api.Company;
import net.windward.Windwardopolis.api.Map;
import net.windward.Windwardopolis.api.Passenger;
import net.windward.Windwardopolis.api.Player;


/**
 * The sample C# AI. Start with this project but write your own code as this is a very simplistic implementation of the AI.
 */
public class MyPlayerBrain implements net.windward.Windwardopolis.AI.IPlayerAI {
    // bugbug - put your team name here.
    private static String NAME = "Tejas, Zongyi, Cheng, Neil";

    // bugbug - put your school name here. Must be 11 letters or less (ie use MIT, not Massachussets Institute of Technology).

    public static String SCHOOL = "UofT.";

    private static Random random = new Random();


    /**
     * The name of the player.
     */
    private String privateName;

    public final String getName() {
        return privateName;
    }

    private void setName(String value) {
        privateName = value;
    }

    /**
     * The game map.
     */
    private Map privateGameMap;

    public final Map getGameMap() {
        return privateGameMap;
    }

    private void setGameMap(Map value) {
        privateGameMap = value;
    }

    /**
     * All of the players, including myself.
     */
    private java.util.ArrayList<Player> privatePlayers;

    public final java.util.ArrayList<Player> getPlayers() {
        return privatePlayers;
    }

    private void setPlayers(java.util.ArrayList<Player> value) {
        privatePlayers = value;
    }

    /**
     * All of the companies.
     */
    private java.util.ArrayList<Company> privateCompanies;

    public final java.util.ArrayList<Company> getCompanies() {
        return privateCompanies;
    }

    private void setCompanies(java.util.ArrayList<Company> value) {
        privateCompanies = value;
    }

    /**
     * All of the passengers.
     */
    private java.util.ArrayList<Passenger> privatePassengers;

    public final java.util.ArrayList<Passenger> getPassengers() {
        return privatePassengers;
    }

    private void setPassengers(java.util.ArrayList<Passenger> value) {
        privatePassengers = value;
    }

    /**
     * Me (my player object).
     */
    private Player privateMe;

    public final Player getMe() {
        return privateMe;
    }

    private void setMe(Player value) {
        privateMe = value;
    }

    private PlayerAIBase.PlayerOrdersEvent sendOrders;

    private static final java.util.Random rand = new java.util.Random();

    public MyPlayerBrain(String name) {
        setName(!net.windward.Windwardopolis.DotNetToJavaStringHelper.isNullOrEmpty(name) ? name : NAME);
    }

    /**
     * The avatar of the player. Must be 32 x 32.
     */
    public final byte[] getAvatar() {
        try {
            // open image
            File file = new File(getClass().getResource("/net/windward/Windwardopolis/res/MyAvatar.png").getFile());

            FileInputStream fisAvatar = new FileInputStream(file);
            byte [] avatar = new byte[fisAvatar.available()];
            fisAvatar.read(avatar, 0, avatar.length);
            return avatar;

        } catch (IOException e) {
            System.out.println("error reading image");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Called at the start of the game.
     *
     * @param map         The game map.
     * @param me          You. This is also in the players list.
     * @param players     All players (including you).
     * @param companies   The companies on the map.
     * @param passengers  The passengers that need a lift.
     * @param ordersEvent Method to call to send orders to the server.
     */
    public final void Setup(Map map, Player me, java.util.ArrayList<Player> players, java.util.ArrayList<Company> companies, java.util.ArrayList<Passenger> passengers, PlayerAIBase.PlayerOrdersEvent ordersEvent) {

        try {
            setGameMap(map);
            setPlayers(players);
            setMe(me);
            setCompanies(companies);
            setPassengers(passengers);
            sendOrders = ordersEvent;

            java.util.ArrayList<Passenger> pickup = AllPickups(me, passengers);

            // get the path from where we are to the dest.
            java.util.ArrayList<Point> path = CalculatePathPlus1(me, pickup.get(0).getLobby().getBusStop());
            sendOrders.invoke("ready", path, pickup);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Called to send an update message to this A.I. We do NOT have to send orders in response.
     *
     * @param status     The status message.
     * @param plyrStatus The player this status is about. THIS MAY NOT BE YOU.
     * @param players    The status of all players.
     * @param passengers The status of all passengers.
     */
    public final void GameStatus(PlayerAIBase.STATUS status, Player plyrStatus, java.util.ArrayList<Player> players, java.util.ArrayList<Passenger> passengers) {

    	  // bugbug - Framework.cs updates the object's in this object's Players, Passengers, and Companies lists. This works fine as long
        // as this app is single threaded. However, if you create worker thread(s) or respond to multiple status messages simultaneously
        // then you need to split these out and synchronize access to the saved list objects.

        try {
           

            switch (status) {
                case UPDATE:
                    return;
            }

            ArrayList<PassengerWithScore> passengerScores = new ArrayList<PassengerWithScore>();
            java.util.ArrayList<Passenger> pickup = new java.util.ArrayList<Passenger>();
            Point ptDest = null;
            if (getMe().getLimo().getPassenger() != null)
            {
                ArrayList<PassengerWithScore> selfPassengerScore = new ArrayList<PassengerWithScore>();
                for (int j = 0; j < getCompanies().size(); j++)
                {
                    double score = calculateHeursitics(getMe(), getCompanies().get(j), getMe().getLimo().getPassenger(), players);

                    selfPassengerScore.add(new PassengerWithScore(getMe().getLimo().getPassenger(),SimpleAStar.CalculatePath(getGameMap(), getMe().getLimo().getMapPosition(), getCompanies().get(j).getBusStop()),
                            score));
                }
                Collections.sort(selfPassengerScore, new PassengerScoreComparator());
                java.util.ArrayList<Passenger> availPassengers = AllPickups(getMe(), passengers);
                for (int i = 0; i < availPassengers.size(); i++)
                {
                    for (int j = 0; j < getCompanies().size(); j++)
                    {
                        double score = calculateHeursitics(getMe(), getCompanies().get(j), availPassengers.get(i), players);
                        passengerScores.add(new PassengerWithScore(availPassengers.get(i),SimpleAStar.CalculatePath(getGameMap(), getMe().getLimo().getMapPosition(), getCompanies().get(j).getBusStop()),
                                score));

                    }
                }
            }
            else
            {
                java.util.ArrayList<Passenger> availPassengers = AllPickups(getMe(), passengers);
                for (int i = 0; i < availPassengers.size(); i++)
                {
                    for (int j = 0; j < getCompanies().size(); j++)
                    {
                        double score = calculateHeursitics(getMe(), getCompanies().get(j), availPassengers.get(i), players);
                        passengerScores.add(new PassengerWithScore(availPassengers.get(i),SimpleAStar.CalculatePath(getGameMap(), getMe().getLimo().getMapPosition(), getCompanies().get(j).getBusStop()),
                        score));

                    }
                }
            }
            
            Collections.sort(passengerScores, new PassengerScoreComparator());
            ptDest = passengerScores.get(0).passenger.getDestination().getBusStop();


            for (int i = 0; i <passengerScores.size(); i++)
            {
                pickup.add(passengerScores.get(i).passenger);
            }

            // get the path from where we are to the dest.
            java.util.ArrayList<Point> path = CalculatePathPlus1(plyrStatus, ptDest);

            // update our saved Player to match new settings
            if (path.size() > 0) {
                getMe().getLimo().getPath().clear();
                getMe().getLimo().getPath().addAll(path);
            }
            if (pickup.size() > 0) {
                getMe().getPickUp().clear();
                getMe().getPickUp().addAll(pickup);
            }

            sendOrders.invoke("move", path, pickup);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

	private List<Company> getCompaniesWithNoEnemies(Player plyrStatus) {
		// Go somewhere where an enemy is not
		List<Company> companiesToConsider = new ArrayList<Company>(getCompanies());
		for (Company company : getCompanies()) {
		    for (Passenger enemy : plyrStatus.getLimo().getPassenger().getEnemies()) {
		    	if (enemy.getLobby() != null){
			    	if (enemy.getLobby().equals(company)) {
			    		companiesToConsider.remove(company);
			    	}
		    	}
		    }
		}
		return companiesToConsider;
	}

    private java.util.ArrayList<Point> CalculatePathPlus1(Player me, Point ptDest) {
        java.util.ArrayList<Point> path = SimpleAStar.CalculatePath(getGameMap(), me.getLimo().getMapPosition(), ptDest);
        // add in leaving the bus stop so it has orders while we get the message saying it got there and are deciding what to do next.
        if (path.size() > 1) {
            path.add(path.get(path.size() - 2));
        }
        return path;
    }

    private static java.util.ArrayList<Passenger> AllPickups(Player me, Iterable<Passenger> passengers) {
        java.util.ArrayList<Passenger> pickup = new java.util.ArrayList<Passenger>();

        for (Passenger psngr : passengers) {
            if ((!me.getPassengersDelivered().contains(psngr)) && (psngr != me.getLimo().getPassenger()) && (psngr.getCar() == null) && (psngr.getLobby() != null) && (psngr.getDestination() != null))
                pickup.add(psngr);
        }

        //add sort by random so no loops for can't pickup
        return pickup;
    }

    private int getTotalTripDistance(Point start, Point pickSpot, Point end){
    	int lengthToGetThem = SimpleAStar.CalculatePath(getGameMap(), start, pickSpot).size();
    	int lengthToDropThem = SimpleAStar.CalculatePath(getGameMap(), pickSpot, end).size();
    	return lengthToDropThem + lengthToGetThem;
    	
    }
    
    
    private double calculateHeursitics(final Player me, Company company, Passenger passenger, java.util.ArrayList<Player> players){
    	double points = 0;
    	if (company.equals(passenger.getDestination())){ 
    		points = passenger.getPointsDelivered();    	
    	}
    	double timeToPassenger = 0;
    	if (!me.getLimo().equals(passenger.getCar())){
    		timeToPassenger = SimpleAStar.CalculatePath(getGameMap(), me.getLimo().getMapPosition(), passenger.getLobby().getBusStop()).size();
    	}
    	
    	double timeToDropOffFromPassenger = SimpleAStar.CalculatePath(getGameMap(),me.getLimo().getMapPosition(),company.getBusStop()).size();;
    	if (!me.getLimo().equals(passenger.getCar())){
        	timeToDropOffFromPassenger = SimpleAStar.CalculatePath(getGameMap(),passenger.getLobby().getBusStop(),company.getBusStop()).size();;
    	}
    	
    	double minOtherAIDistanceToPassenger = 0;
    	if (!me.getLimo().equals(passenger.getCar())){
    		minOtherAIDistanceToPassenger = getMinDistanceToPassenger(me, passenger, players);
    	}
    	
    	double totalTime = timeToPassenger + timeToDropOffFromPassenger;
    	double enemyAtDropOff = getEnemiesAtDropOffScore(passenger, company, totalTime); 
    	
    	double aiWithEnemyDropOff = 0;
    	
    	double futureTimeDropOff = 0;
    	System.out.println("Total Time: " + totalTime);
    	System.out.println("Penalty For DropOff: " + enemyAtDropOff);
    	double overallScore = 10*points - 0.1*totalTime - 0.05*minOtherAIDistanceToPassenger - enemyAtDropOff;
    	
    	return overallScore;
    }
    

	private double getEnemiesAtDropOffScore(Passenger passenger, Company company, double totalTime) {
    	List<Passenger> passengersAtCompany = company.getPassengers();
    	double score = 0;
    	double penalty = 100;
    	for (Passenger passengerAtCompany : passengersAtCompany) {
			if (passenger.getEnemies().contains(passengerAtCompany)) {
				score += penalty / totalTime ;
			}
		}
    	return score;
	}

	private double getMinDistanceToPassenger(Player me, Passenger passenger, ArrayList<Player> players) {
    	List<Double> values = new ArrayList<Double>();
    	for (Player player : players) {
    		values.add((double) SimpleAStar.CalculatePath(getGameMap(), player.getLimo().getMapPosition(), passenger.getLobby().getBusStop()).size());
		}
		return Collections.min(values);
	}

	private java.util.ArrayList<Passenger> SortPassengers(final Player me, java.util.ArrayList<Passenger> passengers, java.util.ArrayList<Player> players){
    	java.util.Map <Passenger, Double> passengersToScores = new HashMap<Passenger, Double>();
    	
    	System.out.println(passengers);
    	Collections.sort(passengers, new Comparator<Passenger>() {
			@Override
			public int compare(Passenger player0, Passenger player1) {
				//TODO: Get time rather than distance
				double player0Penalty = 0;
				double player1Penalty = 0;
				for (Passenger enemy: player0.getEnemies()){
					if(enemy.getLobby()!= null){
						if (enemy.getLobby().getBusStop().equals(player0.getDestination().getBusStop())){
							player0Penalty+=1;
						}
					}
				}
				for (Passenger enemy: player1.getEnemies()){
					if (enemy.getLobby()!=null){
						if (enemy.getLobby().getBusStop().equals(player1.getDestination().getBusStop())){
							player1Penalty+=1;
						}
					}
				}
				double totalTripLength0 = getTotalTripDistance(me.getLimo().getMapPosition(), player0.getLobby().getBusStop(), player0.getDestination().getBusStop());
				double totalTripLength1 = getTotalTripDistance(me.getLimo().getMapPosition(), player1.getLobby().getBusStop(), player1.getDestination().getBusStop());
				return  totalTripLength0/player0.getPointsDelivered() - player0Penalty < totalTripLength1/player1.getPointsDelivered() - player1Penalty ? 1 : -1;
			}
		});
    	Collections.reverse(passengers);
    	System.out.println(passengers);
    	return passengers;
    }
    
    private Company getClosestCompanyToMe(Player me, List<Company> companies) {
    	Company closestCompany = null;
    	int min = Integer.MAX_VALUE;
    	for (Company company : companies) {
    		int size = CalculatePathPlus1(me, company.getBusStop()).size();
    		if (size < min) {
    			min = size;
    			closestCompany = company;
    		}
    	}
		return closestCompany;
    }
}