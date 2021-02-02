import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static boolean check = true;
    private static ServerSocket ss2 = null;
    private static final int port = 5000;

    static final ReentrantLock lock = new ReentrantLock();

    static String clientType;

    /**
     * sendCheck()
     */
    private static void sendCheck(){
        if (ServerThread.listSocket.size() > 0) {
            System.out.println("listSocket.size(): " + ServerThread.listSocket.size());
            try {
                lock.lock();
                ServerThread.listUnchecked = new ArrayList<>();
                ServerThread.listUnchecked.addAll(ServerThread.listSocket);
                System.out.println("Unchecked.size(): " + ServerThread.listUnchecked.size());
                lock.unlock();
                Thread.sleep(1000);
                lock.lock();
                for (Socket socket : ServerThread.listUnchecked) {
                    ServerThread.sendMessage(new PrintWriter(socket.getOutputStream()), "check/.../Main");
                    System.out.println("check/.../Main");
                }
                lock.unlock();
                Thread.sleep(4000);
                lock.lock();
                System.out.println("Unchecked.size(), after sleep(): " + ServerThread.listUnchecked.size());
                for (Socket socket : ServerThread.listUnchecked) {
                    System.out.println("Unchecked remove: " + socket.toString());
                    ServerThread.close(socket);
                }
                lock.unlock();
                sendCheck();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * getClientType()
     * @param socket Socket
     */
    private static void getClientType(Socket socket){
        String line;
        String[] bytes;
        try {
            line = new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
            System.out.println(line);
            bytes = line.split("/.../");
            if(bytes[0].equals("type")){
                clientType = bytes[1];
                System.out.println(clientType);
                ServerThread.sendMessage(new PrintWriter(socket.getOutputStream()), "type");
            }else{
                System.out.println("Wrong message type");
                ServerThread.sendMessage(new PrintWriter(socket.getOutputStream()), "disconnect/.../wrong_message_type");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * startServer()
     * rekursive Methode, damit bei einem Fehlversuch der Server wieder gestartet werden kann, wenn der User es möchte
     */
    private static void startServer(){
        try{
            check = true;
            ss2 = new ServerSocket();
            ss2.bind(new InetSocketAddress(InetAddress.getLocalHost(), port));
            System.out.println(ss2.getInetAddress().getHostAddress());
            serverRunning();
        }catch(IOException ioe){
            check = false;
            System.out.println("Server error");
            ioe.printStackTrace();
            int input = JOptionPane.showOptionDialog(null, "Server could not be started", "ERROR",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.ERROR_MESSAGE, null,
                    new String[]{"Restart", "Cancel"}, null);
            if(input == 0){ //Restart click
                System.out.println("\nServer restarted");
                startServer();
            }else{
                System.out.println("\nCancel");
            }
        }
    }

    /**
     * serverRunning()
     */
    private static void serverRunning(){
        System.out.println("Server started (Port: " + port + ")");

        //start UI
        new UI().setVisible(true);

        while(check){
            try{
                Socket s = ss2.accept();

                getClientType(s);

                if(clientType != null) {
                    if (clientType.equals("WebSocket")) {
                        InputStream in = s.getInputStream();
                        OutputStream out = s.getOutputStream();
                        Scanner scan = new Scanner(in, StandardCharsets.UTF_8);
                        //WebSocket Handshake
                        try {
                            String data = scan.useDelimiter("\\r\\n\\r\\n").next();
                            Matcher get = Pattern.compile("^GET").matcher(data);
                            if (get.find()) {
                                Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                                match.find();
                                byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                                        + "Connection: Upgrade\r\n"
                                        + "Upgrade: websocket\r\n"
                                        + "Sec-WebSocket-Accept: "
                                        + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)))
                                        + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
                                out.write(response, 0, response.length);
                            }
                        } catch (NoSuchAlgorithmException nsae) {
                            nsae.printStackTrace();
                        }
                    }

                    String ip = s.getInetAddress().getHostAddress();
                    Thread serverThread = new Thread(new ServerThread(s, ip));
                    serverThread.start();

                    /*/sendCheck
                    if(ServerThread.listSocket.size() == 1){
                        new Thread(Main::sendCheck).start();
                    }//*/
                }else{
                    System.out.println("clientType == null");
                }
            }catch(IOException ioe) {
                check = false;
                ioe.printStackTrace();
            }
        }
    }

    /**
     * main()
     * @param args String[]
     */
    public static void main(String[] args){
        startServer();
    }
}