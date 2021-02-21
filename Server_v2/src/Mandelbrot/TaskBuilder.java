package src.Mandelbrot;

public class TaskBuilder {

	private int imageWidth;
	private int imageHeight;

	private Task[] tasks;
	private int pos;
	private boolean calculated;
	private double xMove;
	private double yMove;

	private double zoom;
	private double accumulatedZoomFactor;

	private int depth;
	private int itr;

	public TaskBuilder(int imageWidth, int imageHeight) {
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.calculated = false;
		this.tasks = new Task[imageHeight];
		setDefaultValues();
		createNewTasks();
	}

	private void setDefaultValues() {
		pos = 0;
		xMove = 0;
		yMove = 0;
		zoom = 200;
		accumulatedZoomFactor = 1;
		depth = 0;
		itr = 50;
	}

	public void moveX(double factor) {
		xMove += factor;
		createNewTasks();
	}

	public void moveY(double factor) {
		yMove += factor;
		createNewTasks();
	}

	public boolean zoomIn(double factor) {

		accumulatedZoomFactor *= (1 + factor);

		zoom *= (1 + factor);
		xMove += xMove * factor;
		yMove += yMove * factor;

//		if (accumulatedZoomFactor > 2.0) {
//			createNewTasks();
//			increaseIteration(++depth);
//			accumulatedZoomFactor = 1;
//			return true;
//		}
		createNewTasks();
		return false;
	}

	public boolean zoomOut(double factor) {

		accumulatedZoomFactor /= (1 + factor);

		zoom /= (1 + factor);
		xMove -= xMove - (xMove / (1 + factor));
		yMove -= yMove - (yMove / (1 + factor));

//		if (accumulatedZoomFactor < 1.0) {
//			createNewTasks();
//			decreaseIteration(--depth);
//			accumulatedZoomFactor = 1;
//			return true;
//		}
		createNewTasks();
		return false;
	}

	public void defaultImage() {
		setDefaultValues();
		createNewTasks();
	}

	private void increaseIteration(int depth) {
		if (depth % 5 == 0)
			itr += itr * 0.5;
	}

	private void decreaseIteration(int depth) {
		if (depth % 5 == 0)
			itr -= itr - (itr / 1.5);
	}

	private void createNewTasks() {
		calculated = false;
		for (int i = 0; i < imageHeight; i++) {
			tasks[i] = new Task(i, xMove, yMove, zoom, itr);
		}
	}

	public double getMoveX() {
		return xMove;
	}

	public double getMoveY() {
		return yMove;
	}

	public synchronized Task getTask() {

		if (pos < imageHeight && calculated == false)
			return tasks[pos++];

		calculated = true;
		pos = 0;

		return null;
	}
}
