package src.Server;

import java.io.*;
import java.net.Socket;
import java.nio.ByteOrder;
import java.util.StringTokenizer;

import src.Benchmarks.PixelBenchmark;
import src.Mandelbrot.Task;

public class AndroidSocketThread implements Runnable {

    private final Socket socket;
    private final Server server;

    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private DataOutputStream dataOutputStream;

    private final PixelBenchmark bm = new PixelBenchmark();
    private final PixelBenchmark fm = new PixelBenchmark();

    private final Thread thread;
    private Task task;

    private boolean disconnected;
    private boolean connected;

    public AndroidSocketThread(Socket socket, Server server) {
        System.out.println("AndroidSocket");

        this.socket = socket;
        this.server = server;
        this.thread = new Thread(this);

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

    private void sendMessage(String text) {
        printWriter.println(text);
        printWriter.flush();
    }

    private void receiveMessage() {

        String input;
        StringTokenizer token;
        String compare;

        try {
            while (((input = (bufferedReader.readLine())) != null) && !Thread.currentThread().isInterrupted()) {
                //System.out.println("input (vor): " + input);

                bm.start();
                fm.start();
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
                        //sendTask();
                        server.setImage();
                        break;
                    case "frame":
                        server.setImage();
                        fm.stop();
//					    System.out.println("Total time frame (real): " + fm.getResult());
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
        sendMessage("First contact successful");
        thread.start();
    }

    private void connect() throws IOException {
        connected = true;
        System.out.println("Size: " + server.getMANDELBROT_PANEL_WIDTH() + "x" + server.getMANDELBROT_PANEL_HEIGHT());
        int width = server.getMANDELBROT_PANEL_WIDTH();
        int height = server.getMANDELBROT_PANEL_HEIGHT();
        sendMessage("Connect success/.../" + width + "/.../" + height);
        server.connect();
    }

    private void disconnect() {
        if (connected) {
            disconnected = true;
            close();
            server.disconnect();
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

        //int y = java.nio.ByteBuffer.wrap(task.getY()).order((ByteOrder.LITTLE_ENDIAN)).getInt();
        sendMessage(task.getY());

        receiveMessage();

        //double xMove = java.nio.ByteBuffer.wrap(task.getxMove()).order((ByteOrder.LITTLE_ENDIAN)).getDouble();
        sendMessage(task.getxMove());

        receiveMessage();

        //double yMove = java.nio.ByteBuffer.wrap(task.getyMove()).order((ByteOrder.LITTLE_ENDIAN)).getDouble();
        sendMessage(task.getyMove());

        receiveMessage();

        //double zoom = java.nio.ByteBuffer.wrap(task.getZoom()).order((ByteOrder.LITTLE_ENDIAN)).getDouble();
        sendMessage(task.getZoom());

        receiveMessage();

        //int itr = java.nio.ByteBuffer.wrap(task.getItr()).order((ByteOrder.LITTLE_ENDIAN)).getInt();
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
        bm.stop();
    }

    private void close() {
        System.out.println(Thread.currentThread().getName() + ": Connection Closing...");
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
        System.out.println(Thread.currentThread().getName() + " beendet");

    }
}