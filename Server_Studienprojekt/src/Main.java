import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
    private static boolean checkRunning = false;
    private static ServerSocket ss2 = null;
    private static final int port = 80;

    static final ReentrantLock lock = new ReentrantLock();

    /**
     * startServer()
     * rekursive Methode, damit bei einem Fehlversuch der Server wieder gestartet werden kann, wenn der User es möchte
     */
    private static void startServer(){
        try{
            check = true;
            ss2 = new ServerSocket(port);
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
     * sendCheck()
     */
    private static void sendCheck(){
        //synchronized(lock) {
            if (ServerThread.listSocket.size() > 0) {
                System.out.println("listSocket.size(): " + ServerThread.listSocket.size());
                try {
                    //synchronized(Thread.currentThread()) {
                    ServerThread.listUnchecked = new ArrayList<>();
                    ServerThread.listUnchecked.addAll(ServerThread.listSocket);
                    System.out.println("Unchecked.size(): " + ServerThread.listUnchecked.size());
                    Thread.sleep(1000);
                    for (Socket socket : ServerThread.listUnchecked) {
                        PrintWriter os = new PrintWriter(socket.getOutputStream());
                        ServerThread.sendMessage(os, "check/.../Main");
                        System.out.println("check/.../Main");
                    }
                    Thread.sleep(4000);
                    System.out.println("Unchecked.size(), after sleep(): " + ServerThread.listUnchecked.size());
                    lock.lock();
                    ///ArrayList<Socket> listUnchecked2 = new ArrayList<>(ServerThread.listUnchecked);
                    //lock.unlock();
                    //synchronized (lock) {
                        //for (Socket socket : listUnchecked2) {
                        for (Socket socket : ServerThread.listUnchecked) {
                            System.out.println("Unchecked remove: " + socket.toString());
                            ServerThread.close(socket);
                        }//*/
                    //}
                    lock.unlock();
                    //}
                    sendCheck();
                } catch (Exception e) {
                    checkRunning = false;
                    e.printStackTrace();
                }
            } else {
                checkRunning = false;
            }
        //}
    }

    /**
     * main()
     * @param args String[]
     */
    public static void main(String[] args){

        //start Server
        System.out.println("Server started (Port: " + port + ")");
        startServer();

        //start UI
        new UI().setVisible(true);

        while(check){
            try{
                Socket s = ss2.accept();
                System.out.println("s.toString (Main): " + s.toString());
                String ip = s.getInetAddress().getHostAddress();
                /*System.out.println(ip);
                ServerThread.listIP.add(ip);
                ServerThread.listSocket.add(s);//*/

                InputStream in = s.getInputStream();
                OutputStream out = s.getOutputStream();
                Scanner scan = new Scanner(in, "UTF-8");
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
                                + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
                                + "\r\n\r\n").getBytes("UTF-8");
                        out.write(response, 0, response.length);
                    }
                }
                catch(NoSuchAlgorithmException nsae){
                    nsae.printStackTrace();
                }
                Thread serverThread = new Thread (new ServerThread(s, ip));
                serverThread.start();

                /*if(ServerThread.listSocket.size() == 1 && !checkRunning){
                    checkRunning = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            checkRunning = true;
                            sendCheck();//*/
                            /*while(ServerThread.listSocket.size() > 0) {
                                System.out.println("listSocket.size(): " + ServerThread.listSocket.size());
                                try {
                                    ServerThread.listUnchecked = new ArrayList<>();
                                    ServerThread.listUnchecked.addAll(ServerThread.listSocket);
                                    System.out.println("Unchecked.size(): " + ServerThread.listUnchecked.size());
                                    Thread.sleep(1000);
                                    for (Socket socket : ServerThread.listUnchecked) {
                                        PrintWriter os = new PrintWriter(socket.getOutputStream());
                                        ServerThread.sendMessage(os, "check/.../Main");
                                        System.out.println("check/.../Main");
                                    }
                                    Thread.sleep(4000);
                                    System.out.println("Unchecked.size(), after sleep(): " + ServerThread.listUnchecked.size());
                                    for (Socket socket : ServerThread.listUnchecked){
                                        System.out.println("Unchecked remove: " + socket.toString());
                                        ServerThread.close(socket);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }//*/
                        /*}
                    }).start();
                }//*/
            }catch(IOException ioe) {
                check = false;
                ioe.printStackTrace();
            }
        }
    }
}
