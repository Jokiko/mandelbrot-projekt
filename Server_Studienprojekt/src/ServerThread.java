import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends JFrame implements Runnable{
    private static BufferedReader is = null;
    private static PrintWriter os = null;
    static Socket s;

    private static boolean check;
    public static int anzClients;
    public static int runningClients;
    private static int yourNumber;
    private static int anzPanels;

    public static ArrayList<Socket> listSocket = new ArrayList<>();
    public static ArrayList<Socket> listRunning = new ArrayList<>();
    public static ArrayList<Socket> listUnchecked = new ArrayList<>();
    public static ArrayList<String> listIP = new ArrayList<>();
    public static ArrayList<String> listName = new ArrayList<>();
    public static ArrayList<String> listTypes = new ArrayList<>();
    public static ArrayList<JPanel> listPanel = new ArrayList<>();

    int colorItr = 20;
    static int i = 0;

    /**
     * ServerThread()
     * @param s Socket
     * @param ip String
     */
    public ServerThread(Socket s, String ip) {
        ServerThread.s = new Socket();
        ServerThread.s = s;
        check = true;
        listSocket.add(s);
        listIP.add(ip);
        listTypes.add("plot");
        listTypes.add("click");
        listTypes.add("zoomIn");
        listTypes.add("zoomOut");
        listTypes.add("Up");
        listTypes.add("Down");
        listTypes.add("Left");
        listTypes.add("Right");
        listTypes.add("restart");
        listTypes.add("restartResume");
        System.out.println("s.toString (ServerThread): " + s.toString());
    }

    /**
     * sendMessageText()
     * @param text String
     */
    public static void sendMessageText(String text){
        if(text.equals("disconnect") || text.equals("close")){
            try {
                for (Socket socket : listSocket) {
                    PrintWriter os = new PrintWriter(socket.getOutputStream());
                    sendMessage(os, "disconnect");
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }else {
            sendMessage(os, text);
        }
        System.out.println(text); //restart, zoomIn, zoomOut, Up, Down, Right, Left
    }

    /**
     * sendMessage
     */
    public synchronized static void sendMessage(PrintWriter os, String message){
        try {
            os.println(message);
            os.flush();
            System.out.println("send \"" + message + "\" to Client");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * receiveMessage()
     */
    private synchronized void receiveMessage(){
        try {
            is = new BufferedReader((new InputStreamReader(s.getInputStream())));
            os = new PrintWriter(s.getOutputStream());
        } catch (Exception e) {
            System.out.println("Error in server thread");
        }
        String line;
        String[] bytes;
        try {
            while (check && !Thread.currentThread().isInterrupted()) {
                line = is.readLine();
                if (line != null) {
                    bytes = line.split("/.../");

                    System.out.println("in if : " + line);

                    if (bytes[0].equals("connect")) {
                        sendMessage(os, "connect response from the server");
                        anzClients++;
                        yourNumber++;
                        UI.lblAnzClients.setText("anzClients: " + anzClients);
                        System.out.println("anzClients: " + anzClients);
                        if(anzClients == 1) {
                            UI.I = new BufferedImage(UI.imgPicture.getWidth(), UI.imgPicture.getHeight(), BufferedImage.TYPE_INT_RGB);
                            UI.btnRestart.setEnabled(true);
                            UI.btnZoomIn.setEnabled(true);
                            UI.btnZoomOut.setEnabled(true);
                            UI.btnLeft.setEnabled(true);
                            UI.btnRight.setEnabled(true);
                            UI.btnUp.setEnabled(true);
                            UI.btnDown.setEnabled(true);
                            //UI.btnEnd.setEnabled(true);
                        }//*/
                        for(Socket socket : listSocket) {
                            PrintWriter os = new PrintWriter(socket.getOutputStream());
                            sendMessage(os, "anzClients/.../" + anzClients);
                        }
                        sendMessage(os, "yourNumber/.../" + yourNumber);

                        if(listName.contains(bytes[1])){
                            i++;
                            listName.add(bytes[1] + "_" + i);
                        }else {
                            listName.add(bytes[1]);
                        }
                        /*for(String name : listName){
                            if(name.equals(bytes[1])){
                                String[] nameParts = name.split("/../");
                                JPanel panel = new JPanel();
                                anzPanels++;
                                int height = 50*anzPanels;
                                System.out.println(height);
                                //panel.setSize(UI.width-(UI.width-210), height);
                                //panel.setBounds(UI.width-230, height, UI.width-(UI.width-220), 50);
                                if(nameParts.length == 2) {
                                    System.out.println(nameParts[0] + ", " + nameParts[1]);
                                    //panel.add(new JLabel("<html><body>" + nameParts[0] + "<br>" + nameParts[1] + "</body></html>"));
                                    //UI.panel.add(new JLabel("<html><body>" + nameParts[0] + "<br>" + nameParts[1] + "</body></html>"));
                                }else{
                                    System.out.println(nameParts[0]);
                                    //panel.add(new JLabel(nameParts[0]));
                                    //UI.panel.add(new JLabel(nameParts[0]));
                                }
                                //UI.contentPane.add(panel);
                                //UI.scrollPane.add(panel);
                                //listPanel.add(panel);
                            }
                        }//*/
                    }

                    /*if (bytes[0].equals("name")){
                        listName.add(bytes[1]);
                        for(String name : listName){
                            if(name.equals(bytes[1])){
                                String[] nameParts = name.split("/../");
                                JPanel panel = new JPanel();
                                anzPanels++;
                                int height = 50*anzPanels;
                                System.out.println(height);
                                panel.setSize(UI.width-(UI.width-220),  height);
                                if(nameParts.length == 2) {
                                    System.out.println(nameParts[0] + ", " + nameParts[1]);
                                    panel.add(new JLabel("<html><body>" + nameParts[0] + "<br>" + nameParts[1] + "</body></html>"));
                                    //UI.panel.add(new JLabel("<html><body>" + nameParts[0] + "<br>" + nameParts[1] + "</body></html>"));
                                }else{
                                    System.out.println(nameParts[0]);
                                    panel.add(new JLabel(nameParts[0]));
                                    //UI.panel.add(new JLabel(nameParts[0]));
                                }
                                UI.scrollPane.add(panel);
                            }
                        }
                    }//*/

                    //if (bytes[0].equals("plot")){
                    if (listTypes.contains(bytes[0])){
                        //sendMessage(os, "plot");
                        int x = Integer.parseInt(bytes[1]);
                        int y = Integer.parseInt(bytes[2]);
                        int itr = Integer.parseInt(bytes[3]);
                        UI.I.setRGB(x, y, itr | (itr << colorItr));
                        validate();
                        repaint();
                    }

                    if (bytes[0].equals("start")){
                        runningClients++;
                        listRunning.add(s);
                        sendMessage(os, "size/.../" + UI.imgPicture.getWidth() + "/.../" + UI.imgPicture.getHeight());
                        /*sendMessage(os, "width/.../" + UI.imgPicture.getWidth());
                        sendMessage(os, "height/.../" + UI.imgPicture.getHeight());//*/
                        System.out.println("runningClients: " + runningClients);
                    }

                    if (bytes[0].equals("pause")){
                        runningClients--;
                        listRunning.remove(s);
                        sendMessage(os, "pause");
                    }

                    if (bytes[0].equals("resume")){
                        runningClients++;
                        listRunning.add(s);
                        sendMessage(os, "resume");
                    }

                    if (bytes[0].equals("close")) {
                        close(bytes[1]);
                    }

                    if (bytes[0].equals("check")){
                        Main.lock.lock();
                        listUnchecked.remove(s);
                        Main.lock.unlock();
                    }
                }else{
                    close("" + null);
                    check = false;
                }
            }
        } catch (IOException ioe) {
            check = false;
            line = Thread.currentThread().getName(); //reused String line for getting thread name
            System.out.println("IOException/ Client " + line + " terminated abruptly");
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            check = false;
            line = Thread.currentThread().getName(); //reused String line for getting thread name
            System.out.println("NullPointerException/ Client " + line + " Closed");
            npe.printStackTrace();
        }
        /* finally {
                try {
                    //closeMessage(line);
                    System.out.println(Thread.currentThread().getName() + ": Connection Closing...");
                    anzClients--;
                    runningClients--;
                    UI.lblAnzClients.setText("anzClients: " + anzClients);
                    if(anzClients == 0) {
                        //UI.I = null;
                        UI.btnRestart.setEnabled(false);
                        UI.btnZoomIn.setEnabled(false);
                        UI.btnZoomOut.setEnabled(false);
                        UI.btnLeft.setEnabled(false);
                        UI.btnRight.setEnabled(false);
                        UI.btnUp.setEnabled(false);
                        UI.btnDown.setEnabled(false);
                        //UI.btnEnd.setEnabled(false);
                    }///
                    if (is != null) {
                        is.close();
                        System.out.println("Socket Input Stream Closed");
                    }
                    if (os != null) {
                        os.close();
                        System.out.println("Socket Out Closed");
                    }
                    if (s != null) {
                        s.close();
                        System.out.println("Socket Closed");
                    }
                } catch (IOException ioe) {
                    System.out.println("Socket Close Error");
                    ioe.printStackTrace();
                }
            }//end finally
        //}//*/
    }//*/

    /**
     * close()
     * @param obj Object
     */
    public static void close(Object obj){
        check = false;
        Socket so;
        //String line = "";
        if(obj instanceof Socket){
            so = (Socket) obj;
        }else{
            so = s;
        }
        /*if(obj instanceof String){
            //line = (String) obj;
            so = s;
        }//*/
        System.out.println(Thread.currentThread().getName() + ": Connection Closing...");
        //int z = -1;
        System.out.println("listSocket.size(): " + listSocket.size());
        //for(int k = 0; k < listSocket.size(); k++){
            //if(listSocket.get(k) == so){
                //z = k;
                int z = listSocket.indexOf(so);
                if(z != -1) {
                    System.out.println("listSocket.remove: " + listSocket.get(z));
                    listSocket.remove(z);
                    System.out.println("listIP.remove: " + so.getInetAddress().getHostAddress());
                    listIP.remove(so.getInetAddress().getHostAddress());
                    System.out.println("listName.remove: " + listName.get(z));
                    listName.remove(z);
                }
                if(listRunning.contains(so)){
                    System.out.println("listRunning.remove: " + so);
                    listRunning.remove(so);
                }else{
                    System.out.println("listRunning.remove: no socket removed");
                }
                //break;
            //}
        //}
        /*listIP.remove(s.getInetAddress().getHostAddress());
        System.out.println("listIP.remove: " + listSocket.get(z).getInetAddress().getHostAddress());//s.getInetAddress().getHostAddress());
        listIP.remove(listSocket.get(z).getInetAddress().getHostAddress());
        System.out.println("listIP.remove: " + so.getInetAddress().getHostAddress());
        listIP.remove(so.getInetAddress().getHostAddress());
        System.out.println("listSocket.remove: " + listSocket.get(z).toString());
        listSocket.remove(z);
        System.out.println("listSocket.remove: " + s.toString());
        listName.remove(line);
        System.out.println("listName.remove: " + listName.get(z));//line);
        listName.remove(z);//*/
        Main.lock.lock();
        //synchronized (Main.lock) {
            System.out.println("listUnchecked.remove: " + so + "; listUnchecked.size (vor remove): " + listUnchecked.size());
            listUnchecked.remove(so);
        //}
        anzClients = listSocket.size();
        runningClients = listRunning.size();
        yourNumber--;
        Main.lock.unlock();
        if(i > 0) {
            i--;
        }
        UI.lblAnzClients.setText("anzClients: " + anzClients);
        if(anzClients == 0) {
            //UI.I = null;
            UI.btnRestart.setEnabled(false);
            UI.btnZoomIn.setEnabled(false);
            UI.btnZoomOut.setEnabled(false);
            UI.btnLeft.setEnabled(false);
            UI.btnRight.setEnabled(false);
            UI.btnUp.setEnabled(false);
            UI.btnDown.setEnabled(false);
            //UI.btnEnd.setEnabled(false);
            i = 0;
        }///
        //anzPanels--;
        //closeMessage(line);
        try {
            for (Socket socket : listSocket) {
                PrintWriter os = new PrintWriter(socket.getOutputStream());
                sendMessage(os, "close");
                //System.out.println("Response to Client (" + socket.getInetAddress() + "): " + line);
            }
            is.close();
            System.out.println("InputStream closed: " + is.toString());
            os.close();
            System.out.println("OutputStream closed: " + os.toString());//*/
            s.close();
            System.out.println("Socket closed: " + s.toString());
            so.close();
            System.out.println("Socket closed: " + so.toString());
        }catch(IOException e){
            e.printStackTrace();
        }
        Thread.currentThread().interrupt();
    }
    /*public static void close(Socket so){
        System.out.println(Thread.currentThread().getName() + ": Connection Closing...");
        anzClients--;
        runningClients--;
        if(i > 0) {
            i--;
        }
        UI.lblAnzClients.setText("anzClients: " + anzClients);
        if(anzClients == 0) {
            //UI.I = null;
            UI.btnRestart.setEnabled(false);
            UI.btnZoomIn.setEnabled(false);
            UI.btnZoomOut.setEnabled(false);
            UI.btnLeft.setEnabled(false);
            UI.btnRight.setEnabled(false);
            UI.btnUp.setEnabled(false);
            UI.btnDown.setEnabled(false);
            //UI.btnEnd.setEnabled(false);
            i = 0;
        }///
        if(listRunning.size() > 0) {
            listRunning.remove(so);
            System.out.println("listRunning.remove: " + s.toString());
        }else{
            System.out.println("nothing removed from listRunning");
        }
        listSocket.remove(so);
        System.out.println("listSocket.remove: " + s.toString());
        listIP.remove(so.getInetAddress().getHostAddress());
        System.out.println("listIP.remove: " + s.getInetAddress().getHostAddress());
        /*listName.remove(line);
        System.out.println("listName.remove: " + line);///
        //anzPanels--;
        check = false;
        //closeMessage(line);
        try {
            for (Socket socket : listSocket) {
                PrintWriter os = new PrintWriter(socket.getOutputStream());
                sendMessage(os, "close");
                //System.out.println("Response to Client (" + socket.getInetAddress() + "): " + line);
            }
            so.close();
            System.out.println("Socket closed: " + so.toString());
        }catch(IOException e){
            e.printStackTrace();
        }
        //Thread.currentThread().interrupt();
    }//*/

    /**
     * run()
     */
    public void run() {
        //while(check && !Thread.currentThread().isInterrupted()) {
        receiveMessage();
        System.out.println("ServerThread beendet");
        //}
        /*try {
            is = new BufferedReader((new InputStreamReader(s.getInputStream())));
            os = new PrintWriter(s.getOutputStream());
        } catch (Exception e) {
            System.out.println("Error in server thread");
        }
        String line;
        String[] bytes;
        try {
            while (check && !Thread.currentThread().isInterrupted()) {
                line = is.readLine();
                if (line != null) {
                    bytes = line.split("/.../");

                    System.out.println("in if : " + line);

                    if (bytes[0].equals("connect")) {
                        sendMessage(os, "connect response from the server");
                        anzClients++;
                        yourNumber++;
                        UI.lblAnzClients.setText("anzClients: " + anzClients);
                        System.out.println("anzClients: " + anzClients);
                        if(anzClients == 1) {
                            UI.I = new BufferedImage(UI.imgPicture.getWidth(), UI.imgPicture.getHeight(), BufferedImage.TYPE_INT_RGB);
                            UI.btnRestart.setEnabled(true);
                            UI.btnZoomIn.setEnabled(true);
                            UI.btnZoomOut.setEnabled(true);
                            UI.btnLeft.setEnabled(true);
                            UI.btnRight.setEnabled(true);
                            UI.btnUp.setEnabled(true);
                            UI.btnDown.setEnabled(true);
                            //UI.btnEnd.setEnabled(true);
                        }///
                        for(Socket socket : listSocket) {
                            PrintWriter os = new PrintWriter(socket.getOutputStream());
                            sendMessage(os, "anzClients/.../" + anzClients);
                        }
                        sendMessage(os, "yourNumber/.../" + yourNumber);

                        if(listName.contains(bytes[1])){
                            i++;
                            listName.add(bytes[1] + "_" + i);
                        }else {
                            listName.add(bytes[1]);
                        }
                            /*for(String name : listName){
                                if(name.equals(bytes[1])){
                                    String[] nameParts = name.split("/../");
                                    JPanel panel = new JPanel();
                                    anzPanels++;
                                    int height = 50*anzPanels;
                                    System.out.println(height);
                                    //panel.setSize(UI.width-(UI.width-210), height);
                                    //panel.setBounds(UI.width-230, height, UI.width-(UI.width-220), 50);
                                    if(nameParts.length == 2) {
                                        System.out.println(nameParts[0] + ", " + nameParts[1]);
                                        //panel.add(new JLabel("<html><body>" + nameParts[0] + "<br>" + nameParts[1] + "</body></html>"));
                                        //UI.panel.add(new JLabel("<html><body>" + nameParts[0] + "<br>" + nameParts[1] + "</body></html>"));
                                    }else{
                                        System.out.println(nameParts[0]);
                                        //panel.add(new JLabel(nameParts[0]));
                                        //UI.panel.add(new JLabel(nameParts[0]));
                                    }
                                    //UI.contentPane.add(panel);
                                    //UI.scrollPane.add(panel);
                                    //listPanel.add(panel);
                                }
                            }///
                    }

                        if (bytes[0].equals("name")){
                            listName.add(bytes[1]);
                            for(String name : listName){
                                if(name.equals(bytes[1])){
                                    String[] nameParts = name.split("/../");
                                    JPanel panel = new JPanel();
                                    anzPanels++;
                                    int height = 50*anzPanels;
                                    System.out.println(height);
                                    panel.setSize(UI.width-(UI.width-220),  height);
                                    if(nameParts.length == 2) {
                                        System.out.println(nameParts[0] + ", " + nameParts[1]);
                                        panel.add(new JLabel("<html><body>" + nameParts[0] + "<br>" + nameParts[1] + "</body></html>"));
                                        //UI.panel.add(new JLabel("<html><body>" + nameParts[0] + "<br>" + nameParts[1] + "</body></html>"));
                                    }else{
                                        System.out.println(nameParts[0]);
                                        panel.add(new JLabel(nameParts[0]));
                                        //UI.panel.add(new JLabel(nameParts[0]));
                                    }
                                    UI.scrollPane.add(panel);
                                }
                            }
                        }///

                    if (bytes[0].equals("plot")){
                        //sendMessage(os, "plot");
                        int x = Integer.parseInt(bytes[1]);
                        int y = Integer.parseInt(bytes[2]);
                        int itr = Integer.parseInt(bytes[3]);
                        UI.I.setRGB(x, y, itr | (itr << colorItr));
                        validate();
                        repaint();
                    }

                    if (bytes[0].equals("start")){
                        runningClients++;
                        listRunning.add(s);
                        sendMessage(os, "size/.../" + UI.imgPicture.getWidth() + "/.../" + UI.imgPicture.getHeight());
                            sendMessage(os, "width/.../" + UI.imgPicture.getWidth());
                            sendMessage(os, "height/.../" + UI.imgPicture.getHeight());///
                        System.out.println("runningClients: " + runningClients);
                    }

                    if (bytes[0].equals("pause")){
                        runningClients--;
                        listRunning.remove(s);
                        sendMessage(os, "pause");
                    }

                    if (bytes[0].equals("resume")){
                        runningClients++;
                        listRunning.add(s);
                        sendMessage(os, "resume");
                    }

                    if (bytes[0].equals("close")) {
                        close(bytes[1]);
                    }

                    if (bytes[0].equals("check")){
                        listUnchecked.remove(s);
                    }
                }else{
                    close("" + null);
                    check = false;
                }
            }
            System.out.println("ServerThread beendet");
        } catch (IOException ioe) {
            check = false;
            line = Thread.currentThread().getName(); //reused String line for getting thread name
            System.out.println("IOException/ Client " + line + " terminated abruptly");
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            check = false;
            line = Thread.currentThread().getName(); //reused String line for getting thread name
            System.out.println("NullPointerException/ Client " + line + " Closed");
            npe.printStackTrace();
        }//*/
    }
}