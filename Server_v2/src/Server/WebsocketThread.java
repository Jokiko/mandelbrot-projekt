package src.Server;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.swing.JFrame;
//
//public class ServerThreadWebSocket extends JFrame implements Runnable {
//	private Server server;
//	private PrintWriter os = null;
//	private InputStream is = null;
//	private Socket socket;
//
//	private boolean check;
//	private int anzClients;
//	private int runningClients;
//	private int yourNumber;
//
//	private static int i = 0;
//
//	/**
//	 * ServerThread()
//	 * 
//	 * @param s Socket
//	 */
//	ServerThreadWebSocket(Socket socket, Server server) {
//		this.server = server;
//		this.socket = socket;
//		check = true;
//	}
//
//	/**
//	 * sendMessageText() in MethodsUI.java, KeyboardListener.java,
//	 * ButtonListener.java benoetigt
//	 * 
//	 * @param text String
//	 */
//	public void sendMessageText(String text) {
//		System.out.println("text (ServerThreadWebSocket): " + text);
//		try {
//			if (text.equals("disconnect") || text.equals("close")) {
//				for (Socket socket : server.getListSocket()) {
//					MethodsServerThread.sendMessage(new PrintWriter(socket.getOutputStream()), text);
//				}
//			} else {
//				for (Socket socket : server.getListRunning()) {
//					MethodsServerThread.sendMessage(new PrintWriter(socket.getOutputStream()), text);
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println(text);
//	}
//
//	/**
//	 * receiveMessage()
//	 */
//	private synchronized void receiveMessage() {
//		try {
//			is = socket.getInputStream();
//			os = new PrintWriter(socket.getOutputStream());
//		} catch (Exception e) {
//			System.out.println("Error in serverThreadWebSocket");
//		}
//		String line = null;
//		String[] stringBytes;
//		int length;
//		int buffLength = 1024;
//		byte[] b = new byte[buffLength];
//		try {
//			while (check && !Thread.currentThread().isInterrupted()) {
//				System.out.println(Thread.currentThread().getName());
//				// Laenge der erhaltenen verschlüsselten Nachricht
//				length = is.read(b); // blockt
//
//				// Dekodierung der WebSocket Nachricht
//				if (length != -1) {
//					byte rLength;
//					int rMaskIndex = 2;
//					int rDataStart;
//					// Hier fehlt eventuell noch ein Check, ob b[0] etwas anderes als Text ist.
//					byte data = b[1];
//					byte op = (byte) 127;
//					// Check, wo die Laenge geschrieben ist, wenn <=125 dann ist das die Laenge
//					rLength = (byte) (data & op);
//					// wenn 126, dann steht in den naechsten 16 bit die Laenge
//					if (rLength == (byte) 126)
//						rMaskIndex = 4;
//					// wenn 127, dann steht in den naechsten 64 bit die Laenge
//					if (rLength == (byte) 127)
//						rMaskIndex = 10;
//
//					// auf die Laenge folgt ein 4 bit langer mask key, der zum dekodieren benoetigt
//					// wird
//					byte[] masks = new byte[4];
//
//					int j = 0;
//					int i;
//					for (i = rMaskIndex; i < (rMaskIndex + 4); i++) {
//						masks[j] = b[i];
//						j++;
//					}
//
//					// auf den masking key folgen die verschluesselten Daten
//					rDataStart = rMaskIndex + 4;
//
//					int messLen = length - rDataStart;
//
//					byte[] message = new byte[messLen];
//
//					// Entschluesslung der Daten
//					for (i = rDataStart, j = 0; i < length; i++, j++) {
//						message[j] = (byte) (b[i] ^ masks[j % 4]);
//					}
//
//					line = new String(message, StandardCharsets.UTF_8);
//					b = new byte[buffLength];
//					System.out.println("b_ende: " + Thread.currentThread().getName() + "; " + Arrays.toString(b));
//				}
//
//				if (line != null) {
//					stringBytes = line.split("/.../");
//
//					System.out.println("in if : " + line);
//
//					if (stringBytes[0].equals("connect")) {
//						MethodsServerThread.sendMessage(os, "connect response from the server");
//						anzClients = server.getListSocket().size();
//						yourNumber = server.getListSocket().size();
//						UserInterface.lblAnzClients.setText("anzClients: " + anzClients);
//						System.out.println("anzClients: " + anzClients);
//						if (anzClients == 1) {
//							MethodsUI.I = new BufferedImage(UserInterface.imgPicture.getWidth(),
//									UserInterface.imgPicture.getHeight(), BufferedImage.TYPE_INT_RGB);
//							UserInterface.btnRestart.setEnabled(true);
//							UserInterface.btnZoomIn.setEnabled(true);
//							UserInterface.btnZoomOut.setEnabled(true);
//							UserInterface.btnLeft.setEnabled(true);
//							UserInterface.btnRight.setEnabled(true);
//							UserInterface.btnUp.setEnabled(true);
//							UserInterface.btnDown.setEnabled(true);
//						}
//						for (Socket socket : server.getListSocket()) {
//							PrintWriter os = new PrintWriter(socket.getOutputStream());
//							MethodsServerThread.sendMessage(os, "anzClients/.../" + anzClients);
//						}
//						MethodsServerThread.sendMessage(os, "yourNumber/.../" + yourNumber);
//
//						/*
//						 * if(listName.contains(stringBytes[1])){ i++; listName.add(stringBytes[1] + "_"
//						 * + i); }else { listName.add(stringBytes[1]); } for (String name : listName) {
//						 * System.out.println("Name: " + name); }//
//						 */
//					}
//
//					if (server.getListTypes().contains(stringBytes[0])) {
//						int x = Integer.parseInt(stringBytes[1]);
//						int y = Integer.parseInt(stringBytes[2]);
//						int itr = Integer.parseInt(stringBytes[3]);
//						int colorItr = 20;
//						MethodsUI.I.setRGB(x, y, itr | (itr << colorItr));
//						validate();
//						repaint();
//					}
//
//					if (stringBytes[0].equals("start")) {
//						runningClients++;
//						server.getListRunning().add(socket);
//						MethodsServerThread.sendMessage(os, "size/.../" + UserInterface.imgPicture.getWidth() + "/.../"
//								+ UserInterface.imgPicture.getHeight());
//						System.out.println("runningClients: " + runningClients);
//					}
//
//					if (stringBytes[0].equals("pause")) {
//						/*
//						 * for(Socket socket : listRunning){ sendMessage(new
//						 * PrintWriter(socket.getOutputStream()), "pauseChange"); }//
//						 */
//						runningClients--;
//						server.getListRunning().remove(socket);
//						MethodsServerThread.sendMessage(os, "pause");
//					}
//
//					if (stringBytes[0].equals("resume")) {
//						runningClients++;
//						server.getListRunning().add(socket);
//						MethodsServerThread.sendMessage(os, "resume");
//						/*
//						 * for(Socket socket : server.getListRunning()){ sendMessage(new
//						 * PrintWriter(socket.getOutputStream()), "resumeChange"); }//
//						 */
//					}
//
//					if (stringBytes[0].equals("close")) {
//						close(stringBytes[1]);
//					}
//
//					if (stringBytes[0].equals("check")) {
//						server.getLock().lock();
//						server.getListUnchecked().remove(socket);
//						server.getLock().unlock();
//					}
//				} else {
//					close("" + null);
//					check = false;
//				}
//			}
//		} catch (IOException ioe) {
//			check = false;
//			line = Thread.currentThread().getName(); // reused String line for getting thread name
//			System.out.println("IOException/ Client " + line + " terminated abruptly");
//			ioe.printStackTrace();
//		} catch (NullPointerException npe) {
//			check = false;
//			line = Thread.currentThread().getName(); // reused String line for getting thread name
//			System.out.println("NullPointerException/ Client " + line + " Closed");
//			npe.printStackTrace();
//		}
//	}
//
//	/**
//	 * close()
//	 * 
//	 * @param obj Object
//	 */
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
//			System.out.println("listRunning.remove: no socket removed");
//		}
//		if (server.getListWebSocket().contains(so)) {
//			System.out.println("server.getListWebSocket().remove: " + so);
//			server.getListWebSocket().remove(so);
//		} else {
//			System.out.println("server.getListWebSocket().remove: no socket removed");
//		}
//
//		server.getLock().lock();
//		System.out.println("Server.listUnchecked.remove: " + so + "; Server.listUnchecked.size (vor remove): "
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
//				is.close();
//				System.out.println("InputStream closed: " + is.toString());
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
//
//	/**
//	 * run()
//	 */
//	public void run() {
//		receiveMessage();
//		System.out.println("ServerThread beendet");
//	}
//}