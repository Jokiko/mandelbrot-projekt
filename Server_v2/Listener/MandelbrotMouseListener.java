package Listener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import Panels.MandelbrotPanel;

public class MandelbrotMouseListener extends MouseAdapter {

	private MandelbrotPanel mandelbrotPanel;

	private boolean canceled;

	private int mWidth;
	private int mHeight;

	private int startX;
	private int startY;

	private int middleRectX;
	private int middleRectY;

	private int endX;
	private int endY;

	private int widthRect;
	private int heightRect;

	public MandelbrotMouseListener(MandelbrotPanel mandelbrotPanel) {
		this.mandelbrotPanel = mandelbrotPanel;
		this.mWidth = mandelbrotPanel.getWidth();
		this.mHeight = mandelbrotPanel.getHeight();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			startX = e.getX();
			startY = e.getY();
			canceled = false;
		}

		if (e.getButton() == MouseEvent.BUTTON3) {
			mandelbrotPanel.setRectangle(0, 0, 0, 0);
			mandelbrotPanel.setRectangleX(0, 0);
			canceled = true;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		if (!canceled) {
			endX = e.getX();
			endY = e.getY();

			// Mitte des Rechtecks
			middleRectX = (startX + endX) / 2;
			middleRectY = (startY + endY) / 2;

			// Ueberpruefung, damit das Rechteck richtig gezeichnet wird
			if (startX > endX) {
				widthRect = startX - endX;
				if (startY > endY) {
					heightRect = startY - endY;
					mandelbrotPanel.setRectangle(endX, endY, widthRect, heightRect);
				} else {
					heightRect = endY - startY;
					mandelbrotPanel.setRectangle(endX, startY, widthRect, heightRect);
				}
			} else {
				widthRect = endX - startX;
				if (startY > endY) {
					heightRect = startY - endY;
					mandelbrotPanel.setRectangle(startX, endY, widthRect, heightRect);
				} else {
					heightRect = endY - startY;
					mandelbrotPanel.setRectangle(startX, startY, widthRect, heightRect);
				}
			}

			// Mittelpunkt des Rechtecks wird mit einem Kreuz dargestellt
			mandelbrotPanel.setRectangleX(middleRectX, middleRectY);
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (!canceled) {
			double xMove = mandelbrotPanel.getMoveX();
			double yMove = mandelbrotPanel.getMoveY();

			mandelbrotPanel.setRectangle(0, 0, 0, 0);
			mandelbrotPanel.setRectangleX(0, 0);

			xMove += (middleRectX - (mWidth / 2.0));
			yMove += (middleRectY - (mHeight / 2.0));

			mandelbrotPanel.setMoveX(xMove);
			mandelbrotPanel.setMoveY(yMove);

			mandelbrotPanel.zoomIn(((mWidth / (widthRect * 1.0)) + (mHeight / (heightRect * 1.0))) / 2.0);

		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

		double mouseRotation = e.getPreciseWheelRotation();

		if (mouseRotation < 0)
			mandelbrotPanel.zoomIn(mouseRotation * -0.2);
		else
			mandelbrotPanel.zoomOut(mouseRotation * 0.2);

	}
}
