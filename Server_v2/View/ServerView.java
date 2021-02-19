package View;

import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import Listener.ButtonListener;
import Listener.KeyboardListener;
import Listener.WindowListener;
import Panels.ButtonPanel;
import Panels.ContentPanel;
import Panels.MandelbrotPanel;
import Panels.MonitorPanel;;

/**
 * The class <code>ServerView</code> inherits from <code>JFrame</code>. It
 * creates the necessary UI components and adjusts there position. The default
 * content pane is overwritten by <code>ContentPanel</code>
 * 
 * @author randy
 *
 */

public class ServerView extends JFrame {

	/*
	 * "contenPanel" overwrites the default content pane of ServerView and contains
	 * every other panel created.
	 */
	private ContentPanel contentPanel;
	/*
	 * "mandelbrotPanel" displays the calculated mandelbrot set
	 */
	private MandelbrotPanel mandelbrotPanel;
	/*
	 * "buttonPanel" contains every button to navigate through the mandelbrot set
	 */
	private ButtonPanel buttonPanel;
	/*
	 * "monitorPanel" displays monitoring values
	 */
	private MonitorPanel monitorPanel;

	/*
	 * "buttonListener" is added to all buttons of "buttonPanel"
	 */

	private ButtonListener buttonListener;

	/*
	 * "keyboardListener" is added to "mandelbrotPanel" to enable keyboard
	 * navigation through the mandelbrot set
	 */

	private KeyboardListener keyboardListener;

	private BufferedImage image;

	/*
	 * Mandelbrot resolution
	 */

	private final int MANDELBROT_PANEL_WIDTH = 1000;
	private final int MANDELBROT_PANEL_HEIGHT = 1000;

	/*
	 * "buttonPanel" size (depends on mandelbrot resolution)
	 */
	private final int BUTTON_PANEL_WIDTH = MANDELBROT_PANEL_WIDTH;
	private final int BUTTON_PANEL_HEIGHT = 50;

	/*
	 * "monitorPanel" size (depends on mandelbrot resolution)
	 */

	private final int MONITOR_PANEL_WIDTH = 200;
	private final int MONITOR_PANEL_HEIGHT = MANDELBROT_PANEL_HEIGHT;

	/*
	 * total screen size needed (depends on all components)
	 */

	private final int FRAME_WIDTH = MANDELBROT_PANEL_WIDTH + MONITOR_PANEL_WIDTH;
	private final int FRAME_HEIGHT = MANDELBROT_PANEL_HEIGHT + BUTTON_PANEL_HEIGHT;

	/*
	 * GridBagLayout offsets (needed to align every component)
	 */

	private int mandelbrot_panel_offset_x = 0;
	private int mandelbrot_panel_offset_y = 0;
	private int button_panel_offset_x;
	private int button_panel_offset_y;
	private int monitor_panel_offset_x;
	private int monitor_panel_offset_y;

	/*
	 * Determines how big a GridBagLayout cell is
	 */

	private final int GRID_WIDTH = 100;
	private final int GRID_HEIGHT = 100;

	/**
	 * Creates a new ServerView.
	 */
	public ServerView() {
		super("Mandelbrot_Server_Java");

		instantiate();
		setupFrame();
		addMandelbrotPanel();
		addButtonPanel();
		addMonitorPanel();
		pack();

	}

	/*
	 * Initiates
	 */
	private void instantiate() {

		contentPanel = new ContentPanel(FRAME_WIDTH, FRAME_HEIGHT);
		mandelbrotPanel = new MandelbrotPanel(MANDELBROT_PANEL_WIDTH, MANDELBROT_PANEL_HEIGHT);
		buttonPanel = new ButtonPanel(BUTTON_PANEL_WIDTH, BUTTON_PANEL_HEIGHT);
		monitorPanel = new MonitorPanel(MONITOR_PANEL_WIDTH, MONITOR_PANEL_HEIGHT);
		image = new BufferedImage(MANDELBROT_PANEL_WIDTH, MANDELBROT_PANEL_HEIGHT, BufferedImage.TYPE_INT_RGB);

		buttonListener = new ButtonListener(mandelbrotPanel);
		keyboardListener = new KeyboardListener(mandelbrotPanel);

	}

	/*
	 * Calls JFrame methods to setup our Window
	 */

	private void setupFrame() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setResizable(false);
		setLocationRelativeTo(null);
		setIconImage(new ImageIcon("picture/mandelbrot.png").getImage());

		addWindowListener(new WindowListener());
		setContentPane(contentPanel);

	}

	/*
	 * Adds the "mandelbrotPanel" as the top-left placed component (offset (0,0))
	 * and calculates the correct offsets of its direct neighbor components.
	 */

	private void addMandelbrotPanel() {

		int grid_width = MANDELBROT_PANEL_WIDTH / GRID_WIDTH;
		int grid_height = MANDELBROT_PANEL_HEIGHT / GRID_HEIGHT;

		contentPanel.setLocation(mandelbrot_panel_offset_x, mandelbrot_panel_offset_y, grid_width, grid_height);
		contentPanel.addComponent(mandelbrotPanel);

		button_panel_offset_x = mandelbrot_panel_offset_x;
		button_panel_offset_y = mandelbrot_panel_offset_y + grid_height;

		monitor_panel_offset_x = mandelbrot_panel_offset_x + grid_width;
		monitor_panel_offset_y = mandelbrot_panel_offset_x;
	}

	/*
	 * Adds the "buttonPanel" beneath the "mandelbrotPanel" and adds the custom
	 * "buttonListener" to every button in "buttonPanel"
	 */
	private void addButtonPanel() {

		int grid_width = BUTTON_PANEL_WIDTH / GRID_WIDTH;
		int grid_height = BUTTON_PANEL_HEIGHT / GRID_HEIGHT;

		for (Component c : buttonPanel.getComponents()) {
			if (c instanceof JButton)
				((JButton) c).addActionListener(buttonListener);
		}

		contentPanel.setLocation(button_panel_offset_x, button_panel_offset_y, grid_width, grid_height);
		contentPanel.addComponent(buttonPanel);
	}

	/*
	 * Adds "monitorPanel" right next to the "mandelbrotPanel"
	 */

	private void addMonitorPanel() {

		int grid_width = MONITOR_PANEL_WIDTH / GRID_WIDTH;
		int grid_height = MONITOR_PANEL_HEIGHT / GRID_HEIGHT;

		contentPanel.setLocation(monitor_panel_offset_x, monitor_panel_offset_y, grid_width, grid_height);
		contentPanel.addComponent(monitorPanel);
	}

	/**
	 * @return MandelbrotPanel
	 */
	public MandelbrotPanel getMandelbrotPanel() {
		return mandelbrotPanel;
	}

	/**
	 * @return ButtonPanel
	 */
	public ButtonPanel getButtonPanel() {
		return buttonPanel;
	}

	/**
	 * @return MonitorPanel
	 */
	public MonitorPanel getMonitorPanel() {
		return monitorPanel;
	}

	public void setRGB(int x, int y, int value) {
		image.setRGB(x, y, value);
	}
	
	public void setImage() {
		mandelbrotPanel.setImage(image);
	}
}
