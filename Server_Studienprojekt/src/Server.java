import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    private static boolean check = true;
    private static ServerSocket serverSocket = null;
    private static final int port = 5000;

    static final ReentrantLock lock = new ReentrantLock();

    private static String clientType;

    private static String data;
    private static OutputStream out;

    /**
     * ArrayLists
     */
    public static ArrayList<Socket> listSocket = new ArrayList<>();
    public static ArrayList<Socket> listRunning = new ArrayList<>();
    public static ArrayList<Socket> listUnchecked = new ArrayList<>();
    public static ArrayList<Socket> listWebSocket = new ArrayList<>();
    public static ArrayList<String> listTypes = new ArrayList<>();
    public static ArrayList<String> listIP = new ArrayList<>();
    public static ArrayList<String> listName = new ArrayList<>();

    /**
     * sendCheck()
     */
    private static void sendCheck(){
        if (listSocket.size() > 0) {
            System.out.println("listSocket.size(): " + listSocket.size());
            try {
                lock.lock();
                listUnchecked = new ArrayList<>();
                listUnchecked.addAll(listSocket);
                System.out.println("Unchecked.size(): " + listUnchecked.size());
                lock.unlock();
                Thread.sleep(1000);
                lock.lock();
                for (Socket socket : listUnchecked) {
                    MethodsServerThread.sendMessage(new PrintWriter(socket.getOutputStream()), "check/.../Main");
                    System.out.println("check/.../Main");
                }
                lock.unlock();
                Thread.sleep(4000);
                lock.lock();
                System.out.println("Unchecked.size(), after sleep(): " + listUnchecked.size());
                for (Socket socket : listUnchecked) {
                    System.out.println("Unchecked remove: " + socket.toString());
                    if(listWebSocket.contains(socket)){
                        ServerThreadWebSocket.close(socket);
                    }else {
                        ServerThread.close(socket);
                    }
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
        String[] stringBytes;
        try {
            out = socket.getOutputStream();
            Scanner scan = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream())));
            line = scan.next();
            //System.out.println("line: " + line);
            stringBytes = line.split("/.../");
            if(stringBytes[0].equals("type")){
                clientType = stringBytes[1];
                MethodsServerThread.sendMessage(new PrintWriter(socket.getOutputStream()), "type");
            }else{
                clientType = "WebSocket";
                listWebSocket.add(socket);
                data = line + scan.useDelimiter("\\r\\n\\r\\n").next();
                //System.out.println("data: \n" + data);
            }
            System.out.println("ClientType: " + clientType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * serverRunning()
     */
    private static void serverRunning(){
        System.out.println("Server started (IP: " + serverSocket.getInetAddress().getHostAddress() + "; Port: " + port + ")");
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

        //start UI
        new UI().setVisible(true);

        while(check){
            try{
                Socket clientSocket = serverSocket.accept();

                getClientType(clientSocket);

                if(clientType != null) {
                    if(listWebSocket.contains(clientSocket)) {
                        //if(clientType.equals("WebSocket")) {
                        //System.out.println("vorher");
                        //WebSocket Handshake
                        try {
                            //System.out.println("vor get");
                            Matcher get = Pattern.compile("^GET").matcher(data);
                            boolean find = get.find();
                            if (find) {
                                //System.out.println("get.find()");
                                Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                                if(match.find()) {
                                    //System.out.println("match.find()");
                                    byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                                            + "Connection: Upgrade\r\n"
                                            + "Upgrade: websocket\r\n"
                                            + "Sec-WebSocket-Accept: "
                                            + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)))
                                            + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
                                    out.write(response, 0, response.length);

                                    String ip = clientSocket.getInetAddress().getHostAddress();
                                    Thread serverThreadWebSocket = new Thread(new ServerThreadWebSocket(clientSocket));
                                    listSocket.add(clientSocket);
                                    //listIP.add(ip);
                                    serverThreadWebSocket.setName(clientType + "_" + serverThreadWebSocket.getId());
                                    System.out.println("serverThread-Name: " + serverThreadWebSocket.getName());
                                    serverThreadWebSocket.start();
                                }
                            }
                        } catch (NoSuchAlgorithmException nsae) {
                            System.out.println("error");
                            nsae.printStackTrace();
                        }
                    }else {
                        String ip = clientSocket.getInetAddress().getHostAddress();
                        Thread serverThread = new Thread(new ServerThread(clientSocket));
                        listSocket.add(clientSocket);
                        //listIP.add(ip);
                        serverThread.setName(clientType + "_" + serverThread.getId());
                        System.out.println("serverThread-Name: " + serverThread.getName());
                        serverThread.start();
                    }

                    /*/sendCheck
                    if(ServerThread.listSocket.size() == 1){
                        new Thread(Main::sendCheck).start();
                    }//*/
                }else{
                    System.out.println("clientType == null");
                }//*/
            }catch(IOException ioe) {
                check = false;
                ioe.printStackTrace();
            }
        }
    }

    /**
     * startServer()
     * rekursive Methode, damit bei einem Fehlversuch der Server wieder gestartet werden kann, wenn der User es möchte
     */
    public static void startServer(){
        try{
            //System.out.println("ip: " + InetAddress.getLocalHost());
            String[] ipBytes = InetAddress.getLocalHost().toString().split("/");
            //System.out.println("ipBytes: " + Arrays.toString(ipBytes));
            String[] bytesIP = ipBytes[1].split("\\.");
            //System.out.println("bytesIP: " + Arrays.toString(bytesIP));//*/

            check = true;

            if(bytesIP[0].equals("136") && bytesIP[1].equals("199") && bytesIP[2].equals("4")) {
                serverSocket = new ServerSocket();
                serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), port));
            }else {
                serverSocket = new ServerSocket(5000);
            }
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
}
