import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ServerThread extends JFrame implements Runnable{
    private static BufferedReader is = null;
    private static PrintWriter os = null;
    private static InputStream iss = null;
    static Socket socket;

    private static boolean check;
    public static int anzClients;
    public static int runningClients;
    private static int yourNumber;

    /**
     * ArrayLists
     */
    public static ArrayList<Socket> listSocket = new ArrayList<>();
    public static ArrayList<Socket> listRunning = new ArrayList<>();
    public static ArrayList<Socket> listUnchecked = new ArrayList<>();
    public static ArrayList<String> listIP = new ArrayList<>();
    public static ArrayList<String> listTypes = new ArrayList<>();
    public static ArrayList<String> listName = new ArrayList<>();

    int colorItr = 20;
    static int i = 0;

    /**
     * ServerThread()
     * @param s Socket
     * @param ip String
     */
    ServerThread(Socket s, String ip) {
        socket = s;
        check = true;
        listSocket.add(s);
        listIP.add(ip);
        listTypes.add("plot");
        listTypes.add("click");
        listTypes.add("rectangle");
        listTypes.add("zoomIn");
        listTypes.add("zoomOut");
        listTypes.add("Up");
        listTypes.add("Down");
        listTypes.add("Left");
        listTypes.add("Right");
        listTypes.add("restart");
        listTypes.add("restartResume");
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
        System.out.println(text);
    }

    /**
     * sendMessage()
     * @param os PrintWriter
     * @param message String
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
            is = new BufferedReader((new InputStreamReader(socket.getInputStream())));
            iss = socket.getInputStream();
            os = new PrintWriter(socket.getOutputStream());
        } catch (Exception e) {
            System.out.println("Error in serverThread");
        }
        String line = null;
        String[] bytes;
        int length;
        int buffLength = 1024;
        byte[] b = new byte[buffLength];
        try {
            while (check && !Thread.currentThread().isInterrupted()) {
                if(Main.clientType.equals("WebSocket")) {
                    //Laenge der erhaltenen verschlüsselten Nachricht
                    length = iss.read(b);

                    //Dekodierung der WebSocket Nachricht
                    if (length != -1) {
                        byte rLength;
                        int rMaskIndex = 2;
                        int rDataStart;
                        //Hier fehlt eventuell noch ein Check, ob b[0] etwas anderes als Text ist.
                        byte data = b[1];
                        byte op = (byte) 127;
                        //Check, wo die Laenge geschrieben ist, wenn <=125 dann ist das die Laenge
                        rLength = (byte) (data & op);
                        //wenn 126, dann steht in den naechsten 16 bit die Laenge
                        if (rLength == (byte) 126) rMaskIndex = 4;
                        //wenn 127, dann steht in den naechsten 64 bit die Laenge
                        if (rLength == (byte) 127) rMaskIndex = 10;

                        //auf die Laenge folgt ein 4 bit langer mask key, der zum dekodieren benoetigt wird
                        byte[] masks = new byte[4];

                        int j = 0;
                        int i;
                        for (i = rMaskIndex; i < (rMaskIndex + 4); i++) {
                            masks[j] = b[i];
                            j++;
                        }

                        //auf den masking key folgen die verschluesselten Daten
                        rDataStart = rMaskIndex + 4;

                        int messLen = length - rDataStart;

                        byte[] message = new byte[messLen];

                        //Entschluesslung der Daten
                        for (i = rDataStart, j = 0; i < length; i++, j++) {
                            message[j] = (byte) (b[i] ^ masks[j % 4]);
                        }

                        line = new String(message, StandardCharsets.UTF_8);
                        b = new byte[buffLength];
                    }
                }else{
                    line = is.readLine();
                }//*/

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
                        }
                        for(Socket socket : listSocket) {
                            PrintWriter os = new PrintWriter(socket.getOutputStream());
                            sendMessage(os, "anzClients/.../" + anzClients);
                        }
                        sendMessage(os, "yourNumber/.../" + yourNumber);

                        /*if(listName.contains(bytes[1])){
                            i++;
                            listName.add(bytes[1] + "_" + i);
                        }else {
                            listName.add(bytes[1]);
                        }
                        for (String name : listName) {
                            System.out.println("Name: " + name);
                        }//*/
                    }

                    if (listTypes.contains(bytes[0])){
                        int x = Integer.parseInt(bytes[1]);
                        int y = Integer.parseInt(bytes[2]);
                        int itr = Integer.parseInt(bytes[3]);
                        UI.I.setRGB(x, y, itr | (itr << colorItr));
                        validate();
                        repaint();
                    }

                    if (bytes[0].equals("start")){
                        runningClients++;
                        listRunning.add(socket);
                        sendMessage(os, "size/.../" + UI.imgPicture.getWidth() + "/.../" + UI.imgPicture.getHeight());
                        System.out.println("runningClients: " + runningClients);
                    }

                    if (bytes[0].equals("pause")){
                        /*for(Socket socket : listRunning){
                            sendMessage(new PrintWriter(socket.getOutputStream()), "pauseChange");
                        }//*/
                        runningClients--;
                        listRunning.remove(socket);
                        sendMessage(os, "pause");
                    }

                    if (bytes[0].equals("resume")){
                        runningClients++;
                        listRunning.add(socket);
                        sendMessage(os, "resume");
                        /*for(Socket socket : listRunning){
                            sendMessage(new PrintWriter(socket.getOutputStream()), "resumeChange");
                        }//*/
                    }

                    if (bytes[0].equals("close")) {
                        close(bytes[1]);
                    }

                    if (bytes[0].equals("check")){
                        Main.lock.lock();
                        listUnchecked.remove(socket);
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
    }

    /**
     * close()
     * @param obj Object
     */
    public static void close(Object obj){
        check = false;
        Socket so;
        if(obj instanceof Socket){
            so = (Socket) obj;
        }else{
            so = socket;
        }
        System.out.println(Thread.currentThread().getName() + ": Connection Closing...");
        System.out.println("listSocket.size(): " + listSocket.size());
        int z = listSocket.indexOf(so);
        if(z != -1) {
            System.out.println("listSocket.remove: " + so);
            listSocket.remove(so);
            System.out.println("listIP.remove: " + so.getInetAddress().getHostAddress());
            listIP.remove(so.getInetAddress().getHostAddress());
            /*System.out.println("listName.remove: " + listName.get(z));
            listName.remove(z);//*/
        }
        if(listRunning.contains(so)){
            System.out.println("listRunning.remove: " + so);
            listRunning.remove(so);
        }else{
            System.out.println("listRunning.remove: no socket removed");
        }
        Main.lock.lock();
        System.out.println("listUnchecked.remove: " + so + "; listUnchecked.size (vor remove): " + listUnchecked.size());
        listUnchecked.remove(so);
        Main.lock.unlock();
        anzClients = listSocket.size();
        runningClients = listRunning.size();
        yourNumber--;
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
        }
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
            socket.close();
            System.out.println("Socket closed: " + socket.toString());
            so.close();
            System.out.println("Socket closed: " + so.toString());
        }catch(IOException e){
            e.printStackTrace();
        }
        Thread.currentThread().interrupt();
    }

    /**
     * run()
     */
    public void run() {
        receiveMessage();
        System.out.println("ServerThread beendet");
    }
}