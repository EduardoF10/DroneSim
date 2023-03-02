package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JPanel;

import entity.Car;
import entity.Drone;

public class SimPanel extends JPanel implements Runnable {
	
	double callCounter;
	
	
	// DEVICE SCREEN DIMENSIONS
	final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	final int maxScreenWidth = (int) screenSize.getWidth();
	final int maxScreenHeight = (int) screenSize.getHeight();
	
	// w x h -> 55 x 48
	public final int yardPix = maxScreenHeight / 48;
	
	// Entity Dimensions
	public final int ENTITY_DIM = 48;
	
	// PANEL SCREEN DIMENSIONS
	int panelWidth = yardPix * 55;
	int panelHeight = yardPix * 48;
	
	
	// Cars in the field
	public ArrayList<Car> cars;
	
	// Drone
	Drone drone;
	
	// Random decider one for each car
	public ArrayList<Random> rands;
	
	// FPS
	int FPS = 120;
	
	// Key handler
	KeyHandler keyH = new KeyHandler();
	
	// Sim clock
	Thread simThread;
	
	// Finish line dimensions x, y, width, height
	public int[] finishLineDim;
	
	
	public SimPanel() {
		
		this.setPreferredSize(new Dimension(panelWidth, panelHeight));
		this.setBackground(Color.LIGHT_GRAY);
		this.setDoubleBuffered(true);
		this.addKeyListener(keyH);
		this.setFocusable(true);
		
		// Setting up the cars and randomizers
		cars = new ArrayList<Car>();
		rands = new ArrayList<Random>();
		this.setupInitialConditions();
		
		
		
		
		
	}
	
	public void startSimThread() {
		simThread = new Thread(this);
		simThread.start();
	}

	
	// RUN LOOP
	@Override
	public void run() {
		
		double drawInterval = 1000000000 / FPS;		// 0.01666 seconds
		double timePassed;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;
		long timer = 0;
		int drawCount = 0;
		this.callCounter = 0;
		
		while (simThread != null) {
			
			currentTime = System.nanoTime();
			timePassed = currentTime - lastTime;
			delta += timePassed / drawInterval;
			timer += (currentTime - lastTime);
			lastTime = currentTime;
			callCounter++;
			
			// 1 UPDATE: update information such as character positions
			update(timePassed);
			
			// If enough time has passed (drawInterval) to be able to update and draw
			if (delta >= 1) {
				
				// 2 DRAW: draw the screen with the updated information
				repaint();
				
				// Reseting time needed
				delta --;
				
				// Drawing counter
				drawCount++;
				
			}
			
			
			// If a second has passed
			if (timer >= 1000000000) {
//				System.out.println("FPS: " + drawCount);
				System.out.println("Call count: " + Double.toString(callCounter));
				callCounter = 0;
				
				// Decreasing the decision timer by one and updating if it reached 0
				this.decreaseDecisionTimer();
				
				drawCount = 0;
				timer = 0;
			}
			
		}
		
	}
	
	public void update(double timePassed) {
		
		this.drone.run();
		
		moveCars(timePassed);
		moveDrone(timePassed);
		
	}
	
	private void moveDrone(double timePassed) {
		
		double travelDist = (this.drone.getSpeed() * yardPix) * (timePassed / 1000000000);
		double travelX = travelDist * Math.cos(this.drone.getRadDir());
		double travelY = travelDist * Math.sin(this.drone.getRadDir());
		this.drone.setX(this.drone.getX() + travelX);
		this.drone.setY(this.drone.getY() - travelY);
		
	}
	
	private void moveCars(double timePassed) {
		
		double travelDist;
		double travelX;
		double travelY;
		
		// Going over cars that can move
		for (int i = 0; i < cars.size(); i++) {
			if (cars.get(i).canMove()) {
				travelDist = (cars.get(i).getSpeed() * yardPix) * (timePassed / 1000000000);
				travelX = travelDist * Math.cos(cars.get(i).getRadDir());
				travelY = travelDist * Math.sin(cars.get(i).getRadDir());
				cars.get(i).setX(cars.get(i).getX() + travelX);
				cars.get(i).setY(cars.get(i).getY() - travelY);
			}
			
			// If crossed the finish line
			if (cars.get(i).getX() > finishLineDim[0] + finishLineDim[2] && cars.get(i).canMove()) {
				cars.get(i).toggleCanMove();
			}
			
			// Default path to push the cars away from the boundaries
			if (cars.get(i).getY() <= 18) {
				cars.get(i).setDirection(315);
				cars.get(i).setDecisionTimer(2);
			}
			else if (cars.get(i).getY() >= panelHeight - (yardPix * 4)) {
				cars.get(i).setDirection(45);
				cars.get(i).setDecisionTimer(2);
			}
			
		}
		
	}
	
	public void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
		
		drawFinishLine(g2);
		
