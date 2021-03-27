package src.Panels;

import src.Server.Server;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MonitorPanel extends JPanel {

	private final Server server;
	private final int WIDTH;
	private final int HEIGHT;
	private final Dimension DIMENSION;

	private final JLabel number_of_clients;
	private final JLabel noneClients;
	private final JLabel number_android_clients;
	private final JLabel number_cuda_clients;
	private final JLabel number_webSocket_clients;
	private final JLabel noneItr;
	private final JLabel number_iterations;

	private volatile int anzAndroidClients, anzCudaClients, anzWebSocketClients;

	public MonitorPanel(Server server,int width, int height) {
		this.server = server;
		this.WIDTH = width;
		this.HEIGHT = height;
		this.DIMENSION = new Dimension(WIDTH, HEIGHT);

		setBorder(BorderFactory.createLineBorder(Color.WHITE));
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setMinimumSize(DIMENSION);
		setPreferredSize(DIMENSION);
		setMaximumSize(DIMENSION);

		setBackground(Color.BLACK);

		number_of_clients = new JLabel(String.format("Clients: %d", 0));
		noneClients = new JLabel(" ");
		number_android_clients = new JLabel(String.format("Number of Android Clients: %d", 0));
		number_cuda_clients = new JLabel(String.format("Number of Cuda Clients: %d", 0));
		number_webSocket_clients = new JLabel(String.format("Number of WebSocket Clients: %d", 0));
		noneItr = new JLabel(" ");
		number_iterations = new JLabel(String.format("Iterations: %d", 200));

		number_of_clients.setForeground(Color.WHITE);
		noneClients.setForeground(Color.WHITE);
		number_android_clients.setForeground(Color.WHITE);
		number_cuda_clients.setForeground(Color.WHITE);
		number_webSocket_clients.setForeground(Color.WHITE);
		noneItr.setForeground(Color.WHITE);
		number_iterations.setForeground(Color.WHITE);

		add(number_of_clients);
		add(noneClients);
		add(number_android_clients);
		add(number_cuda_clients);
		add(number_webSocket_clients);
		add(noneItr);
		add(number_iterations);

		this.server.setNumberIterations(number_iterations);
	}

	public void setNumberOfClients(int number) {
		number_of_clients.setText(String.format("Clients: %d", number));
	}
	public void setNumberOfAndroidClients(int number) {
		anzAndroidClients += number;
		number_android_clients.setText(String.format("Number of Android Clients: %d", anzAndroidClients));
	}
	public void setNumberOfCudaClients(int number) {
		anzCudaClients += number;
		number_cuda_clients.setText(String.format("Number of Cuda Clients: %d", anzCudaClients));
	}
	public void setNumberOfWebSocketClients(int number) {
		anzWebSocketClients += number;
		number_webSocket_clients.setText(String.format("Number of WebSocket Clients: %d", anzWebSocketClients));
	}

}
