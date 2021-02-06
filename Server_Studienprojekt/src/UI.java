import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class UI extends JFrame{

    /**
     * JPanels
     */
    public static JPanel contentPane = new JPanel();
    public static JPanel imgPicture;

    /**
     * JLabels
     */
    public static JLabel lblAnzClients;

    /**
     * JButtons
     */
    public static JButton btnZoomIn;
    public static JButton btnZoomOut;
    public static JButton btnRestart;
    public static JButton btnUp;
    public static JButton btnDown;
    public static JButton btnLeft;
    public static JButton btnRight;

    /**
     * screenSize
     */
    private final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public static int width = (int) screenSize.getWidth();
    public static int height = (int) screenSize.getHeight();

    /**
     * UI()
     */
    UI(){
        setTitle("Mandelbrot_Server_Java");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setExtendedState(6);
        setIconImage(new ImageIcon("picture/mandelbrot.png").getImage());
        setBounds(0, 0, width, height);
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        addWindowListener(new WindowListener());

//contentPane
        contentPane.setBackground(UIManager.getColor("blue"));
        contentPane.setLayout(null);
        contentPane.setFocusable(true);
        contentPane.addKeyListener(new KeyboardListener());
        setContentPane(contentPane);

//imgPicture
        imgPicture = new MethodsUI.imgPanel();
        imgPicture.addMouseMotionListener(new pictureListener.pictureMouseMotionListener());
        imgPicture.addMouseListener(new pictureListener.pictureMouseListener());
        contentPane.add(imgPicture);

        MethodsUI.plotValRe();

//btnRestart
        btnRestart = new JButton("Restart");
        btnRestart.addActionListener(new ButtonListener());
        btnRestart.setBounds((int) (width / 128.0), (int) (height / 1.1), (int) (width / 9.6), (int) (height / 25.6));
        btnRestart.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        //btnRestart.setEnabled(false);
        contentPane.add(btnRestart);

//btnZoomIn
        btnZoomIn = new JButton("Zoom in");
        btnZoomIn.addActionListener(new ButtonListener());
        btnZoomIn.setBounds((int) (width / 3.88), (int) (height / 1.1), (int) (width / 9.6), (int) (height / 25.6));
        btnZoomIn.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        //btnZoomIn.setEnabled(false);
        contentPane.add(btnZoomIn);

//btnZoomOut
        btnZoomOut = new JButton("Zoom out");
        btnZoomOut.addActionListener(new ButtonListener());
        btnZoomOut.setBounds((int) (width / 1.57), (int) (height / 1.1), (int) (width / 9.6), (int) (height / 25.6));
        btnZoomOut.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        //btnZoomOut.setEnabled(false);
        contentPane.add(btnZoomOut);

//btnLeft
        btnLeft = new JButton("Left");
        btnLeft.addActionListener(new ButtonListener());
        btnLeft.setBounds((int) ((width / 2.0) - (int) (width / 8.5)), (int) (height / 1.1), (int) (width / (8.5*2)), (int) (height / 25.6));
        btnLeft.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        //btnLeft.setEnabled(false);
        contentPane.add(btnLeft);

//btnUp
        btnUp = new JButton("Up");
        btnUp.addActionListener(new ButtonListener());
        btnUp.setBounds((int) ((width / 2.0) - (int) (width / (8.5*2))), (int) (height / 1.1), (int) (width / (8.5*2)), (int) (height / 25.6));
        btnUp.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        //btnUp.setEnabled(false);
        contentPane.add(btnUp);

//btnDown
        btnDown = new JButton("Down");
        btnDown.addActionListener(new ButtonListener());
        btnDown.setBounds((int) (width / 2.0), (int) (height / 1.1), (int) (width / (8.5*2)), (int) (height / 25.6));
        btnDown.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        //btnDown.setEnabled(false);
        contentPane.add(btnDown);

//btnRight
        btnRight = new JButton("Right");
        btnRight.addActionListener(new ButtonListener());
        btnRight.setBounds((int) ((width / 2.0) + (int) (width / (8.5*2))), (int) (height / 1.1), (int) (width / (8.5*2)), (int) (height / 25.6));
        btnRight.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        //btnRight.setEnabled(false);
        contentPane.add(btnRight);

//lblAnzClients
        lblAnzClients = new JLabel("anzClients: 0");
        lblAnzClients.setBounds(width - (int) (width / 9.6) - 10, (int) (height / 1.1), (int) (width / 9.6), (int) (height / 25.6));
        lblAnzClients.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        contentPane.add(lblAnzClients);
    }
}