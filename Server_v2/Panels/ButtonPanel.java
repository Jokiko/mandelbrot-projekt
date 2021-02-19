package Panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ButtonPanel extends JPanel {

	private JButton btnRestart;
	private JButton btnZoomIn;
	private JButton btnLeft;
	private JButton btnUp;
	private JButton btnDown;
	private JButton btnRight;
	private JButton btnZoomOut;

	private final int WIDTH;
	private final int HEIGHT;
	private final Dimension DIMENSION;

	public ButtonPanel(int width, int height) {

		this.WIDTH = width;
		this.HEIGHT = height;
		this.DIMENSION = new Dimension(WIDTH, HEIGHT);

		setBorder(BorderFactory.createLineBorder(Color.WHITE));

		setMinimumSize(DIMENSION);
		setPreferredSize(DIMENSION);
		setMaximumSize(DIMENSION);

		setLayout(new GridBagLayout());
		setBackground(Color.BLACK);

		initializeButtons();
		addButtons();
	}

	private void initializeButtons() {

		// Divide width by number of buttons
		Dimension buttonSize = new Dimension( (WIDTH) / 7, HEIGHT);

		btnRestart = new JButton("Restart");
		btnZoomIn = new JButton("Zoom In");
		btnLeft = new JButton("Left");
		btnUp = new JButton("Up");
		btnDown = new JButton("Down");
		btnRight = new JButton("Right");
		btnZoomOut = new JButton("Zoom Out");
		
		btnRestart.setMinimumSize(buttonSize);
		btnZoomIn.setMinimumSize(buttonSize);
		btnLeft.setMinimumSize(buttonSize);
		btnUp.setMinimumSize(buttonSize);
		btnDown.setMinimumSize(buttonSize);
		btnRight.setMinimumSize(buttonSize);
		btnZoomOut.setMinimumSize(buttonSize);

		btnRestart.setPreferredSize(buttonSize);
		btnZoomIn.setPreferredSize(buttonSize);
		btnLeft.setPreferredSize(buttonSize);
		btnUp.setPreferredSize(buttonSize);
		btnDown.setPreferredSize(buttonSize);
		btnRight.setPreferredSize(buttonSize);
		btnZoomOut.setPreferredSize(buttonSize);

		btnRestart.setMaximumSize(buttonSize);
		btnZoomIn.setMaximumSize(buttonSize);
		btnLeft.setMaximumSize(buttonSize);
		btnUp.setMaximumSize(buttonSize);
		btnDown.setMaximumSize(buttonSize);
		btnRight.setMaximumSize(buttonSize);
		btnZoomOut.setMaximumSize(buttonSize);

	}

	private void addButtons() {
		add(btnRestart);
		add(btnZoomIn);
		add(btnLeft);
		add(btnUp);
		add(btnDown);
		add(btnRight);
		add(btnZoomOut);

	}
}
