package src.View;

import java.awt.Component;

import javax.swing.*;

import src.Listener.ButtonListener;
import src.Listener.KeyboardListener;
import src.Listener.MandelbrotMouseListener;
import src.Listener.WindowListener;
import src.Mandelbrot.MandelbrotImage;
import src.Panels.ButtonPanel;
import src.Panels.ContentPanel;
import src.Panels.MandelbrotPanel;
import src.Panels.MonitorPanel;
import src.Server.Server;

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
	 * "server" serves as controller
	 */
	private Server server;

	/*
	 * "contentPanel" overwrites the default content pane of ServerView and contains
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
	 * interactions
	 */
	private KeyboardListener keyboardListener;

	/*
	 * "mouseLister" is added to "mandelbrotPanel" to enable mouse and mouse-motion
	 * interactions
	 */
	private MandelbrotMouseListener mouseListener;

	/*
	 * "windowListener" is added to "ServerView" JFrame and listens for closing
	 * events
	 */
	private WindowListener windowListener;

	/*
	 * Mandelbrot resolution
	 */
	private int MANDELBROT_PANEL_WIDTH;
	private int MANDELBROT_PANEL_HEIGHT;

	/*
	 * "buttonPanel" size (depends on mandelbrot resolution)
	 */
	private int BUTTON_PANEL_WIDTH;
	private final int BUTTON_PANEL_HEIGHT = 50;

	/*
	 * "monitorPanel" size (depends on mandelbrot resolution)
	 */
	private final int MONITOR_PANEL_WIDTH = 200;
	private int MONITOR_PANEL_HEIGHT;

	/*
	 * total screen size needed (depends on all components)
	 */
	private int FRAME_WIDTH;
	private int FRAME_HEIGHT;

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
	 * Determines how big a GridBagLayout cell is (panel_width / grid_width =
	 * cell_width) (panel_height / grid_height = cell_height)
	 * 
	 */
	private final int GRID_WIDTH = 100;
	private final int GRID_HEIGHT = 100;

	/**
	 * Creates a new ServerView.
	 */
	public ServerView(Server server, int width, int height) {
		super("Mandelbrot_Server_Java");
		this.server = server;

		setVariables(width, height);

		initiate();
		setupFrame();
		addMandelbrotPanel();
		addButtonPanel();
		addMonitorPanel();

		pack();

	}

	/*
	 * sets the variables
	 */
	private void setVariables(int width, int height){

		MANDELBROT_PANEL_WIDTH = width;
		MANDELBROT_PANEL_HEIGHT = height;

		FRAME_WIDTH = width + MONITOR_PANEL_WIDTH;
		FRAME_HEIGHT = height + BUTTON_PANEL_HEIGHT;

		BUTTON_PANEL_WIDTH = width;
		MONITOR_PANEL_HEIGHT = height;
	}

	/*
	 * Initiates
	 */
	private void initiate() {

		contentPanel = new ContentPanel(FRAME_WIDTH, FRAME_HEIGHT);
		mandelbrotPanel = new MandelbrotPanel(MANDELBROT_PANEL_WIDTH, MANDELBROT_PANEL_HEIGHT);
		buttonPanel = new ButtonPanel(BUTTON_PANEL_WIDTH, BUTTON_PANEL_HEIGHT);
		monitorPanel = new MonitorPanel(MONITOR_PANEL_WIDTH, MONITOR_PANEL_HEIGHT);

		buttonListener = new ButtonListener(server);
		keyboardListener = new KeyboardListener(server);
		mouseListener = new MandelbrotMouseListener(server, mandelbrotPanel);
		windowListener = new WindowListener(server);

	}

	/*
	 * Calls JFrame methods to setup our Window
	 */
	private void setupFrame() {

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addWindowListener(windowListener);
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setResizable(false);
		setLocationRelativeTo(null);
		setIconImage(new ImageIcon("./picture/mandelbrot.png").getImage());
		setContentPane(contentPanel);

	}

	/*
	 * Adds the "mandelbrotPanel" as the top-left placed component (offset (0,0))
	 * and calculates the correct offsets of its direct neighbor components.
	 */
	private void addMandelbrotPanel() {

		int grid_width = MANDELBROT_PANEL_WIDTH / GRID_WIDTH;
		int grid_height = MANDELBROT_PANEL_HEIGHT / GRID_HEIGHT;

		mandelbrotPanel.addMouseListener(mouseListener);
		mandelbrotPanel.addMouseMotionListener(mouseListener);
		mandelbrotPanel.addMouseWheelListener(mouseListener);
		mandelbrotPanel.addKeyListener(keyboardListener);

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

	public void enableButtons() {
		buttonPanel.enableAll();
	}

	public void disableButtons() {
		buttonPanel.disableAll();
	}

	public void setImage(MandelbrotImage image) {
		mandelbrotPanel.setImage(image);
	}

	public void setNumberOfClients(int number) {
		monitorPanel.setNumberOfClients(number);
	}

	public int getMandelbrotWidth() {
		return MANDELBROT_PANEL_WIDTH;
	}

	public int getMandelbrotHeight() {
		return MANDELBROT_PANEL_HEIGHT;
	}

}
