package Panels;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MonitorPanel extends JPanel {

	private final int WIDTH;
	private final int HEIGHT;
	private final Dimension DIMENSION;

	private JLabel number_of_clients;
	private JLabel frames_per_second;
	private JLabel packages_per_second;

	public MonitorPanel(int width, int height) {

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
		frames_per_second = new JLabel(String.format("FPS: %d", 0));
		packages_per_second = new JLabel(String.format("Packages: %d/s", 0));

		number_of_clients.setForeground(Color.WHITE);
		frames_per_second.setForeground(Color.WHITE);
		packages_per_second.setForeground(Color.WHITE);

		add(number_of_clients);
		add(frames_per_second);
		add(packages_per_second);
	}

	public void setNumberOfClients(int number) {
		number_of_clients.setText(String.format("Clients: %d", number));
	}

	public void setFPS(int number) {
		frames_per_second.setText(String.format("FPS: %d", number));
	}

	public void setPackagesPerSecond(int number) {
		packages_per_second.setText(String.format("Packages: %d/s", number));
	}
}
