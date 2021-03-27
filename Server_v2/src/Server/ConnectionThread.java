package src.Server;

import src.View.ServerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionThread implements Runnable {

	private final ServerSocket serverSocket;
	private final ServerView userInterface;
	private final Server server;
	private final Thread thread;
	private boolean running = true;

	private String clientType;
	private String androidClientName;
	private String data;
	private OutputStream out;

	public ConnectionThread(ServerSocket serverSocket, Server server, ServerView userInterface) {
		this.serverSocket = serverSocket;
		this.server = server;
		this.userInterface = userInterface;
		this.thread = new Thread(this);
	}

	private void accept() throws IOException, NoSuchAlgorithmException {

		Socket clientSocket = serverSocket.accept();
		getClientType(clientSocket);

		if (clientType == null) {
			System.out.println("clientType == null");
			return;
		}

		switch(clientType){
			case "WebSocket":
				userInterface.setNumberOfWebSocketClients(1);
				server.createWebSocketThread(clientSocket, "WebSocket_" + System.nanoTime());
				break;
			case "Android":
				userInterface.setNumberOfAndroidClients(1);
				server.createAndroidSocketThread(clientSocket, androidClientName +  + System.nanoTime());
				break;
			case "Cuda":
				userInterface.setNumberOfCudaClients(1);
				server.createSocketThread(clientSocket, "Cuda_" + System.nanoTime());
				break;
		}
	}

	private void getClientType(Socket clientSocket) throws NoSuchAlgorithmException {

		StringTokenizer tokenizer;
		Scanner scan;
		String tmp;

		try {

			out = clientSocket.getOutputStream();
			scan = new Scanner(new BufferedReader(new InputStreamReader(clientSocket.getInputStream())));
			tokenizer = new StringTokenizer(scan.next(), "/.");
			tmp = tokenizer.nextToken();

			if (tmp.equals("type")) {
				clientType = tokenizer.nextToken();
				if(clientType.equals("Android")){
					androidClientName = tokenizer.nextToken();
				}
			} else {
				clientType = "WebSocket";
				data = tmp + scan.useDelimiter("\\r\\n\\r\\n").next();
				webSocketHandshake(clientSocket, data);
			}
			System.out.println("\nClientType: " + clientType);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Handshake gemaess https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_a_WebSocket_server_in_Java#handshaking
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

	public void start() {
		thread.start();
	}

	public void run() {
		while (running) {
			try {
				accept();
			} catch (NoSuchAlgorithmException e) {
				running = false;
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void stop() {
		running = false;
	}
}
