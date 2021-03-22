package src.Mandelbrot;

import src.Panels.MandelbrotPanel;
import src.Server.Server;

import java.util.ArrayList;

public class TaskBuilder {

	private final Server server;
	private final int imageWidth;
	private final int imageHeight;

	private final MandelbrotPanel mandelbrotPanel;

	private final ArrayList<Task> taskList = new ArrayList<>();
	private boolean calculated;
	private double xMove;
	private double yMove;

	private double zoom;

	private double tmpBefore;
	private int steps;

	private int depth;
	private int itr;
	private int zoomDepth;


	public TaskBuilder(Server server, int imageWidth, int imageHeight) {
		this.server = server;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.calculated = false;
		this.mandelbrotPanel = server.getServerView().getMandelbrotPanel();
		setDefaultValues();
		createNewTasks();
	}

	private void setDefaultValues() {
		mandelbrotPanel.requestFocus();
		xMove = 0;
		yMove = 0;
		zoom = 200;
		depth = 0;
		itr = 200;
		zoomDepth = 0;
		server.getNumberIterations().setText(String.format("Iterations: %d", itr));
	}

	public void moveX(double factor) {
		mandelbrotPanel.requestFocus();
		xMove += factor;
		createNewTasks();
	}

	public void moveY(double factor) {
		mandelbrotPanel.requestFocus();
		yMove += factor;
		createNewTasks();
	}

	public void zoomIn(double factor) {
		mandelbrotPanel.requestFocus();

		steps = 0;
		tmpBefore = zoom;

		zoom *= (1 + factor);
		while((tmpBefore *= 1.2) < zoom){
			steps++;
		}

		xMove += xMove * factor;
		yMove += yMove * factor;


		increaseIteration(steps);

		createNewTasks();
	}

	public void zoomOut(double factor) {
		mandelbrotPanel.requestFocus();

		if(zoomDepth > -10) {

			zoom /= (1 + factor);

			if(zoom >= 170 && zoom <= 200){
				zoomDepth = 0;
				itr = 200;
				server.getNumberIterations().setText(String.format("Iterations: %d", itr));
			}

			xMove -= xMove - (xMove / (1 + factor));
			yMove -= yMove - (yMove / (1 + factor));

			decreaseIteration(--depth);

			createNewTasks();
		}else{
			depth = 0;
		}
	}

	public void defaultImage() {
		setDefaultValues();
		createNewTasks();
	}

	private void increaseIteration(int depth) {
		if (depth == 0) {
			zoomDepth++;
		} else {
			zoomDepth += depth;
		}
		if (zoomDepth > 0 && zoomDepth % 10 == 0) {
			itr += itr * 0.25;
		} else {
			itr += itr * 0.25 * (depth / 10);
		}
		server.getNumberIterations().setText(String.format("Iterations: %d", itr));
	}

	private void decreaseIteration(int depth) {
		zoomDepth--;
		int tmp = itr;
		if (depth % 10 == 0) {
			if((tmp -= tmp - (tmp / 1.25)) >= 200) {
				itr = tmp;
			}else{
				itr = 200;
			}
			server.getNumberIterations().setText(String.format("Iterations: %d", itr));
		}
	}

	private void createNewTasks() {
		taskList.clear();
		calculated = false;
		for (int i = 0; i < imageHeight; i++) {
			taskList.add(new Task(i, xMove, yMove, zoom, itr));
		}
	}

	public synchronized Task getTask() {

		if (taskList.size() > 0 && !calculated) {
			Task task = taskList.get(0);
			taskList.remove(task);
			return task;
		}

		calculated = true;

		return null;
	}

	public synchronized void addToTaskList(Task task){
		taskList.add(task);
	}
}
