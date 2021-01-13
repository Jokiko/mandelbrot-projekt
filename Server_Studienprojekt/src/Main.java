import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static boolean check = true;
    private static boolean checkRunning = false;
    private static ServerSocket ss2 = null;
    private static final int port = 5000;

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
