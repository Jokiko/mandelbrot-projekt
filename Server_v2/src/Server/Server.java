package src.Server;

import java.io.IOException;
import java.net.*;

import javax.swing.*;

import src.Mandelbrot.MandelbrotImage;
import src.Mandelbrot.Task;
import src.Mandelbrot.TaskBuilder;
import src.View.ServerView;

public class Server {
	/* Used to show the current number pf iterations*/
	private JLabel number_iterations;

	/* Used to build TCP connection */
	private InetAddress host;
	private ServerSocket serverSocket;
	private ConnectionThread connectionThread;

	/* UserInterface */
	private ServerView userInterface;

	/* Data to be displayed in "userInterface */
	private MandelbrotImage image;

	/* Creates tasks based on current user interactions */
	private TaskBuilder taskbuilder;

	/* TCP ServerPort */
	private final int port;

	/* Number of clients */
	private volatile int connected;

	/******** Getter ********/
	public int getMANDELBROT_PANEL_WIDTH() {
		return userInterface.getMANDELBROT_PANEL_WIDTH();
	}
	public int getMANDELBROT_PANEL_HEIGHT() {
		return userInterface.getMANDELBROT_PANEL_HEIGHT();
	}
	public int getConnected() {
		return connected;
	}
	public JLabel getNumberIterations(){
		return number_iterations;
	}
	public ServerView getServerView(){
		return userInterface;
	}

	/******** Setter ********/
	public void setNumberIterations(JLabel number_iterations){
		this.number_iterations = number_iterations;
	}

	/**
	 * Constructor of {@code Server}
	 * 
	 * @param port The port this server will be waiting for connections
	 */
	public Server(int port) {
		this.port = port;
		this.connected = 0;
	}

	/**
	 * Startup method. Can be called again by user via JOptionPane if any exception
	 * occurs during initialization.
	 */
	public void startServer() {
		initializeUserInterface();
		initializeHost();
		initializeServerSocket();
		initializeConnectionThread();
		initializeTaskBuilder();
		initializeImage();
	}

	/*
	 * Initializes the host variable. Calls "displayRestartPane()" in case of
	 * exception
	 */
	private void initializeHost() {
		try {
			host = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			displayRestartPane();
		}
	}

	/*
	 * Initializes the serverSocket. Calls "displayRestartPane()" in case of
	 * exception
	 */
	private void initializeServerSocket() {
		try {
			//TODO anpassen
			serverSocket = new ServerSocket(port);
			//serverSocket = new ServerSocket();
			//serverSocket.bind(new InetSocketAddress(host, port));
		} catch (IOException ioe) {
			System.out.println("Server error");
			ioe.printStackTrace();
			displayRestartPane();
		}

		System.out.printf("Server with IP %s started\n", host.getHostAddress());
		System.out.printf("Listening at port %d\n", port);
	}

	/*
	 * Displays an JOptionPane to retry the startup in case of exception.
	 */
	private void displayRestartPane() {
		int input = JOptionPane.showOptionDialog(null, "Server could not be started", "ERROR",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[] { "Restart", "Cancel" },
				null);
		if (input == 0) { // Restart click
			System.out.println("\nServer restarted");
			startServer();
		} else {
			System.exit(1);
		}
	}

	/*
	 * Starts "connectionThread", which accepts incoming client connection requests
	 */
	private void initializeConnectionThread() {
		connectionThread = new ConnectionThread(serverSocket, this);
		connectionThread.start();
	}

	/*
	 * sets the width and height for the mandelbrotPanel
	 */
	private int[] setResolution(){
		int[] resolution = {0, 0};
		JTextField widthField = new JTextField( "500", 5);
		JTextField heightField = new JTextField("500", 5);

		JPanel myPanel = new JPanel();
		myPanel.add(new JLabel("Width:"));
		myPanel.add(widthField);
		myPanel.add(Box.createHorizontalStrut(15)); // a spacer
		myPanel.add(new JLabel("Height:"));
		myPanel.add(heightField);

		int result = JOptionPane.showConfirmDialog(null, myPanel,
				"Enter Resolution", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			try {
				resolution[0] = Integer.parseInt(widthField.getText());
				System.out.println("Width value: " + resolution[0]);
				resolution[1] = Integer.parseInt(heightField.getText());
				System.out.println("Height value: " + resolution[1]);
				if(resolution[0] <= 0 || resolution[1] <= 0){
					JOptionPane.showOptionDialog(null, "Width (" + resolution[0] + ") and height (" + resolution[1] + ") \ncannot be <= 0", "ERROR",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.ERROR_MESSAGE, null,
							new String[]{"OK"}, null);
				}
			}catch(NumberFormatException e){
				JOptionPane.showOptionDialog(null, "Wrong input for resolution", "ERROR",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.ERROR_MESSAGE, null,
						new String[]{"OK"}, null);
			}
		}else{
			System.out.println("Cancel");
			System.exit(1);
		}
		return resolution;
	}

