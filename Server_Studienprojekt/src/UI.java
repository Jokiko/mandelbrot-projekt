import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class UI extends JFrame{

    public static JPanel contentPane = new JPanel();
    public static JPanel imgPicture;

    public static JLabel lblAnzClients;

    public static JButton btnZoomIn;
    public static JButton btnZoomOut;
    public static JButton btnRestart;
    public static JButton btnUp;
    public static JButton btnDown;
    public static JButton btnLeft;
    public static JButton btnRight;
    public static JButton btnEnd;

    //public static JScrollPane scrollPane;

    private final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public static int width = (int) screenSize.getWidth();
    public static int height = (int) screenSize.getHeight();
    /*private int height_1 = (int) screenSize.getHeight();
    private int height = height_1 - (height_1 / 55); //minus Taskleiste//*/

    boolean click = false;

    public static BufferedImage I;
    int xMove, yMove = 0;
    double zoomX = 200;
    double zoomY = 200;
    /*double zx, zy, cx, cy, temp;
    int numItr = 50;
    int colorItr = 20;
    double zoomIncrease = 100;//*/

    int testX = 0;
    int testY = 0;

    //private int processors = Runtime.getRuntime().availableProcessors();

//restart
    private void restart(){
        if(ServerThread.runningClients > 0) {
            zoomX = 200;
            zoomY = 200;
            xMove = 0;
            yMove = 0;
            testX = 0;
            testY = 0;
            click = false;

            /*plotPoints();
            validate();
            repaint();//*/

            ServerThread.sendMessageText("restart/.../");
        }
    }

//ZoomIn
    private void zoomIn(double factor) {
        if(ServerThread.runningClients > 0) {
            zoomX *= (1 + factor);
            zoomY *= (1 + factor);

            //plotPoints();
            //I = new BufferedImage(UI.imgPicture.getWidth(), UI.imgPicture.getHeight(), BufferedImage.TYPE_INT_RGB);
            /*validate();
            repaint();//*/

            ServerThread.sendMessageText("zoomIn/.../" + factor);
            //ServerThread.sendMessageText("zoomIn/.../" + zoomX + "/.../" + zoomY);
        }
    }

//ZoomOut
    private void zoomOut(double factor){
        if(ServerThread.runningClients > 0) {
            zoomX *= (1 - factor);
            zoomY *= (1 - factor);

            /*plotPoints();
            validate();
            repaint();//*/

            ServerThread.sendMessageText("zoomOut/.../" + factor);
            //ServerThread.sendMessageText("zoomOut/.../" + zoomX + "/.../" + zoomY);
        }
    }

//Zoom with + and -
    public void Zoom(AWTEvent event){
        if(ServerThread.runningClients > 0) {
            if (event instanceof KeyEvent) {
                KeyEvent ke = (KeyEvent) event;
                if (btnZoomIn.isVisible() && ke.getKeyCode() == 521) {
                    zoomIn(0.05);
                } else {
                    if (btnZoomOut.isVisible() && ke.getKeyCode() == 45) {
                        zoomOut(0.05);
                    }
                }
            }
        }
    }

//imgPanel to portray the mandelbrot set
    public static class imgPanel extends JPanel{
        int imgWidth = width-20;//-250;
        int imgHeight = (int) ((height / 1.1) - (int) (height / 25.6));
        public imgPanel(){
            setBounds(10, 10, imgWidth, imgHeight);
        }

        @Override
        public void paint (Graphics g){
            super.paint(g);
            g.drawImage(I, 0, 0, this);
        }
    }//*/

//plotPoints: calculate the mandelbrot set
    /*private void plotPoints(){
        if(click){
            if(testX < (imgPicture.getWidth()/2.0)){
                xMove += imgPicture.getWidth()/2 - testX;
                System.out.println("testX < : " + testX);
            }else{
                xMove -= testX - imgPicture.getWidth()/2;
                System.out.println("testX >= : " + testX);
            }
            if(testY < (imgPicture.getHeight()/2.0)){
                yMove += imgPicture.getHeight()/2 - testY;
                System.out.println("testY < : " + testY);
            }else{
                yMove -= testY - imgPicture.getHeight()/2;
                System.out.println("testY > : " + testY);
            }
        }///
        I = new BufferedImage(imgPicture.getWidth(), imgPicture.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < imgPicture.getHeight(); y++) {
            for (int x = 0; x < imgPicture.getWidth(); x++) {
                zx = zy = 0;
                if(!click) {
                    if (xMove == 0 && yMove == 0) {
                        //cx = (x - 320 - xMove) / zoom;
                        cx = (x - (imgPicture.getWidth() / 2.0)) / zoomX; // Division mit 2, damit in der Mitte des Bildschirms
                        //cy = (y - 290 - yMove) / zoom;
                        cy = (y - (imgPicture.getHeight() / 2.0)) / zoomY; // Division mit 2, damit in der Mitte des Bildschirms
                    }else{
                        cx = (x - (imgPicture.getWidth() / 2.0) + xMove) / zoomX;
                        cy = (y - (imgPicture.getHeight() / 2.0) + yMove) / zoomY;
                    }
                }else{
                    if(testX < (imgPicture.getWidth()/2.0)){
                        cx = (x - (imgPicture.getWidth() / 2.0) - xMove) / zoomX;
                    }else{
                        cx = (x - (imgPicture.getWidth() / 2.0) + xMove) / zoomX;
                    }
                    if(testY < (imgPicture.getHeight()/2.0)){
                        cy = (y - (imgPicture.getHeight() / 2.0) - yMove) / zoomY;
                    }else{
                        cy = (y - (imgPicture.getHeight() / 2.0) + yMove) / zoomY;
                    }
                    //cx = (x - xMove) / zoom;
                    //cx = (x - (imgPicture.getWidth() / 2.0) + (xMove/2.0)) / zoomX;
                    //cx = (x - (imgPicture.getWidth() / 2.0) + (testX/2.0)) / zoomX;
                    //cx = (x - (xMove/2.0)) / zoomX;
                    //cy = (y - yMove) / zoom;
                    //cy = (y - (imgPicture.getHeight() / 2.0) + (yMove/2.0)) / zoomY;
                    //cy = (y - (imgPicture.getHeight() / 2.0) + (testY/2.0)) / zoomY;
                    //cy = (y - (yMove/2.0)) / zoomY;
                }///
                int itr = numItr;
                while (zx * zx + zy * zy < 4 && itr > 0) {
                    temp = zx * zx - zy * zy + cx;
                    zy = 2 * zx * zy + cy;
                    zx = temp;
                    itr--;
                }///
                I.setRGB(x, y, itr | (itr << colorItr));
            }
        }///
    }//*/

//drawX
    /*private void drawX(int x, int y){

        Graphics g = imgPicture.getGraphics();

        g.setColor(Color.WHITE);
        g.drawLine(x, y, x-5, y);
        g.drawLine(x, y, x+5, y);
        g.drawLine(x, y, x, y-5);
        g.drawLine(x, y, x, y+5);

    }//*/

//actionPerformed: btnUp, btnDown, btnLeft, btnRight
    private void actionPerformedButton(ActionEvent ae){
        if(ServerThread.runningClients > 0) {
            String event = ae.getActionCommand();

            switch (event) {
                case "Up":
                    yMove -= 50;
                    ServerThread.sendMessageText("Up/.../50");
                    //ServerThread.sendMessageText("Up/.../" + yMove);
                    break;
                case "Down":
                    yMove += 50;
                    ServerThread.sendMessageText("Down/.../50");
                    //ServerThread.sendMessageText("Down/.../" + yMove);
                    break;
                case "Left":
                    xMove -= 50;
                    ServerThread.sendMessageText("Left/.../50");
                    //ServerThread.sendMessageText("Left/.../" + xMove);
                    break;
                case "Right":
                    xMove += 50;
                    ServerThread.sendMessageText("Right/.../50");
                    //ServerThread.sendMessageText("Right/.../" + xMove);
                    break;
            }

            /*plotPoints();
            validate();
            repaint();//*/
        }
    }

//actionPerformed: Pfeiltasten and Escape (restart)
    private void actionPerformed(AWTEvent event){
        if(ServerThread.runningClients > 0) {
            if (event instanceof KeyEvent) {
                KeyEvent ke = (KeyEvent) event;
                int eventCode = ke.getKeyCode();

                switch (eventCode) {
                    case 38: //move up
                        yMove -= 25;
                        ServerThread.sendMessageText("Up/.../25");
                        //ServerThread.sendMessageText("Up/.../" + yMove);
                        break;
                    case 40: //move down
                        yMove += 25;
                        ServerThread.sendMessageText("Down/.../25");
                        //ServerThread.sendMessageText("Down/.../" + yMove);
                        break;
                    case 37: //move left
                        xMove -= 25;
                        ServerThread.sendMessageText("Left/.../25");
                        //ServerThread.sendMessageText("Left/.../" + xMove);
                        break;
                    case 39: //move right
                        xMove += 25;
                        ServerThread.sendMessageText("Right/.../25");
                        //ServerThread.sendMessageText("Right/.../" + xMove);
                        break;
                    case 27: //escape
                        restart();
                }

                /*plotPoints();
                validate();
                repaint();//*/
            }
        }
    }

//UI
    UI(){
        setTitle("Test_Server_Java");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setExtendedState(6);

        setBounds(0, 0, width, height);
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        contentPane.setBackground(UIManager.getColor("blue"));
        setContentPane(contentPane);
        contentPane.setLayout(null);

//imgPicture
        imgPicture = new imgPanel();
        imgPicture.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(ServerThread.runningClients > 0) {
                    click = true;
                    testX = e.getX();
                    testY = e.getY();
                    int middleWidth = imgPicture.getWidth() / 2;
                    int middleHeight = imgPicture.getHeight() / 2;
                    int moveX = e.getX() - middleWidth;
                    int moveY = e.getY() - middleHeight;
                    System.out.println("e.getX(): " + e.getX() + "; e.getY(): " + e.getY());
                    System.out.println("imgPicture.getWidth(): " + imgPicture.getWidth() + "; imgPicture.getHeight(): " + imgPicture.getHeight());
                    System.out.println("imgPicture Mitte width: " + imgPicture.getWidth() / 2 + "; imgPicture Mitte height: " + imgPicture.getHeight() / 2);
                    System.out.println("moveX: " + moveX + "; moveY: " + moveY);
                    //zoomIn(0.2);
                    ServerThread.sendMessageText("click/.../" + 0.2 + "/.../" + moveX + "/.../" + moveY);
                    click = false;
                }
            }
        });
        contentPane.add(imgPicture);

        /*plotPoints();
        validate();
        repaint();//*/

