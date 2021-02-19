import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ServerThreadWebSocket extends JFrame implements Runnable{
    private static PrintWriter os = null;
    private static InputStream is = null;
    private static OutputStream out = null;
    private static Socket socket;


    private static boolean check;
    private static int anzClients;
    private static int runningClients;
    private static int yourNumber;

    private static int i = 0;

    /**
     * ServerThread()
     * @param s Socket
     */
    ServerThreadWebSocket(Socket s) {
        socket = s;
        check = true;
    }

    /**
     * sendMessageText()
     * in MethodsUI.java, KeyboardListener.java, ButtonListener.java benoetigt
     * @param text String
     */
    public static void sendMessageText(String text){
        System.out.println("text (ServerThreadWebSocket): " + text);
        try {
            if (text.equals("disconnect") || text.equals("close")) {
                    for (Socket socket : Server.listSocket) {
                        MethodsServerThread.sendMessage(new PrintWriter(socket.getOutputStream()), text);
                    }
            } else {
                for(Socket socket : Server.listRunning) {
                    MethodsServerThread.sendMessage(new PrintWriter(socket.getOutputStream()), text);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(text);
    }

    /**
     * receiveMessage()
     */
    private synchronized void receiveMessage(){
        try {
            is = socket.getInputStream();
            os = new PrintWriter(socket.getOutputStream());
            out = socket.getOutputStream();
        } catch (Exception e) {
            System.out.println("Error in serverThreadWebSocket");
        }
        String line = null;
        String[] stringBytes;
        int length;
        int buffLength = 1024;
        byte[] b = new byte[buffLength];
        try {
            while (check && !Thread.currentThread().isInterrupted()) {
                System.out.println(Thread.currentThread().getName());
                //Laenge der erhaltenen verschlüsselten Nachricht
                length = is.read(b); //blockt

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
                    System.out.println("b_ende: " + Thread.currentThread().getName() + "; " + Arrays.toString(b));
                }

                if (line != null) {
                    stringBytes = line.split("/.../");

                    System.out.println("in if : " + line);

                    if (stringBytes[0].equals("connect")) {
                        MethodsServerThread.sendWebSocketMessage(out, "connect response from the server");
                        anzClients = Server.listSocket.size();
                        yourNumber = Server.listSocket.size();
                        UI.lblAnzClients.setText("anzClients: " + anzClients);
                        System.out.println("anzClients: " + anzClients);
                        if(anzClients == 1) {
                            MethodsUI.I = new BufferedImage(UI.imgPicture.getWidth(), UI.imgPicture.getHeight(), BufferedImage.TYPE_INT_RGB);
                            UI.btnRestart.setEnabled(true);
                            UI.btnZoomIn.setEnabled(true);
                            UI.btnZoomOut.setEnabled(true);
                            UI.btnLeft.setEnabled(true);
                            UI.btnRight.setEnabled(true);
                            UI.btnUp.setEnabled(true);
                            UI.btnDown.setEnabled(true);
                        }
                        for(Socket socket : Server.listSocket) {
                            PrintWriter os = new PrintWriter(socket.getOutputStream());
                            MethodsServerThread.sendWebSocketMessage(out, "anzClients/.../" + anzClients);
                        }
                        MethodsServerThread.sendWebSocketMessage(out, "yourNumber/.../" + yourNumber);

                        /*if(listName.contains(stringBytes[1])){
                            i++;
                            listName.add(stringBytes[1] + "_" + i);
                        }else {
                            listName.add(stringBytes[1]);
                        }
                        for (String name : listName) {
                            System.out.println("Name: " + name);
                        }//*/
                    }

                    if (Server.listTypes.contains(stringBytes[0])){
                        int x = Integer.parseInt(stringBytes[1]);
                        int y = Integer.parseInt(stringBytes[2]);
                        int itr = Integer.parseInt(stringBytes[3]);
                        int colorItr = 20;
                        MethodsUI.I.setRGB(x, y, itr | (itr << colorItr));
                        validate();
                        repaint();
                    }

                    if (stringBytes[0].equals("start")){
                        runningClients++;
                        Server.listRunning.add(socket);
                        MethodsServerThread.sendWebSocketMessage(out, "size/.../" + UI.imgPicture.getWidth() + "/.../" + UI.imgPicture.getHeight());
                        System.out.println("runningClients: " + runningClients);
                    }

                    if (stringBytes[0].equals("pause")){
                        /*for(Socket socket : listRunning){
                            sendMessage(new PrintWriter(socket.getOutputStream()), "pauseChange");
                        }//*/
                        runningClients--;
                        Server.listRunning.remove(socket);
                        MethodsServerThread.sendWebSocketMessage(out, "pause");
                    }

                    if (stringBytes[0].equals("resume")){
                        runningClients++;
                        Server.listRunning.add(socket);
                        MethodsServerThread.sendWebSocketMessage(out, "resume");
                        /*for(Socket socket : Server.listRunning){
                            sendMessage(new PrintWriter(socket.getOutputStream()), "resumeChange");
                        }//*/
                    }

                    if (stringBytes[0].equals("close")) {
                        close(stringBytes[1]);
                    }

                    if (stringBytes[0].equals("check")){
                        Server.lock.lock();
                        Server.listUnchecked.remove(socket);
                        Server.lock.unlock();
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
        System.out.println("Server.listSocket.size(): " + Server.listSocket.size());
        int z = Server.listSocket.indexOf(so);
        if(z != -1) {
            System.out.println("Server.listSocket.remove: " + so);
            Server.listSocket.remove(so);
            /*System.out.println("Server.listIP.remove: " + so.getInetAddress().getHostAddress());
            Server.listIP.remove(so.getInetAddress().getHostAddress());//*/
            /*System.out.println("Server.listName.remove: " + Server.listName.get(z));
            Server.listName.remove(z);//*/
        }
        if(Server.listRunning.contains(so)){
            System.out.println("Server.listRunning.remove: " + so);
            Server.listRunning.remove(so);
        }else{
            System.out.println("listRunning.remove: no socket removed");
        }
        if(Server.listWebSocket.contains(so)){
            System.out.println("Server.listWebSocket.remove: " + so);
            Server.listWebSocket.remove(so);
        }else{
            System.out.println("Server.listWebSocket.remove: no socket removed");
        }

        Server.lock.lock();
        System.out.println("Server.listUnchecked.remove: " + so + "; Server.listUnchecked.size (vor remove): " + Server.listUnchecked.size());
        Server.listUnchecked.remove(so);
        anzClients = Server.listSocket.size();
        runningClients = Server.listRunning.size();
        yourNumber--;
        if(i > 0) {
            i--;
        }
        UI.lblAnzClients.setText("anzClients: " + anzClients);
        Server.lock.unlock();
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
            try {
                is.close();
                System.out.println("InputStream closed: " + is.toString());
                os.close();
                System.out.println("OutputStream closed: " + os.toString());
                socket.close();
                System.out.println("Socket closed: " + socket.toString());
                so.close();
                System.out.println("Socket closed: " + so.toString());
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        try {
            for (Socket socket : Server.listSocket) {
                PrintWriter os = new PrintWriter(socket.getOutputStream());
                MethodsServerThread.sendMessage(os, "close");
                //System.out.println("Response to Client (" + socket.getInetAddress() + "): " + line);
            }
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