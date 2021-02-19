package Server;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.JFrame;

import Benchmarks.PixelBenchmark;

public class SocketThread implements Runnable {

	private BufferedReader br;
	private PrintWriter os;
	private Socket socket;
	private Server server;
	private PixelBenchmark bm = new PixelBenchmark();
	private PixelBenchmark tm = new PixelBenchmark();
	private PixelBenchmark fm = new PixelBenchmark();
	private Thread thread;

	private int anzClients;
	private int runningClients;
	private int yourNumber;

	private int i = 0;

	/**
	 * ServerThread()
	 * 
	 * @param s Socket
	 */
	SocketThread(Socket s, Server server) {
		this.server = server;
		this.thread = new Thread(this);
		thread.start();
		socket = s;
	}

	/**
	 * sendMessageText() in MethodsUI.java, KeyboardListener.java,
	 * ButtonListener.java benoetigt
	 * 
	 * @param text String
	 */
	public void sendMessageText(String text) {
		System.out.println("text (ServerThread): " + text);
		try {
			if (text.equals("disconnect") || text.equals("close")) {
				for (Socket socket : server.getListSocket()) {
					MethodsServerThread.sendMessage(new PrintWriter(socket.getOutputStream()), text);
				}
			} else {
				for (Socket socket : server.getListRunning()) {
					MethodsServerThread.sendMessage(new PrintWriter(socket.getOutputStream()), text);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(text);
	}

	/**
	 * receiveMessage()
	 * 
	 * @throws IOException
	 */
	private synchronized void receiveMessage() {

		String input;
		StringTokenizer token;
		String compare;

		try {

			br = new BufferedReader((new InputStreamReader(socket.getInputStream())));
			os = new PrintWriter(socket.getOutputStream());

			while (((input = (br.readLine())) != null) && !Thread.currentThread().isInterrupted()) {

				bm.start();
				tm.start();
				fm.start();
				token = new StringTokenizer(input, "/.../");
				compare = token.nextElement().toString();

				switch (compare) {
//				case "connect":
//					connect();
//					break;
//				case "start":
//					start();
//					break;
//				case "pause":
//					pause();
//					break;
//				case "resume":
//					resume();
//					break;
				case "tick":
					tm.stop();
					server.setImage();
					// System.out.println("Total time tick: " + tm.getResult());
					break;
				case "frame":
					fm.stop();
					System.out.println("Total time frame (real): " + fm.getResult());
					break;
				default:
					plot(input);
					// case "close":
					// close(token);
					// break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	public void connect() throws IOException {
//
//		MethodsServerThread.sendMessage(os, "connect response from the server");
//		anzClients = server.getListSocket().size();
//		yourNumber = server.getListSocket().size();
//		UserInterface.lblAnzClients.setText("anzClients: " + anzClients);
//		System.out.println("anzClients: " + anzClients);
//
//		if (anzClients == 1) {
//			MethodsUI.I = new BufferedImage(UserInterface.imgPicture.getWidth(), UserInterface.imgPicture.getHeight(),
//					BufferedImage.TYPE_INT_RGB);
//			UserInterface.btnRestart.setEnabled(true);
//			UserInterface.btnZoomIn.setEnabled(true);
//			UserInterface.btnZoomOut.setEnabled(true);
//			UserInterface.btnLeft.setEnabled(true);
//			UserInterface.btnRight.setEnabled(true);
//			UserInterface.btnUp.setEnabled(true);
//			UserInterface.btnDown.setEnabled(true);
//		}
//		for (Socket socket : server.getListSocket()) {
//			PrintWriter os = new PrintWriter(socket.getOutputStream());
//			MethodsServerThread.sendMessage(os, "anzClients/.../" + anzClients);
//		}
//		MethodsServerThread.sendMessage(os, "yourNumber/.../" + yourNumber);
//
//		/*
//		 * if(listName.contains(stringBytes[1])){ i++; listName.add(stringBytes[1] + "_"
//		 * + i); }else { listName.add(stringBytes[1]); } for (String name : listName) {
//		 * System.out.println("Name: " + name); }//
//		 */
//	}

//	public void start() {
//		runningClients++;
//		server.getListRunning().add(socket);
//		MethodsServerThread.sendMessage(os,
//				"size/.../" + UserInterface.imgPicture.getWidth() + "/.../" + UserInterface.imgPicture.getHeight());
//		MethodsServerThread.sendMessage(os, "anzRunning/.../" + server.getListRunning().size());
//		System.out.println("runningClients: " + runningClients);
//	}

//	public void pause() {
//		/*
//		 * for(Socket socket : listRunning){ sendMessage(new
//		 * PrintWriter(socket.getOutputStream()), "pauseChange"); }//
//		 */
//		runningClients--;
//		server.getListRunning().remove(socket);
//		MethodsServerThread.sendMessage(os, "pause");
//	}

//	public void resume() {
//		runningClients++;
//		server.getListRunning().add(socket);
//		MethodsServerThread.sendMessage(os, "resume");
//		/*
//		 * for(Socket socket : server.getListRunning()){ sendMessage(new
//		 * PrintWriter(socket.getOutputStream()), "resumeChange"); }//
//		 */
//	}

	public void plot(String compare) throws IOException {
		int x;
		int y;
		int itr;
		int colorItr = 20;
		x = Integer.parseInt(compare);
		y = Integer.parseInt(br.readLine());
		itr = Integer.parseInt(br.readLine());
		// bm.stop();
//		System.out.println("Total time per pixel: " + bm.getResult());
//		System.out.println("Average time per pixel: " + bm.getAvg());
		server.setRGB(x, y, itr | (itr << colorItr));
		bm.stop();
	}

	public long getId() {
		return thread.getId();
	}

//	public void close(Object obj) {
//		check = false;
//		Socket so;
//		if (obj instanceof Socket) {
//			so = (Socket) obj;
//		} else {
//			so = socket;
//		}
//		System.out.println(Thread.currentThread().getName() + ": Connection Closing...");
//		System.out.println("Server.listSocket.size(): " + server.getListSocket().size());
//		int z = server.getListSocket().indexOf(so);
//		if (z != -1) {
//			System.out.println("Server.listSocket.remove: " + so);
//			server.getListSocket().remove(so);
//			/*
//			 * System.out.println("Server.listIP.remove: " +
//			 * so.getInetAddress().getHostAddress());
//			 * Server.listIP.remove(so.getInetAddress().getHostAddress());//
//			 */
//			/*
//			 * System.out.println("Server.listName.remove: " + Server.listName.get(z));
//			 * Server.listName.remove(z);//
//			 */
//		}
//		if (server.getListRunning().contains(so)) {
//			System.out.println("server.getListRunning().remove: " + so);
//			server.getListRunning().remove(so);
//		} else {
//			System.out.println("server.getListRunning().remove: no socket removed");
//		}
//
//		server.getLock().lock();
//		System.out.println("server.getListUnchecked().remove: " + so + "; server.getListUnchecked().size (vor remove): "
//				+ server.getListUnchecked().size());
//		server.getListUnchecked().remove(so);
//		anzClients = server.getListSocket().size();
//		runningClients = server.getListRunning().size();
//		yourNumber--;
//		if (i > 0) {
//			i--;
//		}
//		UserInterface.lblAnzClients.setText("anzClients: " + anzClients);
//		server.getLock().unlock();
//		if (anzClients == 0) {
//			// UI.I = null;
//			UserInterface.btnRestart.setEnabled(false);
//			UserInterface.btnZoomIn.setEnabled(false);
//			UserInterface.btnZoomOut.setEnabled(false);
//			UserInterface.btnLeft.setEnabled(false);
//			UserInterface.btnRight.setEnabled(false);
//			UserInterface.btnUp.setEnabled(false);
//			UserInterface.btnDown.setEnabled(false);
//			// UI.btnEnd.setEnabled(false);
//			i = 0;
//			try {
//				br.close();
//				System.out.println("BufferedReader closed: " + br.toString());
//				os.close();
//				System.out.println("OutputStream closed: " + os.toString());
//				socket.close();
//				System.out.println("Socket closed: " + socket.toString());
//				so.close();
//				System.out.println("Socket closed: " + so.toString());
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		try {
//			for (Socket socket : server.getListSocket()) {
//				PrintWriter os = new PrintWriter(socket.getOutputStream());
//				MethodsServerThread.sendMessage(os, "close");
//				// System.out.println("Response to Client (" + socket.getInetAddress() + "): " +
//				// line);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		Thread.currentThread().interrupt();
//	}

	/**
	 * run()
	 */
	public void run() {
		receiveMessage();
		System.out.println("ServerThread beendet");
	}
}