package src.Mandelbrot;

import java.awt.image.BufferedImage;

public class MandelbrotImage extends BufferedImage {
	
	public MandelbrotImage(int width, int height, int imageType) {
		super(width, height, imageType);
		
	}

	public void transformX(double factor) {
		//TODO: Move Image on x-Axis
	}

	public void transformY(double factor) {
		//TODO: Move Image on y-Axis
	}

	public void transformZoomIn(double factor) {
		//TODO: Zoom In by rescaling
	}

	public void transformZoomOut(double factor) {
		//TODO: Zoom Out by rescaling
	}
}