//scrollPane
        /*JPanel panel = new JPanel();
        panel.setSize(width-(width-220),  (int) ((height / 1.1) - (int) (height / 25.6)));
        panel.add(new JLabel("<html><body>Textzeile1<br>Textzeile2</body></html>"));

        scrollPane = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(width-230, 10, width-(width-220),  (int) ((height / 1.1) - (int) (height / 25.6)));
        contentPane.add(scrollPane);//*/

//btnRestart
        btnRestart = new JButton("Restart");
        btnRestart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                restart();
            }
        });
        btnRestart.setBounds((int) (width / 128.0), (int) (height / 1.1), (int) (width / 9.6), (int) (height / 25.6));
        btnRestart.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        btnRestart.setEnabled(false);
        contentPane.add(btnRestart);

//btnZoomIn
        btnZoomIn = new JButton("Zoom in");
        btnZoomIn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                zoomIn(0.2);
            }
        });
        btnZoomIn.setBounds((int) (width / 3.88), (int) (height / 1.1), (int) (width / 9.6), (int) (height / 25.6));
        btnZoomIn.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        btnZoomIn.setEnabled(false);
        contentPane.add(btnZoomIn);

//btnZoomOut
        btnZoomOut = new JButton("Zoom out");
        btnZoomOut.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                zoomOut(0.2);
            }
        });
        btnZoomOut.setBounds((int) (width / 1.57), (int) (height / 1.1), (int) (width / 9.6), (int) (height / 25.6));
        btnZoomOut.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        btnZoomOut.setEnabled(false);
        contentPane.add(btnZoomOut);

