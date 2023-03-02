package entity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import main.SimPanel;



/* The task the drone must execute can be divided into phases
 * 
 * Phase 1: Determine the closest intercepting point
 * 
 * Phase 2: Fly towards the intercepting point if there are no significant changes in velocity
 * 
 * Phase 3: Shoot at the drone if there is a clear target
 * 
 * 
 * Phase 4: After taking all the enemies, drone goes past the finish line to land safely
 * 		  : Must take the shortest path to the finish line
 * 
 * Phase 5: End
 * 
 * 
 */

// Step 1 - Direct the drone towards closest enemy


public class Drone extends Entity {
	
	private final double DRONE_SPEED = 7;		// Capable speed
	
	private final int NUM_OF_ENEMIES = 5;		// Number of enemy UGVs on the field
	
	private SimPanel sp;					// Allows the drone to access information about the field
	
	String teamColor;				// Team identifier (team ArUco marker)
	
	private ArrayList<String> downedEnemies;	// Keeps track of which enemies are already down
	
	private int enemiesLeft;        // How many enemies are alive in the field
	
	private double targetDir = 0;		// Info on the current target
	private double targetSpeed = 0;
	private double[] targetLoc = {-1, -1};
	private String targetColor = "";
	
	private int phaseCounter;		// Tells us which phase to execute
	
	private double maneuverRad;		// The angle of return to be on top of the car
	
	private HashMap<String, double[]> prevCarLocations;       // The x and y of the car in the previous state
	private HashMap<String, double[]> curCarLocations;		 
	
	
	
	
	public Drone(SimPanel sp, double x, double y, int width, int height, int droneSelection, int direction, String team) {
		super(x, y, width, height, 0, getDroneImage(droneSelection), direction);
		this.setSpeed(DRONE_SPEED);
		this.sp = sp;
		this.teamColor = team;
		this.downedEnemies = new ArrayList<String>();
		this.enemiesLeft = NUM_OF_ENEMIES;
		this.phaseCounter = 1;					// Starting at the first phase
		this.maneuverRad = 0;
		this.prevCarLocations = new HashMap<String, double[]>();
		this.curCarLocations = new HashMap<String, double[]>();
		carsInit();
		
		
		
	}
	

	
	
	
	// This method will serve as the transition model (the brain) of the drone
	public void run() {
		
//		
//		// Phase 1 
//		if (phaseCounter == 1) {
//			
//			phase1();
//		}
//			
//		else if (phaseCounter == 2) {
//			
//			phase2();
//		}
		
		this.basicRun();
		
		
		
		
	}
	