		drawCars(g2);
		
		drone.draw(g2);
		
		g2.dispose();
	}
	
	private void setNormalCarPositions() {
		
		int heightLane = 8 * yardPix;		// Each lane has a height of 8 yards
		int placementX = 1 * yardPix;		// Cars start "1 yard" away from the screen
		int placementY = 0;
		String[] colors = {"red1", "blue1", "yellow1", "orange1", "green1", "purple1"};
		int carWidth = ENTITY_DIM;
		int carHeight = carWidth;
		
		Random rando = new Random();
		
		int n = 6;
		for (int i = 0; i < n; i++) {
			Random rand = new Random(rando.nextLong());
			rands.add(rand);
			Car car = new Car(placementX - carWidth, placementY + (heightLane / 2) - carHeight / 2, carWidth, carHeight, colors[i]);
			cars.add(car);
			placementY += heightLane;
		}
		
	}
	
	private void setRandomCarPositions() {
		
		String[] colors = {"red1", "blue1", "yellow1", "orange1", "green1", "purple1"};
		int carWidth = ENTITY_DIM;
		int carHeight = carWidth;
		
		Random randFunc = new Random();
		
		int maxX = (int) (panelWidth * 0.5) - (yardPix * 4);
		int maxY = (int) (panelHeight - yardPix * 4) - (yardPix * 4);
		
		int placementX;
		int placementY;
		
		int n = 6;
		for (int i = 0; i < n; i++) {
			Random rand = new Random(randFunc.nextLong());
			rands.add(rand);
			placementX = randFunc.nextInt(maxX) + (yardPix * 4);
			placementY = randFunc.nextInt(maxY) + (yardPix * 4);
			Car car = new Car(placementX, placementY, carWidth, carHeight, colors[i]);
			cars.add(car);
		}
	}
	
	private void setupFinishLine() {
		
		// Finish line setup
		this.finishLineDim = new int[4];
		this.finishLineDim[0] = 51 * yardPix;
		this.finishLineDim[1] = 0;
		this.finishLineDim[2] = yardPix / 4;
		this.finishLineDim[3] = panelHeight;
		
	}
	
	private void setupInitialConditions() {
		
		// Set up car positions
//		this.setNormalCarPositions();
		this.setRandomCarPositions();
		
		// Setup finish line
		this.setupFinishLine();
		
		// Set up drone position
		this.setRandomDronePosition();
		
		
	}
	
	private void setDrone() {
		drone = new Drone(this, yardPix * 8, yardPix * 20, ENTITY_DIM, ENTITY_DIM, 2, 0, "red1");
	}
	
	private void setRandomDronePosition() {
		
		Random rand = new Random();
		
		int maxX = (int) (panelWidth * 0.75) - (yardPix * 4);
		int maxY = (int) (panelHeight - yardPix * 4) - (yardPix * 4);
		
		int placementX = rand.nextInt(maxX) + (yardPix * 4);
		int placementY = rand.nextInt(maxY) + (yardPix * 4);
		
		drone = new Drone(this, placementX, placementY, ENTITY_DIM, ENTITY_DIM, 2, 0, "red1");
		
	}
	
	private void drawFinishLine(Graphics2D g2) {
		g2.setColor(Color.black);
		g2.fillRect(finishLineDim[0], finishLineDim[1], finishLineDim[2], finishLineDim[3]);
	}
	
	
	private void drawCars(Graphics2D g2) {
		for (Car car : this.cars) {
			car.draw(g2);
		}
	}
	
	private void randomFowardDir(Car car, Random rand) {
		
		int quadChoice;
		int quadAngle;
		int resultAngle;
		
		if (!car.canMove()) {
			return;
		}
			
		// Default path to push the cars away from the boundaries
		if (car.getY() <= 18) {
			car.setDirection(315);
		}
		else if (car.getY() >= panelHeight - 18) {
			car.setDirection(45);
		}
			
		else {
			quadChoice = rand.nextInt(2);	// Quadrant choice
			quadAngle = rand.nextInt(91);   // Quadrant angle
			resultAngle = (270 * quadChoice) + quadAngle;
			car.setDirection(resultAngle);
		}
	}
	
	private void decreaseDecisionTimer() {
		for (int i = 0; i < cars.size(); i++) {
			if (cars.get(i).canMove()) {
				if (cars.get(i).getDecisionTimer() == 0) {
					this.randomFowardDir(cars.get(i), rands.get(i));
					this.resetDecisionTimer(cars.get(i), rands.get(i));
				}
				else {
					cars.get(i).setDecisionTimer(cars.get(i).getDecisionTimer() - 1);
				}
			}
		}
	}
	
	private void resetDecisionTimer(Car car, Random rand) {
		int randTimer = rand.nextInt(5);
		car.setDecisionTimer(randTimer);
	}
	
	


}
