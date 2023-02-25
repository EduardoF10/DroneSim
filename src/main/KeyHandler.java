package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class KeyHandler implements KeyListener {
	
	public boolean upPressed, downPressed, leftPressed, rightPressed;
	
	public ArrayList<String> keysPressed = new ArrayList<String>();

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		
		int code = e.getKeyCode();
		
		if (code == KeyEvent.VK_W) {
			if (upPressed == false) {
				keysPressed.add("up");
			}
			upPressed = true;
		}
		if (code == KeyEvent.VK_S) {
			if (downPressed == false) {
				keysPressed.add("down");
			}
			downPressed = true;
		}
		if (code == KeyEvent.VK_A) {
			if (leftPressed == false) {
				keysPressed.add("left");
			}
			leftPressed = true;
		}
		if (code == KeyEvent.VK_D) {
			if (rightPressed == false) {
				keysPressed.add("right");
			}
			rightPressed = true;
		}
		
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
		int code = e.getKeyCode();
		
		if (code == KeyEvent.VK_W) {
			upPressed = false;
			keysPressed.remove("up");
		}
		if (code == KeyEvent.VK_S) {
			downPressed = false;
			keysPressed.remove("down");
		}
		if (code == KeyEvent.VK_A) {
			leftPressed = false;
			keysPressed.remove("left");
		}
		if (code == KeyEvent.VK_D) {
			rightPressed = false;
			keysPressed.remove("right");
		}
	}

}
