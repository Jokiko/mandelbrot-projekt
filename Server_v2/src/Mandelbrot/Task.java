package src.Mandelbrot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Task {

	int y;
	double xMove;
	double yMove;
	double zoom;
	int iteration;

	public Task(int y, double xMove, double yMove, double zoom, int iteration) {

		this.y = y;
		this.xMove = xMove;
		this.yMove = yMove;
		this.zoom = zoom;
		this.iteration = iteration;

	}

	public int getY() {
		return y;
	}

	public double getxMove() {
		return xMove;
	}

	public double getyMove() {
		return yMove;
	}

	public double getZoom() {
		return zoom;
	}

	public int  getItr() {
		return iteration;
	}

}
