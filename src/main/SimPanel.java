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
	
	// DEVICE SCREEN DIMENSIONS
	final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	final int maxScreenWidth = (int) screenSize.getWidth();
	final int maxScreenHeight = (int) screenSize.getHeight();
	
	// w x h -> 55 x 48
	final int yardPix = maxScreenHeight / 48;
	
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
	int FPS = 60;
	
	// Key handler
	KeyHandler keyH = new KeyHandler();
	
	// Sim clock
	Thread simThread;
	
	// Finish line dimensions x, y, width, height
	int[] finishLineDim;
	
	
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

	
	// GAME LOOP
	@Override
	public void run() {
		
		double drawInterval = 1000000000 / FPS;		// 0.01666 seconds
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;
		long timer = 0;
		int drawCount = 0;
		
		while (simThread != null) {
			
			currentTime = System.nanoTime();
			
			delta += (currentTime - lastTime) / drawInterval;
			timer += (currentTime - lastTime);
			lastTime = currentTime;
			
			// If enough time has passed (drawInterval) to be able to update and draw
			if (delta >= 1) {
				
				// 1 UPDATE: update information such as character positions
				update();
				
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
				
				// Decreasing the decision timer by one and updating if it reached 0
				this.decreaseDecisionTimer();
				
				drawCount = 0;
				timer = 0;
			}
			
		}
		
	}
	
	public void update() {
		
		// Going over cars that can move
		for (int i = 0; i < cars.size(); i++) {
			if (cars.get(i).canMove()) {
				
				// If crossed the finish line
				if (cars.get(i).getX() > finishLineDim[0] + finishLineDim[2]) {
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
				
				cars.get(i).run();
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
	
	private void setupInitialConditions() {
		
		int heightLane = 8 * yardPix;		// Each lane has a height of 8 yards
		int placementX = 1 * yardPix;		// Cars start "1 yard" away from the screen
		int placementY = 0;
		String[] colors = {"red", "blue", "yellow", "orange", "green", "purple"};
		int carWidth = (yardPix * 2) / 3;
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
		
		// Finish line setup
		this.finishLineDim = new int[4];
		this.finishLineDim[0] = placementX + (50 * yardPix);
		this.finishLineDim[1] = 0;
		this.finishLineDim[2] = yardPix / 4;
		this.finishLineDim[3] = panelHeight;
		
		// Set drone
		this.setDrone();
		
		
	}
	
	private void setDrone() {
		drone = new Drone(this, yardPix * 8, yardPix * 20, (yardPix * 5) / 3, (yardPix * 5) / 3, 1, 0, "red");
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