	/*
	 * Initializes the UserInterface
	 */
	private void initializeUserInterface() {
		int[] resolution = setResolution();

		if(resolution[0] <= 0 || resolution[1] <= 0){
			resolution = setResolution();
		}
		userInterface = new ServerView(this, resolution[0], resolution[1]);
		userInterface.setVisible(true);
	}

	/*
	 * Initializes "image", which contains the visualized mandelbrotset
	 */
	private void initializeImage() {

		int width = userInterface.getMandelbrotWidth();
		int height = userInterface.getMandelbrotHeight();
		image = new MandelbrotImage(width, height, MandelbrotImage.TYPE_INT_RGB);

	}

	/*
	 * Initializes "taskbuilder", which provides the current tasks to be calculated
	 * by the clients
	 */
	private void initializeTaskBuilder() {

		int width = userInterface.getMandelbrotWidth();
		int height = userInterface.getMandelbrotHeight();
		taskbuilder = new TaskBuilder(this, width, height);

	}

	/*
	 * package private method used by "connectionThread". Initiates and starts a
	 * "SocketThread" object, which is added to "client_sockets"
	 */
	void createSocketThread(Socket clientSocket, String name) {

		SocketThread socketThread = new SocketThread(clientSocket, this, name);
		socketThread.start();

	}

	/*
	 * package private method used by "connectionThread". Initiates and starts a
	 * "AndroidSocketThread" object, which is added to "client_sockets"
	 */
	void createAndroidSocketThread(Socket clientSocket, String name) {

		AndroidSocketThread androidSocketThread = new AndroidSocketThread(clientSocket, this, name);
		androidSocketThread.start();

	}

	/*
	 * package private method used by "connectionThread". Initiates and starts a
	 * "WebSocketThread" object, which is added to "client_websockets"
	 */
	void createWebSocketThread(Socket clientSocket, String name) {

		WebSocketThread websocketThread = new WebSocketThread(clientSocket, this, name);
		websocketThread.start();

	}

	/*---Interaction-methods-called-by-classes-of-package-"Listener"--*/

	public void moveX(double factor) {
		if(getConnected() > 0) {
			taskbuilder.moveX(factor);
		}
	}

	public void moveY(double factor) {
		if(getConnected() > 0){
			taskbuilder.moveY(factor);
		}
	}

	public void zoomIn(double factor) {
		if(getConnected() > 0) {
			taskbuilder.zoomIn(factor);
		}
	}

	public void zoomOut(double factor) {
		if(getConnected() > 0){
			taskbuilder.zoomOut(factor);
		}
	}

	public void defaultImage() {
		taskbuilder.defaultImage();
	}

	public void close() {
		connectionThread.stop();
	}

	/*----------------------------------------------------------------*/

	/*---Package-private-called-by-(Web)SocketThread------------------*/

	synchronized void setRGB(int x, int y, int value) {
		image.setRGB(x, y, value | value << 20);
	}

	void setImage() {
		userInterface.setImage(image);
	}

	void connect() {

		if (++connected == 1)
			userInterface.enableButtons();

		userInterface.setNumberOfClients(connected);
	}

	void disconnect() {

		if (--connected == 0)
			userInterface.disableButtons();

		userInterface.setNumberOfClients(connected);
	}

	/*----------------------------------------------------------------*/

	/*-Getter-Methods-------------------------------------------------*/

	synchronized Task getTask() {
		return taskbuilder.getTask();
	}

	/*----------------------------------------------------------------*/

	synchronized void addToTaskList(Task task){
		taskbuilder.addToTaskList(task);
	}
}
