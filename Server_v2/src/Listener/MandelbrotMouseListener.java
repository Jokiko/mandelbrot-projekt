package src.Listener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import src.Panels.MandelbrotPanel;
import src.Server.Server;

public class MandelbrotMouseListener extends MouseAdapter {

	private final Server server;
	private final MandelbrotPanel mandelbrotPanel;
	private boolean canceled;

	private int mWidth;
	private int mHeight;

	private int startX;
	private int startY;

	private int middleRectX;
	private int middleRectY;

	private int endX;
	private int endY;

	private double factorX;
	private double factorY;

	private int rectX;
	private int rectY;

	private int rectWidth;
	private int rectHeight;

	public MandelbrotMouseListener(Server server, MandelbrotPanel mandelbrotPanel) {
		this.server = server;
		this.mandelbrotPanel = mandelbrotPanel;
		this.mWidth = mandelbrotPanel.getWidth();
		this.mHeight = mandelbrotPanel.getHeight();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && server.getConnected() > 0) {
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

		if (!canceled && server.getConnected() > 0) {
			endX = e.getX();
			endY = e.getY();

			rectX = Math.min(startX, endX);
			rectY = Math.min(startY, endY);

			rectWidth = Math.abs(startX - endX);
			rectHeight = Math.abs(startY - endY);

			// Mitte des Rechtecks
			middleRectX = (startX + endX) / 2;
			middleRectY = (startY + endY) / 2;

			// Mittelpunkt des Rechtecks wird mit einem Kreuz dargestellt
			mandelbrotPanel.setRectangle(rectX, rectY, rectWidth, rectHeight);
			mandelbrotPanel.setRectangleX(middleRectX, middleRectY);
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (!canceled && server.getConnected() > 0) {

			if(startX != endX && startY != endY && endX > 0 && endY > 0){
				factorX = (middleRectX - (mWidth / 2.0));
				factorY = (middleRectY - (mHeight / 2.0));

				server.moveX(factorX);
				server.moveY(factorY);

				server.zoomIn(((mWidth / (rectWidth * 1.0)) + (mHeight / (rectHeight * 1.0))) / 2.0);

			}
			mandelbrotPanel.setRectangle(0, 0, 0, 0);
			mandelbrotPanel.setRectangleX(0, 0);

		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(server.getConnected() > 0) {

			double mouseRotation = e.getPreciseWheelRotation();

			if (mouseRotation < 0)
				server.zoomIn(mouseRotation * -0.2);
			else
				server.zoomOut(mouseRotation * 0.2);
		}
	}
}
