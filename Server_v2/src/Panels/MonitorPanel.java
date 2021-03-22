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
	private final JLabel number_iterations;

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
		number_iterations = new JLabel(String.format("Iterations: %d", 200));

		number_of_clients.setForeground(Color.WHITE);
		number_iterations.setForeground(Color.WHITE);

		add(number_of_clients);
		add(number_iterations);

		this.server.setNumberIterations(number_iterations);
	}

	public void setNumberOfClients(int number) {
		number_of_clients.setText(String.format("Clients: %d", number));
	}
	
}
