package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Entity {
	
	private double x, y;
	private int width, height;
	
	private double speed;
	
	private BufferedImage imageSprite;
	
	private double radDir;		// direction in radians
	
	
	public Entity(double x, double y, int width, int height, double speed, BufferedImage image, int direction) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.speed = speed;
		this.imageSprite = image;
		this.radDir = Math.toRadians(direction);
		
	}
	
	public void draw(Graphics2D g2) {
		g2.drawImage(this.imageSprite, (int) this.x, (int) this.y, this.width, this.height, null);
	}
	
	
	
	// Getters
	public double getX() { return this.x; }
	public double getY() { return this.y; }
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }
	public double getSpeed() { return this.speed; }
	public double getRadDir() { return this.radDir; }
	public BufferedImage getImageSprite() { return this.imageSprite; }
	
	// Setters
	public void setX(double x) { this.x = x; }
	public void setY(double y) { this.y = y; }
	public void setImageSprite(BufferedImage image) { this.imageSprite = image; }
	public void setSpeed(double speed) { this.speed = speed; }
	public void setDirection(double rad) { this.radDir = rad; }
	public void setDirection(int direction) { this.radDir = Math.toRadians(direction); }
	
	
}
