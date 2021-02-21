package src.Mandelbrot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Task {

	byte[] y_bytes;
	byte[] xMove_bytes;
	byte[] yMove_bytes;
	byte[] zoom_bytes;
	byte[] iteration;

	public Task(int y, double xMove, double yMove, double zoom, int itr) {

		y_bytes = ByteBuffer.allocate(4).putInt(y).array();
		xMove_bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(xMove).array();
		yMove_bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(yMove).array();
		zoom_bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(zoom).array();
		iteration = ByteBuffer.allocate(4).putInt(itr).array();
	}

	public byte[] getY() {
		return y_bytes;
	}

	public byte[] getxMove() {
		return xMove_bytes;
	}

	public byte[] getyMove() {
		return yMove_bytes;
	}

	public byte[] getZoom() {
		return zoom_bytes;
	}

	public byte[] getItr() {
		return iteration;
	}

}
