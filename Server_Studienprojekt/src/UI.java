import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

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
     * fuer Mandelbrotmengenberechnung
     */
    public static BufferedImage I;
    int xMove, yMove = 0;
    double zoomX = 200;
    double zoomY = 200;
    double zx, zy, cx, cy, temp;
    int numItr = 50;
    int colorItr = 20;//*/

    /**
     * fuer Rechteck zum hereinzoomen in einen Bereich
     */
    boolean rectangle = false;
    boolean rightClicked = false;
    int widthRect = 0;
    int heightRect = 0;
    int startX = 0;
    int startY = 0;
    int endX = 0;
    int endY = 0;
    int middleRectX = 0;
    int middleRectY = 0;
    int middleImageX = 0;
    int middleImageY = 0;

    /**
     * restart()
     */
    private void restart(){
        //if(ServerThread.runningClients > 0) {
            zoomX = 200;
            zoomY = 200;
            xMove = 0;
            yMove = 0;
            widthRect = 0;
            heightRect = 0;
            startX = 0;
            startY = 0;
            endX = 0;
            endY = 0;
            middleRectX = 0;
            middleRectY = 0;
            middleImageX = 0;
            middleImageY = 0;

            plotValRe();

            //ServerThread.sendMessageText("restart/.../");
        //}
    }

    /**
     * zoomIn()
     * @param factor double
     */
    private void zoomIn(double factor) {
        //if(ServerThread.runningClients > 0) {

            zoomX *= (1 + factor);
            zoomY *= (1 + factor);

            xMove += xMove * factor;
            yMove += yMove * factor;

            plotValRe();

            /*if(!rectangle) {
                ServerThread.sendMessageText("zoomIn/.../" + factor);
            }else{
                ServerThread.sendMessageText("rectangle/.../" + xMove + "/.../" + yMove + "/.../" + factor);
            }//*/
        //}
    }

    /**
     * zoomOut()
     * @param factor double
     */
    private void zoomOut(double factor){
        //if(ServerThread.runningClients > 0) {
            zoomX *= (1 - factor);
            zoomY *= (1 - factor);

            xMove -= xMove * factor;
            yMove -= yMove * factor;

            plotValRe();

            //ServerThread.sendMessageText("zoomOut/.../" + factor);
        //}
    }

    /**
     * Zoom()
     * @param event AWTEvent
     */
    private void Zoom(AWTEvent event){
        //if(ServerThread.runningClients > 0) {
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
        //}
    }

    /**
     * actionPerformedButton()
     * btnUp, btnDown, btnLeft, btnRight
     * @param ae ActionEvent
     */
    private void actionPerformedButton(ActionEvent ae){
        //if(ServerThread.runningClients > 0) {
        String event = ae.getActionCommand();

        switch (event) {
            case "Up":
                yMove -= 50;
                //ServerThread.sendMessageText("Up/.../50");

                plotValRe();
                break;
            case "Down":
                yMove += 50;
                //ServerThread.sendMessageText("Down/.../50");

                plotValRe();
                break;
            case "Left":
                xMove -= 50;
                //ServerThread.sendMessageText("Left/.../50");

                plotValRe();
                break;
            case "Right":
                xMove += 50;
                //ServerThread.sendMessageText("Right/.../50");

                plotValRe();
                break;
            default:
                break;
        }
        //}
    }

    /**
     * actionPerformed()
     * Pfeiltasten and Escape (restart)
     * */
    private void actionPerformed(AWTEvent event){
        //if(ServerThread.runningClients > 0) {
        if (event instanceof KeyEvent) {
            int eventCode = ((KeyEvent) event).getKeyCode();

            switch (eventCode) {
                //move up
                case 38:
                    yMove -= 25;
                    //ServerThread.sendMessageText("Up/.../25");

                    plotValRe();
                    break;
                //move down
                case 40:
                    yMove += 25;
                    //ServerThread.sendMessageText("Down/.../25");

                    plotValRe();
                    break;
                //move left
                case 37:
                    xMove -= 25;
                    //ServerThread.sendMessageText("Left/.../25");

                    plotValRe();
                    break;
                //move right
                case 39:
                    xMove += 25;
                    //ServerThread.sendMessageText("Right/.../25");

                    plotValRe();
                    break;
                //escape
                case 27:
                    restart();
                    break;
                default:
                    break;
            }
        }
        //}
    }

    /**
     * imgPanel
     * stellt die Mandelbrotmenge dar
     */
    public static class imgPanel extends JPanel{
        int imgWidth = width-20;
        int imgHeight = (int) ((height / 1.1) - (int) (height / 25.6));
        public imgPanel(){
            setBounds(10, 10, imgWidth, imgHeight);
        }

        @Override
        public void paint (Graphics g){
            super.paint(g);
            g.drawImage(I, 0, 0, this);
        }
    }

    /**
     * plotPoints()
     * berechnet die Mandelbrotmenge
     */
    private void plotPoints(){
        I = new BufferedImage(imgPicture.getWidth(), imgPicture.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < I.getHeight(); y++) {
            for (int x = 0; x < I.getWidth(); x++) {
                zx = zy = 0;
                cx = (x - (I.getWidth() / 2.0) + xMove) / zoomX;
                cy = (y - (I.getHeight() / 2.0) + yMove) / zoomY;
                // jeweils Division mit 2, damit in der Mitte des Bildschirms
                int itr = numItr;
                while (zx * zx + zy * zy < 4 && itr > 0) {
                    temp = zx * zx - zy * zy + cx;
                    zy = 2 * zx * zy + cy;
                    zx = temp;
                    itr--;
                }
                I.setRGB(x, y, itr | (itr << colorItr));
            }
        }
        // zeichnet Kreuz ueber das Bild (Testzwecke)
        middleImageX = I.getWidth() / 2;
        middleImageY = I.getHeight() / 2;
        Graphics2D g2 = (Graphics2D) I.getGraphics();
        g2.setColor(Color.WHITE);
        g2.drawLine(middleImageX, 0, middleImageX, I.getHeight());
        g2.drawLine(0, middleImageY, I.getWidth(), middleImageY);
    }

    /**
     * drawX()
     * @param x int
     * @param y int
     */
    private void drawX(int x, int y){
        Graphics g = imgPicture.getGraphics();

        g.setColor(Color.WHITE);
        g.drawLine(x, y, x-5, y);
        g.drawLine(x, y, x+5, y);
        g.drawLine(x, y, x, y-5);
        g.drawLine(x, y, x, y+5);
    }

    /**
     * plotValRe()
     * TODO Testzwecke
     */
    private void plotValRe() {
        plotPoints();
        validate();
        repaint();//*/
    }

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

        contentPane.setBackground(UIManager.getColor("blue"));
        setContentPane(contentPane);
        contentPane.setLayout(null);

