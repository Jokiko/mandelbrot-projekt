package src.Panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import src.Listener.KeyboardListener;
import src.Listener.MandelbrotMouseListener;

public class MandelbrotPanel extends JPanel {

	private final int WIDTH;
	private final int HEIGHT;
	private final Dimension DIMENSION;

	private BufferedImage image;
	private MandelbrotMouseListener mouseListener;
	private KeyboardListener keyboardListener;

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

		setBorder(BorderFactory.createLineBorder(Color.WHITE));
		setFocusable(true);
		setMinimumSize(DIMENSION);
		setPreferredSize(DIMENSION);
		setMaximumSize(DIMENSION);

	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, this);
		drawAxisCross(g);
		drawRectangle(g);
		drawRectangleX(g);
		requestFocus();
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

	}

	public int getHeight() {
		return HEIGHT;
	}

	public int getWidth() {
		return WIDTH;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
		validate();
		repaint();
	}
}
