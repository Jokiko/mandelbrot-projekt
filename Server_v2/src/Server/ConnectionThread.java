package src.Server;

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

	private ServerSocket serverSocket;
	private Server server;
	private Thread thread;
	private boolean running = true;

	private String clientType;
	private String data;
	private OutputStream out;

	public ConnectionThread(ServerSocket serverSocket, Server server) {
		this.serverSocket = serverSocket;
		this.server = server;
		this.thread = new Thread(this);
	}

	private void accept() throws IOException, NoSuchAlgorithmException {

		Socket clientSocket = serverSocket.accept();
		getClientType(clientSocket);

		if (clientType == null) {
			System.out.println("clientType == null");
			return;
		}

		if (clientType.equals("WebSocket")){
			server.createWebsocketThread(clientSocket, "Test " + System.nanoTime());
		}
		else{
			server.createSocketThread(clientSocket, "Test" + System.nanoTime());
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void stop() {
		running = false;
	}
}