//imgPicture
        imgPicture = new imgPanel();
        imgPicture.addMouseMotionListener(new MouseAdapter() {
            /**
             * zeichnet Rechteck mit Mittelkreuz, solange linke Maustaste geklickt
             * und bewegt wird
             * @param e MouseEvent
             */
            @Override
            public void mouseDragged(MouseEvent e) {
                if(I != null && !rightClicked) {
                    endX = e.getX();
                    endY = e.getY();

                    // Mitte des Rechtecks
                    middleRectX = (startX + endX) / 2;
                    middleRectY = (startY + endY) / 2;

                    // Mitte des Bildes
                    middleImageX = I.getWidth() / 2;
                    middleImageY = I.getHeight() / 2;

                    Graphics2D g2 = (Graphics2D) imgPicture.getGraphics();
                    g2.setColor(Color.WHITE);

                    // Ueberpruefung, damit das Rechteck richtig gezeichnet wird
                    if (startX > endX) {
                        widthRect = startX - endX;
                        if (startY > endY) {
                            heightRect = startY - endY;
                            g2.drawRect(endX, endY, widthRect, heightRect);
                        } else {
                            heightRect = endY - startY;
                            g2.drawRect(endX, startY, widthRect, heightRect);
                        }
                    } else {
                        widthRect = endX - startX;
                        if (startY > endY) {
                            heightRect = startY - endY;
                            g2.drawRect(startX, endY, widthRect, heightRect);
                        } else {
                            heightRect = endY - startY;
                            g2.drawRect(startX, startY, widthRect, heightRect);
                        }
                    }

                    //Mittelpunkt des Rechtecks wird mit einem Kreuz dargestellt
                    drawX(middleRectX, middleRectY);
                    repaint();
                }
            }
        });
        imgPicture.addMouseListener(new MouseAdapter() {
            /**
             * Startwerte des Rechtecks werden festgelegt, wenn die linke
             * Maustaste gelickt wurde
             * @param e MouseEvent
             */
            @Override
            public void mousePressed(MouseEvent e) {
                if(I != null && e.getButton() == MouseEvent.BUTTON1) {
                    rightClicked = false;
                    rectangle = true;
                    startX = e.getX();
                    startY = e.getY();
                }else{
                    if(e.getButton() == MouseEvent.BUTTON3){
                        startX = startY = endX = endY = 0;
                        rightClicked = true;
                    }
                }
            }

            /**
             * Hereinzoomen in eine bestimmten Bereich wird ausgeführt,
             * wenn linke Maustaste losgelassen wird
             * @param e MouseEvent
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                if(I != null && e.getButton() == MouseEvent.BUTTON1 && !rightClicked) {
                    endX = e.getX();
                    endY = e.getY();
                    if(startX != endX || startY != endY) {
                        xMove += (middleRectX - middleImageX);
                        yMove += (middleRectY - middleImageY);
                        //TODO factor ueberpruefen
                        //double factor = 1.0-(widthRect*1.0)/I.getWidth();
                        double factor = I.getWidth()/(widthRect*1.0);
                        //double factor = 0.5;
                        zoomIn(factor);
                        //ServerThread.sendMessageText("rectangle/.../" + xMove + "/.../" + yMove + "/.../" + factor);
                        startX = 0;
                        startY = 0;
                        endX = 0;
                        endY = 0;
                        widthRect = 0;
                        heightRect = 0;
                        rectangle = false;
                    }
                }
            }

            /**
             * bricht mit rechtem Mausklick das Hereinzoomen in einen
             * bestimmten Bereich ab
             * @param e MouseEvent
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3){
                    startX = startY = endX = endY = 0;
                    rightClicked = true;
                }
            }
        });
        contentPane.add(imgPicture);

        plotValRe();

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
        //btnLeft.setEnabled(false);
        contentPane.add(btnLeft);

//btnUp
        btnUp = new JButton("Up");
        btnUp.addActionListener(this::actionPerformedButton);
        btnUp.setBounds((int) ((width / 2.0) - (int) (width / (8.5*2))), (int) (height / 1.1), (int) (width / (8.5*2)), (int) (height / 25.6));
        btnUp.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        //btnUp.setEnabled(false);
        contentPane.add(btnUp);

//btnDown
        btnDown = new JButton("Down");
        btnDown.addActionListener(this::actionPerformedButton);
        btnDown.setBounds((int) (width / 2.0), (int) (height / 1.1), (int) (width / (8.5*2)), (int) (height / 25.6));
        btnDown.setFont(new Font("Times New Roman", Font.BOLD, (int) ((height / 25.6) / 1.8)));
        //btnDown.setEnabled(false);
        contentPane.add(btnDown);

//btnRight
        btnRight = new JButton("Right");
        btnRight.addActionListener(this::actionPerformedButton);
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