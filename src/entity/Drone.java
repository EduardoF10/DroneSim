package entity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import main.SimPanel;



/* The task the drone must execute can be divided into phases
 * 
 * Phase 1: Fly over UGVs that are not friendly and shoot them out
 * 		  : Using the greedy method where the drone goes towards the nearest enemy first
 * 
 * Phase 2: After taking all the enemies, drone goes past the finish line to land safely
 * 		  : Must take the shortest path to the finish line
 * 
 * Phase 3: End
 * 
 */


public class Drone extends Entity {
	
	private final int NUM_OF_ENEMIES = 5;		// Number of enemy UGVs on the field
	
	private SimPanel sp;					// Allows the drone to access information about the field
	
	String teamColor;				// Team identifier (team ArUco marker)
	
	private ArrayList<String> downedEnemies;	// Keeps track of which enemies are already down
	
	private int enemiesLeft;        // How many enemies are alive in the field
	
	private int phaseCounter;		// Tells us which phase to execute
	
//	private int[] prevDroneMovement;		// The speed and angle of the drone in the previous state
	
	private HashMap<String, double[]> prevCarLocations;       // The x and y of the car in the previous state
	private HashMap<String, double[]> curCarLocations;		 
	
	
	
	
	public Drone(SimPanel sp, double x, double y, int width, int height, int droneSelection, int direction, String team) {
		super(x, y, width, height, getDroneImage(droneSelection), direction);
		
		this.sp = sp;
		this.teamColor = team;
		this.downedEnemies = new ArrayList<String>();
		this.enemiesLeft = NUM_OF_ENEMIES;
		this.phaseCounter = 0;					// Starting at the first phase
		this.prevCarLocations = new HashMap<String, double[]>();
		this.curCarLocations = new HashMap<String, double[]>();
		carsInit();
		
		
		
	}
	

	
	
	
	// This method will serve as the transition model (the brain) of the drone
	public void run() {
		
		
		// Phase 1 
		if (phaseCounter == 0) {
			
			// Store the current location of the cars
			storeCarLocations();
			
			// Get closest enemy
			String closestColor = getClosestColor();
			
			// Predict the location of the enemy given the previous and current tables
			double NextCarX = this.curCarLocations.get(closestColor)[0] + (this.curCarLocations.get(closestColor)[0] - this.prevCarLocations.get(closestColor)[0]);
			double NextCarY = this.curCarLocations.get(closestColor)[1] + (this.curCarLocations.get(closestColor)[1] - this.prevCarLocations.get(closestColor)[1]);
			
			// Calculate the distance required
			
		}
		
		
		
		
		
	}
	
	
	private void carsInit() {
		
		for (int i = 0; i < sp.cars.size(); i++) {
			if (sp.cars.get(i).getColor() != this.teamColor) {
				this.prevCarLocations.put(sp.cars.get(i).getColor(), getCarLocation(sp.cars.get(i)));
			}
		}
	}
	
	
	private static BufferedImage getDroneImage(int droneSelection) {
		
		String filePath = "/drones/" + Integer.toString(droneSelection) + ".png";
		BufferedImage droneImage = null;
		
		try {
			droneImage = ImageIO.read(Car.class.getResourceAsStream(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return droneImage;
		
	}
	
	// Returns the distance and angle of the car from the drone
	private double[] getCarLocation(Car car) {
		double[] result = {car.getX(), car.getY()};
		return result;
	}
	
	// Returns the color of the closest enemy car that is alive
	private String getClosestColor() {
		
		double minDist = -1;
		String minColor = "";
		
		double relativeX;
		double relativeY;
		double relativeDist;
		
		for (Car car : sp.cars) {
			if (car.getColor() != this.teamColor) {
				if (car.canMove()) {
					relativeX = car.getX() - this.getX();
					relativeY = car.getY() - this.getY();
					relativeDist = Math.sqrt((relativeX * relativeX) + (relativeY * relativeY));
					if (relativeDist < minDist || minDist == -1) {
						minColor = car.getColor();
						minDist = relativeDist;
					}
					
				}
			}
		}
		
		return minColor;
		
	}
	
	// Stores the current location of the cars
	private void storeCarLocations() {
		for (Car car : sp.cars) {
			double[] carCoor = {car.getX(), car.getY()};
			this.curCarLocations.put(car.getColor(), carCoor);
		}
	}
	
	private void searchAndShoot() {
		
		// Store the current location of the cars
		storeCarLocations();
					
		// Get closest enemy
		String closestColor = getClosestColor();
					
		// Predict the location of the enemy given the previous and current tables
		double NextCarX = this.curCarLocations.get(closestColor)[0] + (this.curCarLocations.get(closestColor)[0] - this.prevCarLocations.get(closestColor)[0]);
		double NextCarY = this.curCarLocations.get(closestColor)[1] + (this.curCarLocations.get(closestColor)[1] - this.prevCarLocations.get(closestColor)[1]);
					
		// Calculate the distance required
		
	}
	
	
	
}
