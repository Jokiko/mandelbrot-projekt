package src.Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringTokenizer;

import src.Mandelbrot.Task;

public class SocketThread implements Runnable {

	private final Socket socket;
	private final Server server;

	private byte[] y_bytes, xMove_bytes, yMove_bytes, zoom_bytes, itr_bytes;

	private BufferedReader reader;
	private PrintWriter writer;
	private DataOutputStream dout;

	private final Thread thread;
	private Task task;

	private boolean disconnected;
	private boolean connected;

	public SocketThread(Socket socket, Server server, String name) {

		this.socket = socket;
		this.server = server;
		this.thread = new Thread(this);
		this.thread.setName("Thread_" + name);

		connected = false;
		disconnected = false;

		initializeStreams();

	}

	private void initializeStreams() {

		try {
			reader = new BufferedReader((new InputStreamReader(socket.getInputStream())));
			writer = new PrintWriter(socket.getOutputStream());
			dout = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void sendMessage(byte[] task) throws IOException {
		dout.write(task);
		dout.flush();
	}

	private void sendMessage(String text) {
//		System.out.println("SocketThread-" + thread.getId() + ": " + text);
		writer.println(text);
		writer.flush();
	}

	private void receiveMessage() {

		String input;
		StringTokenizer token;
		String compare;

		try {
			while (((input = (reader.readLine())) != null) && !Thread.currentThread().isInterrupted()) {
				token = new StringTokenizer(input, "/.");
				compare = token.nextElement().toString();

				switch (compare) {
				case "connect":
					connect();
					break;
				case "task":
					sendTask();
					break;
				case "tick":
					task = null;
					server.setImage();
					break;
				case "frame":
					server.setImage();
					break;
				case "s":
					return;
				default:
					plot(input);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		sendMessage("First contact successful\n\0");
		thread.start();
	}

	private void connect() {
		sendMessage("Connect success\n\0");
		connected = true;
		server.connect();
	}

	private void disconnect() {
		if (connected) {
			if(task != null){
				server.addToTaskList(task);
			}
			disconnected = true;
			close();
			server.disconnect();
		}
	}

	private void sendTask() throws IOException {

		task = server.getTask();

		if (task == null) {
			sendMessage("noTask\0");
			return;
		}

		sendMessage("task\0");
		receiveMessage();
		y_bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(task.getY()).array();
		sendMessage(y_bytes);
		receiveMessage();
		xMove_bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(task.getXMove()).array();
		sendMessage(xMove_bytes);
		receiveMessage();
		yMove_bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(task.getYMove()).array();
		sendMessage(yMove_bytes);
		receiveMessage();
		zoom_bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(task.getZoom()).array();
		sendMessage(zoom_bytes);
		receiveMessage();
		itr_bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(task.getItr()).array();
		sendMessage(itr_bytes);
	}

	private void plot(String compare) throws IOException {
		int x;
		int y;
		int itr;
		x = Integer.parseInt(compare);
		y = Integer.parseInt(reader.readLine());
		itr = Integer.parseInt(reader.readLine());
		
		server.setRGB(x, y, itr);
	}

	private void close() {
		System.out.println(Thread.currentThread().getName() + ": Connection Closing...");
		thread.interrupt();
		try {
			reader.close();
			System.out.println("BufferedReader closed: " + reader.toString());
			writer.close();
			System.out.println("OutputStream closed: " + writer.toString());
			socket.close();
			System.out.println("Socket closed: " + socket.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		
		receiveMessage();

		if (!disconnected)
			disconnect();
		System.out.println(Thread.currentThread().getName() + " terminated");

	}
}