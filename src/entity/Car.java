package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;


public class Car extends Entity {
	
	private String color;
	private boolean canMove;
	private double[] signalReceived;
	private int decisionTimer;			// Amount of seconds that need to pass to make another direction decision
	

	public Car(double x, double y, int width, int height, String color) {
		
		super(x, y, width, height, 2, getCarImage(color), 0);
		
		this.color = color;
		this.canMove = true;
		double[] initialSignal = {0, 0, 0};
		this.signalReceived = initialSignal;
		this.decisionTimer = 0;
	}
	
	
	private static BufferedImage getCarImage(String color) {
		
		String filePath = "/cars/" + color + ".png";
		BufferedImage carImage = null;
		
		try {
			carImage = ImageIO.read(Car.class.getResourceAsStream(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return carImage;
	}
	
	
	
	// Getters
	public String getColor() { return this.color; }
	public boolean canMove() { return this.canMove; }
	public boolean receivedSignal() { return this.signalReceived[0] == 1; }
	public int getDecisionTimer() { return this.decisionTimer; }
	
	// Setters
	public void setDecisionTimer(int dt) { this.decisionTimer = dt; }
	
	
	
	public void toggleCanMove() { 
		this.canMove = !this.canMove;
		this.signalReceived[0] = 1;
		this.signalReceived[1] = this.getX();
		this.signalReceived[2] = this.getY();
	}
	
	
	
	public void run() {
		double travelX = this.getSpeed() * Math.cos(this.getRadDir());
		double travelY = this.getSpeed() * Math.sin(this.getRadDir());
		
		this.setX(this.getX() + travelX);
		this.setY(this.getY() - travelY);
		
	}
	
	
}
