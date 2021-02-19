
package Panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import Listener.KeyboardListener;
import Listener.MandelbrotMouseListener;

public class MandelbrotPanel extends JPanel {

	private final int WIDTH;
	private final int HEIGHT;
	private final Dimension DIMENSION;

	private BufferedImage image;
	private MandelbrotMouseListener mouseListener;
	private KeyboardListener keyboardListener;

	private double xMove = 0;
	private double yMove = 0;

	private double zoomX = 200;
	private double zoomY = 200;

	private int rectX;
	private int rectY;

	private int rectWidth;
	private int rectHeight;

	private int rectCenterX;
	private int rectCenterY;

	public MandelbrotPanel(int width, int height) {

		this.WIDTH = width;
		this.HEIGHT = height;
		this.DIMENSION = new Dimension(WIDTH, HEIGHT);
		this.image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		this.mouseListener = new MandelbrotMouseListener(this);
		this.keyboardListener = new KeyboardListener(this);

		setBorder(BorderFactory.createLineBorder(Color.WHITE));
		setFocusable(true);
		setMinimumSize(DIMENSION);
		setPreferredSize(DIMENSION);
		setMaximumSize(DIMENSION);

		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		addMouseWheelListener(mouseListener);
		addKeyListener(keyboardListener);
		plotPoints();

	}

	private void plotPoints() {

		double zy;
		double zx;
		double cx;
		double cy;
		double temp;
		int itr;
		int colorItr = 20;

		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {

				zx = zy = 0;
				cx = (x - (WIDTH / 2) + xMove) / zoomX;
				cy = (y - (HEIGHT / 2) + yMove) / zoomY;
				// jeweils Division mit 2, damit in der Mitte des Bildschirms

				itr = 50;

				while (zx * zx + zy * zy < 4 && itr > 0) {
					temp = zx * zx - zy * zy + cx;
					zy = 2 * zx * zy + cy;
					zx = temp;
					itr--;
				}
				image.setRGB(x, y, itr | (itr << colorItr));
			}
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, this);
		drawAxisCross(g);
		drawRectangle(g);
		drawRectangleX(g);
	}

	private void drawAxisCross(Graphics g) {
		g.setColor(Color.WHITE);
		g.drawLine(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
		g.drawLine(0, HEIGHT / 2, WIDTH, HEIGHT / 2);
	}

	public void setRectangle(int x, int y, int width, int height) {
		this.rectX = x;
		this.rectY = y;
		this.rectWidth = width;
		this.rectHeight = height;

		repaint();
	}

	public void setRectangleX(int middleX, int middleY) {
		this.rectCenterX = middleX;
		this.rectCenterY = middleY;
		repaint();
	}

	private void drawRectangle(Graphics g) {
		g.drawRect(rectX, rectY, rectWidth, rectHeight);
	}

	private void drawRectangleX(Graphics g) {
		g.setColor(Color.WHITE);
		g.drawLine(rectCenterX, rectCenterY, rectCenterX - 5, rectCenterY);
		g.drawLine(rectCenterX, rectCenterY, rectCenterX + 5, rectCenterY);
		g.drawLine(rectCenterX, rectCenterY, rectCenterX, rectCenterY - 5);
		g.drawLine(rectCenterX, rectCenterY, rectCenterX, rectCenterY + 5);

		rectCenterX = 0;
		rectCenterY = 0;
	}

	public void restart() {

		zoomX = 200;
		zoomY = 200;

		xMove = 0;
		yMove = 0;

		plotPoints();
		repaint();
	}

	public void zoomIn(double factor) {

		zoomX *= (1 + factor);
		zoomY *= (1 + factor);

		xMove += xMove * factor;
		yMove += yMove * factor;

		plotPoints();
		validate();
		repaint();

	}

	public void zoomOut(double factor) {

		zoomX /= (1 + factor);
		zoomY /= (1 + factor);

		xMove -= xMove * factor;
		yMove -= yMove * factor;

		plotPoints();
		validate();
		repaint();
	}

	public void moveX(double factor) {
		xMove += factor;
		plotPoints();
		validate();
		repaint();
	}

	public void moveY(double factor) {
		yMove += factor;
		plotPoints();
		validate();
		repaint();
	}

	public int getHeight() {
		return HEIGHT;
	}

	public int getWidth() {
		return WIDTH;
	}

	public double getMoveX() {
		return xMove;
	}

	public void setMoveX(double xMove) {
		this.xMove = xMove;
	}

	public double getMoveY() {
		return yMove;
	}

	public void setMoveY(double yMove) {
		this.yMove = yMove;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
		validate();
		repaint();
	}
}
