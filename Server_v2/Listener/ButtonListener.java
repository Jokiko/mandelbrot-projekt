package Listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import Panels.MandelbrotPanel;

public class ButtonListener implements ActionListener {

	private MandelbrotPanel mandelbrotPanel;

	public ButtonListener(MandelbrotPanel mandelbrotPanel) {
		this.mandelbrotPanel = mandelbrotPanel;
	}

	public void actionPerformed(ActionEvent e) {

		switch (e.getActionCommand()) {
		case "Up":
			mandelbrotPanel.moveY(-10);
			break;
		case "Down":
			mandelbrotPanel.moveY(10);
			break;
		case "Left":
			mandelbrotPanel.moveX(-10);
			break;
		case "Right":
			mandelbrotPanel.moveX(10);
			break;
		case "Zoom Out":
			mandelbrotPanel.zoomOut(0.2);
			break;
		case "Zoom In":
			mandelbrotPanel.zoomIn(0.2);
			break;
		case "Restart":
			mandelbrotPanel.restart();
			break;
		default:
			break;
		}
	}
}
