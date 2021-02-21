package src.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import src.Mandelbrot.MandelbrotImage;
import src.Mandelbrot.Task;
import src.Mandelbrot.TaskBuilder;
import src.View.ServerView;

public class Server {

	private InetAddress host;

	private ServerSocket serverSocket;
	private ServerView userInterface;

	private MandelbrotImage image;
	private TaskBuilder taskbuilder;

	private final int port;

	private HashMap<String, Socket> client_sockets = new HashMap<>();
	private HashMap<String, Socket> client_websockets = new HashMap<>();
	private ArrayList<String> client_names = new ArrayList<>();
	private volatile int connected = 0;
	private boolean check;

	private String clientType;
	private String data;

	private OutputStream out;

	public Server(int port) {
		this.port = port;
	}

	/**
	 * Startup method. Can be called again by user via JOptionpane if any exception
	 * occurs during initialization.
	 */
	public void startServer() {
		check = true;
		initializeHost();
		initializeServerSocket();
		initalizeUserInterface();
		initializeTaskbuilder();
		initializeImage();
		acceptClient();
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
			check = false;
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
			System.out.println("\nCancel");
		}
	}

	private void initalizeUserInterface() {
		userInterface = new ServerView(this);
		userInterface.setVisible(true);
	}

	private void initializeImage() {
		int height = userInterface.getMandelbrotWidth();
		int width = userInterface.getMandelbrotHeight();
		image = new MandelbrotImage(width, height, MandelbrotImage.TYPE_INT_RGB);
	}

	private void initializeTaskbuilder() {
		int width = userInterface.getMandelbrotPanel().getWidth();
		int height = userInterface.getMandelbrotPanel().getHeight();
		taskbuilder = new TaskBuilder(width, height);
	}

	private void acceptClient() {
		while (check) {
			try {

				Socket clientSocket = serverSocket.accept();
				getClientType(clientSocket);

				if (clientType == null) {
					System.out.println("clientType == null");
					continue;
				}

				createServerThread(clientSocket);

			} catch (IOException ioe) {
				check = false;
				ioe.printStackTrace();
			} catch (NoSuchAlgorithmException nsae) {
				// TODO Auto-generated catch block
				System.out.println("Error");
				nsae.printStackTrace();
			}
		}
	}

	private void getClientType(Socket clientSocket) throws NoSuchAlgorithmException, IOException {

		StringTokenizer tokenizer;
		Scanner scan;
		String tmp;
		try {

			out = clientSocket.getOutputStream();
			scan = new Scanner(new BufferedReader(new InputStreamReader(clientSocket.getInputStream())));
			tokenizer = new StringTokenizer(scan.next(), "/.../");
			tmp = tokenizer.nextToken();

			if (tmp.equals("type")) {
				clientType = tokenizer.nextToken();
			} else {
				clientType = "WebSocket";
				data = tmp + scan.useDelimiter("\\r\\n\\r\\n").next();
				webSocketHandshake(clientSocket, data);
			}
			System.out.println("ClientType: " + clientType);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void webSocketHandshake(Socket clientSocket, String data) throws NoSuchAlgorithmException, IOException {

		Matcher get = Pattern.compile("^GET").matcher(data);
		Matcher match;

		byte[] response;

		if (get.find()) {
			match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
			if (match.find()) {
				response = ("HTTP/1.1 101 Switching Protocols\r\n" + "Connection: Upgrade\r\n"
						+ "Upgrade: websocket\r\n" + "Sec-WebSocket-Accept: "
						+ Base64.getEncoder()
								.encodeToString(MessageDigest.getInstance("SHA-1")
										.digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
												.getBytes(StandardCharsets.UTF_8)))
						+ "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
				out.write(response, 0, response.length);
			}
		}

	}

	private void createServerThread(Socket clientSocket) {
	
		SocketThread serverThread = new SocketThread(clientSocket, this);

		client_sockets.put("TestName", clientSocket);
		serverThread.start();
		System.out.println("ClientThread-Name: " + clientType + "_" + serverThread.getId());

	}

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

	public synchronized void setRGB(int x, int y, int value) {
		image.setRGB(x, y, value | value << 20);
	}

	public void setImage() {
		userInterface.getMandelbrotPanel().setImage(image);
	}

	public void defaultImage() {
		taskbuilder.defaultImage();
	}

	public void connect() {

		if (++connected == 1)
			userInterface.getButtonPanel().enableAll();

		userInterface.getMonitorPanel().setNumberOfClients(connected);
	}

	public void disconnect() {

		if (--connected == 0)
			userInterface.getButtonPanel().disableAll();

		userInterface.getMonitorPanel().setNumberOfClients(connected);
	}

	/*-Getter-Methods-------------------------------------------------------------*/

	public TaskBuilder getTaskBuilder() {
		return taskbuilder;
	}

	public Task getTask() {
		return taskbuilder.getTask();
	}

	/*----------------------------------------------------------------------------*/
}
