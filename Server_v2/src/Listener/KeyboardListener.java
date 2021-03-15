package src.Listener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import src.Server.Server;

public class KeyboardListener implements KeyListener {

	private Server server;

	public KeyboardListener(Server server) {
		this.server = server;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_MINUS:
			server.zoomOut(0.2);
			break;
		case KeyEvent.VK_PLUS:
			server.zoomIn(0.2);
			break;
		case KeyEvent.VK_LEFT:
			server.moveX(-10);
			break;
		case KeyEvent.VK_RIGHT:
			server.moveX(10);
			break;
		case KeyEvent.VK_UP:
			server.moveY(-10);
			break;
		case KeyEvent.VK_DOWN:
			server.moveY(10);
			break;
		default:
			break;
		}

	}

	@Override
	public void keyPressed(KeyEvent e) {

		switch (e.getKeyCode()) {
		case KeyEvent.VK_MINUS:
			//server.zoomOut(0.05);
			break;
		case KeyEvent.VK_PLUS:
			//server.zoomIn(0.05);
			break;
		case KeyEvent.VK_LEFT:
			server.moveX(-2);
			break;
		case KeyEvent.VK_RIGHT:
			server.moveX(2);
			break;
		case KeyEvent.VK_UP:
			server.moveY(-2);
			break;
		case KeyEvent.VK_DOWN:
			server.moveY(2);
			break;
		case KeyEvent.VK_ESCAPE:
			server.defaultImage();
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
