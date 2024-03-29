package src.Server;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

import src.Mandelbrot.Task;

public class AndroidSocketThread implements Runnable {

    private final Socket socket;
    private final Server server;

    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private DataOutputStream dataOutputStream;

    private final Thread thread;
    private Task task;

    private boolean disconnected;
    private boolean connected;

    public AndroidSocketThread(Socket socket, Server server, String name) {
        System.out.println("Name: " + name);

        this.socket = socket;
        this.server = server;
        this.thread = new Thread(this);
        this.thread.setName("Thread_" + name);

        connected = false;
        disconnected = false;

        initializeStreams();

    }

    private void initializeStreams() {

        try {
            bufferedReader = new BufferedReader((new InputStreamReader(socket.getInputStream())));
            printWriter = new PrintWriter(socket.getOutputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendMessage(int task) throws IOException {
        dataOutputStream.writeInt(task);
        dataOutputStream.flush();
    }

    private void sendMessage(double task) throws IOException {
        dataOutputStream.writeDouble(task);
        dataOutputStream.flush();
    }

    public void sendMessage(String text) {
        printWriter.println(text);
        printWriter.flush();
    }

    private void receiveMessage() {

        String input;
        StringTokenizer token;
        String compare;

        try {
            while (((input = bufferedReader.readLine()) != null) && !Thread.currentThread().isInterrupted()) {
                //System.out.println("input (vor): " + input);

                token = new StringTokenizer(input, "/.");
                compare = token.nextElement().toString();

                switch (compare) {
                    case "connect":
                        connect();
                        break;
                    case "task":
                        sendTask();
                        break;
                    case "tick":
                        task = null;
                        server.setImage();
                        break;
                    case "s":
                        return;
                    default:
                        plot(input);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        thread.start();
        sendMessage("First contact successful");
    }

    private void connect() throws IOException {
        connected = true;
        int width = server.getMandelbrotWidth();
        int height = server.getMandelbrotHeight();
        sendMessage("Connect success/.../" + width + "/.../" + height);
        server.connect();
    }

    private void disconnect() {
        if (connected) {
            if(task != null){
                server.addToTaskList(task);
            }
            disconnected = true;
            close();
            server.disconnect("Android");
        }
    }

    private void sendTask() throws IOException {

        task = server.getTask();

        if (task == null) {
            sendMessage("noTask");
            return;
        }

        sendMessage("task");
        receiveMessage();

        sendMessage(task.getY());
        receiveMessage();

        sendMessage(task.getXMove());
        receiveMessage();

        sendMessage(task.getYMove());
        receiveMessage();

        sendMessage(task.getZoom());
        receiveMessage();

        sendMessage(task.getItr());
    }

    private void plot(String compare) throws IOException {
        int x;
        int y;
        int itr;
        x = Integer.parseInt(compare);
        y = Integer.parseInt(bufferedReader.readLine());
        itr = Integer.parseInt(bufferedReader.readLine());

        //System.out.println("x: " + x + ", y: " + y + "; itr: " + itr);
        server.setRGB(x, y, itr);
    }

    private void close() {
        System.out.println("\n" + Thread.currentThread().getName() + ": Connection Closing...");
        thread.interrupt();
        try {
            bufferedReader.close();
            System.out.println("BufferedReader closed: " + bufferedReader.toString());
            printWriter.close();
            System.out.println("OutputStream closed: " + printWriter.toString());
            socket.close();
            System.out.println("Socket closed: " + socket.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        receiveMessage();

        if (!disconnected)
            disconnect();
        System.out.println(Thread.currentThread().getName() + " terminated");

    }
}