//ZoomIn mit Plustaste und ZoomOut mit Minustaste
        Toolkit tk = Toolkit.getDefaultToolkit();
        tk.addAWTEventListener(this::Zoom, AWTEvent.KEY_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);

//Bewegung mit Pfeiltasten
        tk.addAWTEventListener(this::actionPerformed, AWTEvent.KEY_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);

//btnLeft
        btnLeft = new JButton("Left");
        btnLeft.addActionListener(this::actionPerformedButton);
        btnLeft.setBounds((int) ((width / 2.0) - (int) (width / 8.5)), (int) (height / 1.1), (int) (width / (8.5*2)), (int) (height / 25.6));
        btnLeft.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        btnLeft.setEnabled(false);
        contentPane.add(btnLeft);

//btnUp
        btnUp = new JButton("Up");
        btnUp.addActionListener(this::actionPerformedButton);
        btnUp.setBounds((int) ((width / 2.0) - (int) (width / (8.5*2))), (int) (height / 1.1), (int) (width / (8.5*2)), (int) (height / 25.6));
        btnUp.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        btnUp.setEnabled(false);
        contentPane.add(btnUp);

//btnDown
        btnDown = new JButton("Down");
        btnDown.addActionListener(this::actionPerformedButton);
        btnDown.setBounds((int) (width / 2.0), (int) (height / 1.1), (int) (width / (8.5*2)), (int) (height / 25.6));
        btnDown.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        btnDown.setEnabled(false);
        contentPane.add(btnDown);

//btnRight
        btnRight = new JButton("Right");
        btnRight.addActionListener(this::actionPerformedButton);
        btnRight.setBounds((int) ((width / 2.0) + (int) (width / (8.5*2))), (int) (height / 1.1), (int) (width / (8.5*2)), (int) (height / 25.6));
        btnRight.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        btnRight.setEnabled(false);
        contentPane.add(btnRight);

//btnEnd
        btnEnd = new JButton("End");
        btnEnd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //if(btnEnd.isEnabled()) {
                    ServerThread.sendMessageText("disconnect");
                    System.exit(0);
                //}
            }
        });
        btnEnd.setBounds(width - (int) (width / 9.6) - 10, (int) (height / 1.1), (int) (width / 9.6), (int) (height / 25.6));
        btnEnd.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        //btnEnd.setEnabled(false);
        //contentPane.add(btnEnd);

//lblFPS
        lblAnzClients = new JLabel("anzClients: 0");
        lblAnzClients.setBounds(width - (int) (width / 9.6) - 10, (int) (height / 1.1), (int) (width / 9.6), (int) (height / 25.6));
        lblAnzClients.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        contentPane.add(lblAnzClients);
    }
}