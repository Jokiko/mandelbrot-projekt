package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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

import View.ServerView;

public class Server {

	private ServerView userInterface;
	private ServerSocket serverSocket;
	private InetAddress host;
	private final int port;

	public HashMap<String, Socket> client_sockets = new HashMap<>();
	public HashMap<String, Socket> client_websockets = new HashMap<>();
	public ArrayList<String> client_names = new ArrayList<>();

//	public ArrayList<Socket> listRunning = new ArrayList<>();
//	public ArrayList<Socket> listUnchecked = new ArrayList<>();
//	public ArrayList<String> listTypes = new ArrayList<>();
//	public ArrayList<String> listIP = new ArrayList<>();


	private boolean check = true;

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
//		initalizeListTypes();
		initalizeUserInterface();
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

//	private void initalizeListTypes() {
//
//		listTypes.add("plot");
//		listTypes.add("click");
//		listTypes.add("rectangle");
//		listTypes.add("zoomIn");
//		listTypes.add("zoomOut");
//		listTypes.add("Up");
//		listTypes.add("Down");
//		listTypes.add("Left");
//		listTypes.add("Right");
//		listTypes.add("restart");
//		listTypes.add("restartResume");
//
//	}

	private void initalizeUserInterface() {
		userInterface = new ServerView();
		userInterface.setVisible(true);
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

//				if (listWebSocket.contains(clientSocket)) {
//					webSocketHandshake(clientSocket);
//					createServerThreadWebSocket(clientSocket);
//					continue;
//				}

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
				MethodsServerThread.sendMessage(new PrintWriter(clientSocket.getOutputStream()), "type");
			} else {
				clientType = "WebSocket";
//				listWebSocket.add(clientSocket);
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
//
//	private void createServerThreadWebSocket(Socket clientSocket) {
//
//		String ip = clientSocket.getInetAddress().getHostAddress();
//		ServerThreadWebSocket serverThreadWebSocket = new ServerThreadWebSocket(clientSocket, this);
//		Thread stsThread = new Thread(serverThreadWebSocket);
//
//		listSocket.add(clientSocket);
//		serverThreadWebSocket.setName(clientType + "_" + stsThread.getId());
//		System.out.println("serverThread-Name: " + serverThreadWebSocket.getName());
//		stsThread.start();
//
//	}

	private void createServerThread(Socket clientSocket) {
		String ip = clientSocket.getInetAddress().getHostAddress();
		SocketThread serverThread = new SocketThread(clientSocket, this);

		client_sockets.put("Randy", clientSocket);
		System.out.println("ClientThread-Name: " + clientType + "_" + serverThread.getId());
//		serverThread.run();
	}
	
	public void setRGB(int x, int y, int value) {
		userInterface.setRGB(x, y, value);
	}
	
	public void setImage() {
		userInterface.setImage();
	}

	/*-Getter-Methods-------------------------------------------------------------*/
//	public ArrayList<Socket> getListSocket() {
//		return listSocket;
//	}
//
//	public ArrayList<Socket> getListRunning() {
//		return listRunning;
//	}
//
//	public ArrayList<Socket> getListUnchecked() {
//		return listUnchecked;
//	}
//
//	public ArrayList<Socket> getListWebSocket() {
//		return listWebSocket;
//	}
//
//	public ArrayList<String> getListTypes() {
//		return listTypes;
//	}
//
//	public ArrayList<String> getListIP() {
//		return listIP;
//	}
//
//	public ArrayList<String> getListName() {
//		return listName;
//	}

	/*----------------------------------------------------------------------------*/
}
