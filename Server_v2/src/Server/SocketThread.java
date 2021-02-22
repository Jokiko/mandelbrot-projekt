package src.Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import src.Benchmarks.PixelBenchmark;
import src.Mandelbrot.Task;

public class SocketThread implements Runnable {

	private Socket socket;
	private Server server;

	private BufferedReader reader;
	private PrintWriter writer;
	private DataOutputStream dout;

	private PixelBenchmark bm = new PixelBenchmark();
	private PixelBenchmark fm = new PixelBenchmark();

	private Thread thread;
	private Task task;

	private boolean disconnected;
	private boolean connected;

	public SocketThread(Socket socket, Server server) {

		this.socket = socket;
		this.server = server;
		this.thread = new Thread(this);

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
		writer.flush();
	}

	private void sendMessage(String text) {
		System.out.println("SocketThread-" + thread.getId() + ": " + text);
		writer.println(text);
		writer.flush();
	}

	private void receiveMessage() {

		String input;
		StringTokenizer token;
		String compare;

		try {
			while (((input = (reader.readLine())) != null) && !Thread.currentThread().isInterrupted()) {
				bm.start();
				fm.start();
				token = new StringTokenizer(input, "/.../");
				compare = token.nextElement().toString();

				switch (compare) {
				case "connect":
					connect();
					break;
				case "task":
					sendTask();
					break;
				case "tick":
					server.setImage();
					break;
				case "frame":
					fm.stop();
					System.out.println("Total time frame (real): " + fm.getResult());
					break;
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
		connected = true;
		sendMessage("Connect success\n\0");
		server.connect();
	}

	private void disconnect() {
		if (connected) {
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
		sendMessage(task.getY());
		sendMessage(task.getxMove());
		sendMessage(task.getyMove());
		sendMessage(task.getZoom());
		sendMessage(task.getItr());

	}

	private void plot(String compare) throws IOException {
		int x;
		int y;
		int itr;
		int colorItr = 20;
		x = Integer.parseInt(compare);
		y = Integer.parseInt(reader.readLine());
		itr = Integer.parseInt(reader.readLine());
		server.setRGB(x, y, itr | (itr << colorItr));
		bm.stop();
	}

	private void close() {
		System.out.println(Thread.currentThread().getName() + ": Connection Closing...");
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
		System.out.println("ServerThread beendet");

	}
}