	private Car getCarByColor(String color) {
		for (Car car : sp.cars) {
			if (car.getColor() == color) {
				return car;
			}
		}
		return null;
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
	
	private Car getClosestCar() {
		
		double minDist = -1;
		double relativeX;
		double relativeY;
		double relativeDist;
		Car minCar = null;
		
		for (Car car : sp.cars) {
			if (car.getColor() != this.teamColor) {
				if (car.canMove()) {
					relativeX = car.getX() - this.getX();
					relativeY = car.getY() - this.getY();
					relativeDist = Math.sqrt((relativeX * relativeX) + (relativeY * relativeY));
					if (relativeDist < minDist || minDist == -1) {
						minCar = car;
						minDist = relativeDist;
					}
					
				}
			}
		}
		
		return minCar;
		
	}
	
	private void headTowardsClosestCar(Car closeCar) {
		
		// Set the direction towards the closest interception point
		double radDir = Math.atan2(this.getY() - closeCar.getY(), closeCar.getX() - this.getX());
		this.setDirection(radDir);
	}
	
	private void shootAbove(Car car) {
		if (Math.abs(this.getX() - car.getX()) < 0.001) {
			if (Math.abs(this.getY() - car.getY()) < 0.001) {
				car.setSpeed(0);
				System.out.println("UAV shoots at x: " + Double.toString(this.getX()) + ", y: " + Double.toString(this.getY()));
				System.out.println(car.getColor() + " UGV shot at x: " + Double.toString(car.getX()) + ", y: " + Double.toString(car.getY()));
				car.toggleCanMove();
				this.enemiesLeft--;
				
			}
			
		}
	}
	
	private void headTowardsFinishLine() {
		this.setDirection(0);
	}
	
	private void landingSequence() {
		// If past the finish line
		if (this.getX() > sp.finishLineDim[0]) {
			this.setSpeed(0);
		}
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
	
	private void searchAndFly() {
		
		// Store the current location of the cars
		storeCarLocations();
					
		// Get closest enemy color
		String closestColor = getClosestColor();
		// Predict the location of the enemy given the previous and current tables
		double nextCarX = this.curCarLocations.get(closestColor)[0] + (this.curCarLocations.get(closestColor)[0] - this.prevCarLocations.get(closestColor)[0]);
		double nextCarY = this.curCarLocations.get(closestColor)[1] + (this.curCarLocations.get(closestColor)[1] - this.prevCarLocations.get(closestColor)[1]);
					
		// Calculate the distance and direction required
		double relativeX = nextCarX - this.getX();
		double relativeY = nextCarY - this.getY();
		double relativeDist = Math.sqrt((relativeX * relativeX) + (relativeY * relativeY));
		double rad = Math.acos(relativeX / relativeDist);
		
		// Set the direction of the drone
		this.setDirection(rad);
		
		
		// If we are close to the car, we proceed to phase 2
		if (relativeDist < this.getSpeed()) {
			double maneuverRad = Math.acos(relativeDist / 2 * this.getSpeed());
			this.setDirection(this.getRadDir() + maneuverRad);
			this.phaseCounter = 2;
		}
		
		 
		
	}
	
	private double[] getInterceptionCoor(Car car) {
		
		double t = getPossibleInterceptionTime(car);
		
		// No solution
		if (t <= 0) {
			return null;
		}
		
		double resultX = (car.getSpeed() * sp.yardPix * Math.cos(car.getRadDir())) * t + car.getX();
		double resultY = (car.getSpeed() * sp.yardPix * Math.sin(car.getRadDir())) * t + car.getY();
		
		double[] result = {resultX, resultY};
		return result;
		
	}
	
	private double getPossibleInterceptionTime(Car car) {
		
		double[] quadVals = getQuadraticValues(car);
		
		double[] result = quadraticFunc(quadVals[0], quadVals[1], quadVals[2]);
		
		double t1 = result[0];
		double t2 = result[1];
		
		// No solution
		if (t1 < 0 && t2 < 0) {
			return -1;
		}
		else if (t1 > 0 && t2 > 0) {
			return Math.min(t1,  t2);
		}
		else {
			return Math.max(t1,  t2);
		}
	}
	
	private static double[] quadraticFunc(double a, double b, double c) {
		
		// Comparing the value of a with 0, if a is 0 then the equation is not quadratic   
		if (a == 0) {  
			System.out.println("The value of a cannot be 0.");  
			double[] result = {-1, -1};
			return result;  
		}  
		// Calculating discriminant (d)  
		double d = b * b - 4 * a * c;  
		double sqrtval = Math.sqrt(Math.abs(d));  
		if (d > 0) {  
			System.out.println("The roots of the equation are real and different. \n");
			double result1 = (double)(-b + sqrtval) / (2 * a);
			double result2 = (double)(-b - sqrtval) / (2 * a);
			System.out.println(result1 + "\n"+ result2);
			double[] result = {result1, result2};
			return result;
		}  
		else if (d == 0) {  
			System.out.println("The roots of the equation are real and same. \n");
			double result1 = -(double)b / (2 * a);
			System.out.println(result1 + "\n"+ result1); 
			double[] result = {result1};
			return result;
		}  
		// executes if d < 0  
		else {  
			System.out.println("The roots of the equation are complex and different. \n");  
			System.out.println(-(double)b / (2 * a) + " + i"+ sqrtval + "\n"+ -(double)b / (2 * a)+ " - i" + sqrtval);
			double[] result = {-1, -1};		// Not going to consider complex numbers
			return result;
		}   
	}
	
	// Returns the quadratic values for the intersection with the car
	private double[] getQuadraticValues(Car car) {
		
		double dronePixSpeed = this.getSpeed() * sp.yardPix;
		double carPixSpeed = car.getSpeed() * sp.yardPix;
		
		// a value
		double aValue = (dronePixSpeed * dronePixSpeed) - (carPixSpeed * carPixSpeed);
		
		// b value
		double carSpeedX = carPixSpeed * Math.cos(car.getRadDir());
		double carSpeedY = carPixSpeed * Math.sin(car.getRadDir());
		double xDist = this.getX() - car.getX();
		double yDist = this.getY() - car.getY();
		double D_VR = (carSpeedX * xDist) + (carSpeedY * yDist);
		
		double bValue = 2 * D_VR;
		
		// c value
		double cValue = -1 * ((xDist * xDist) + (yDist * yDist));
		
		double[] result = {aValue, bValue, cValue};
		return result;
		
	}
	
	private void phase1() {
		
		double[] intLoc;
		double[] minLoc = {-1, -1};
		double minDist = -1;
		double minDir = 0;
		double minSpeed = 0;
		String minColor = "";
		
		for (Car car : sp.cars) {
			
			if (car.getColor() == this.teamColor || this.downedEnemies.contains(car.getColor())) {
				continue;
			}
			
			// Get interception location
			intLoc = getInterceptionCoor(car);
			if (intLoc == null) {
				continue;
			}
			
			double relativeX = intLoc[0] - this.getX();
			double relativeY = intLoc[1] - this.getY();
			double relativeDist = Math.sqrt((relativeX * relativeX) + (relativeY * relativeY));
			if (minDist == -1 || relativeDist < minDist) {
				minLoc = intLoc;
				minDist = relativeDist;
				minDir = car.getRadDir();
				minSpeed = car.getSpeed();
				minColor = car.getColor();
			}
			
		}
		
		// Set the direction towards the closest interception point
		double radDir = Math.atan2(this.getY() - minLoc[1], minLoc[0] - this.getX());
		this.setDirection(radDir);
		
		// Save the target
		this.targetDir = minDir;
		this.targetSpeed = minSpeed;
		this.targetLoc = minLoc;
		this.targetColor = minColor;
		
		// Proceed to next phase
		this.phaseCounter = 2;
	}
	
	private void phase2() {
		
		Car targetCar = this.getCarByColor(this.targetColor);
		
		// If target changed course
		if ((this.targetDir != targetCar.getRadDir()) || (this.targetSpeed != targetCar.getSpeed())) {
			phase1();
		}
		
		// Reached destination
		if (Math.abs(this.targetLoc[0] - this.getX()) < 0.01 && Math.abs(this.targetLoc[1] - this.getY()) < 0.01) {
			this.setSpeed(0);
			phase3();
			
		}
		
		
	}
	
	private void phase3() {
//		if (target)
	}
	
	private void basicRun() {
		
		// Fly and Shoot - If there are 1 or more enemies left
		if (this.enemiesLeft > 0) {
			Car closeCar = this.getClosestCar();
			this.headTowardsClosestCar(closeCar);
			// Shoot if above
			this.shootAbove(closeCar);
		}
		// Head towards finish line if enemy cars are down
		else if (this.getSpeed() != 0) {
			// Heading towards finish line and landing if able
			this.headTowardsFinishLine();
			this.landingSequence();
		}
		
		
		
	}
	
	
	
	
}
