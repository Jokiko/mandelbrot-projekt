package src.Server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import src.Mandelbrot.MandelbrotImage;
import src.Mandelbrot.Task;
import src.Mandelbrot.TaskBuilder;
import src.View.ServerView;

public class Server {

	/* Used to build TCP connection */
	
	private InetAddress host;
	private ServerSocket serverSocket;
	private ConnectionThread connectionThread;

	/* Userinterface */
	private ServerView userInterface;

	/* Data to be displayed in "userInterface */
	private MandelbrotImage image;

	/* Creates tasks based on current user interactions */
	private TaskBuilder taskbuilder;

	/* Used to store client sockets */
	private HashMap<String, Socket> client_sockets = new HashMap<>();
	private HashMap<String, Socket> client_websockets = new HashMap<>();

	/* TCP Serverport */
	private final int port;

	/* Number of clients */
	private volatile int connected;

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
	 * Startup method. Can be called again by user via JOptionpane if any exception
	 * occurs during initialization.
	 */

	public void startServer() {
		initializeHost();
		initializeServerSocket();
		initializeConnectionThread();
		initalizeUserInterface();
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
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(host, port));
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
	 * Initializes the UserInterface
	 */

	private void initalizeUserInterface() {
		userInterface = new ServerView(this);
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
		taskbuilder = new TaskBuilder(width, height);

	}

	/*
	 * package private method used by "connectionThread". Initiates and starts a
	 * "SocketThread" object, which is added to "client_sockets"
	 */

	void createSocketThread(Socket clientSocket, String name) {

		SocketThread socketThread = new SocketThread(clientSocket, this);
		client_sockets.put(name, clientSocket);
		socketThread.start();

	}

	/*
	 * package private method used by "connectionThread". Initiates and starts a
	 * "WebsocketThread" object, which is added to "client_websockets"
	 */

	void createWebsocketThread(Socket clientSocket, String name) {

		WebsocketThread websocketThread = new WebsocketThread(clientSocket, this);
		client_sockets.put(name, clientSocket);
		websocketThread.start();

	}

	/*---Interaction-methods-called-by-classes-of-package-"Listener"--*/

	public void moveX(double factor) {
		taskbuilder.moveX(factor);
//		image.transformX(factor);
	}

	public void moveY(double factor) {
		taskbuilder.moveY(factor);
//		image.transformY(factor);
	}

	public void zoomIn(double factor) {
//		if (!taskbuilder.zoomIn(factor)) {
//			image.transformZoomIn(factor);
//		}

		taskbuilder.zoomIn(factor);
	}

	public void zoomOut(double factor) {
//		if (!taskbuilder.zoomOut(factor)) {
//			image.transformZoomOut(factor);
//		}
		taskbuilder.zoomOut(factor);
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

	public TaskBuilder getTaskBuilder() {
		return taskbuilder;
	}

	synchronized Task getTask() {
		return taskbuilder.getTask();
	}

	/*----------------------------------------------------------------*/
}
