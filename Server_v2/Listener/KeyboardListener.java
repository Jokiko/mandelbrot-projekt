package Listener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Panels.MandelbrotPanel;

public class KeyboardListener implements KeyListener {

	private MandelbrotPanel mandelbrotPanel;

	public KeyboardListener(MandelbrotPanel mandelbrotPanel) {
		this.mandelbrotPanel = mandelbrotPanel;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {	
		case KeyEvent.VK_MINUS:
			mandelbrotPanel.zoomOut(0.2);
			break;
		case KeyEvent.VK_PLUS:
			mandelbrotPanel.zoomIn(0.2);
			break;
		case KeyEvent.VK_LEFT:
			mandelbrotPanel.moveX(-10);
			break;
		case KeyEvent.VK_RIGHT:
			mandelbrotPanel.moveX(10);
			break;
		case KeyEvent.VK_UP:
			mandelbrotPanel.moveY(-10);
			break;
		case KeyEvent.VK_DOWN:
			mandelbrotPanel.moveY(10);
			break;
		case KeyEvent.VK_ESCAPE:
			mandelbrotPanel.restart();
			break;
		default:
			break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {

		switch (e.getKeyCode()) {
		case KeyEvent.VK_MINUS:
			mandelbrotPanel.zoomOut(0.05);
			break;
		case KeyEvent.VK_PLUS:
			mandelbrotPanel.zoomIn(0.05);
			break;
		case KeyEvent.VK_LEFT:
			mandelbrotPanel.moveX(-2);
			break;
		case KeyEvent.VK_RIGHT:
			mandelbrotPanel.moveX(2);
			break;
		case KeyEvent.VK_UP:
			mandelbrotPanel.moveY(-2);
			break;
		case KeyEvent.VK_DOWN:
			mandelbrotPanel.moveY(2);
			break;
		case KeyEvent.VK_ESCAPE:
			mandelbrotPanel.restart();
			break;
		default:
			break;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}
