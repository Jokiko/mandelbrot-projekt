package src.Server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.StringTokenizer;

import src.Benchmarks.PixelBenchmark;
import src.Mandelbrot.Task;


public class WebsocketThread implements Runnable {
    private Socket socket;
    private Server server;

    private BufferedReader reader;
    private PrintWriter writer;
    private DataOutputStream dout;

    private static PrintWriter os = null;
    private static InputStream is = null;
    private static OutputStream out = null;

    private PixelBenchmark bm = new PixelBenchmark();
    private PixelBenchmark fm = new PixelBenchmark();

    private Thread thread;
    private Task task;

    private boolean disconnected;
    private boolean connected;

    public WebsocketThread(Socket socket, Server server) {

        this.socket = socket;
        this.server = server;
        this.thread = new Thread(this);

        connected = false;
        disconnected = false;

        initializeStreams();

    }

    private void initializeStreams() {

        try {
            reader = new BufferedReader((new InputStreamReader(socket.getInputStream())));
            writer = new PrintWriter(socket.getOutputStream());
            dout = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static byte[] encode(byte[] rawData){
        
        int frameCount  = 0;
        byte[] frame = new byte[10];

        frame[0] = (byte) 129;

        if(rawData.length <= 125){
            frame[1] = (byte) rawData.length;
            frameCount = 2;
        }else if(rawData.length >= 126 && rawData.length <= 65535){
            frame[1] = (byte) 126;
            int len = rawData.length;
            frame[2] = (byte)((len >> 8 ) & (byte)255);
            frame[3] = (byte)(len & (byte)255);
            frameCount = 4;
        }else{
            frame[1] = (byte) 127;
            int len = rawData.length;
            frame[2] = (byte)((len >> 56 ) & (byte)255);
            frame[3] = (byte)((len >> 48 ) & (byte)255);
            frame[4] = (byte)((len >> 40 ) & (byte)255);
            frame[5] = (byte)((len >> 32 ) & (byte)255);
            frame[6] = (byte)((len >> 24 ) & (byte)255);
            frame[7] = (byte)((len >> 16 ) & (byte)255);
            frame[8] = (byte)((len >> 8 ) & (byte)255);
            frame[9] = (byte)(len & (byte)255);
            frameCount = 10;
        }

        int bLength = frameCount + rawData.length;

        byte[] reply = new byte[bLength];

        int bLim = 0;
        for(int i=0; i<frameCount;i++){
            reply[bLim] = frame[i];
            bLim++;
        }
        for(int i=0; i<rawData.length;i++){
            reply[bLim] = rawData[i];
            bLim++;
        }

        return reply;
    }

    private void sendMessage(byte[] task) throws IOException {
        try {

            byte[] reply = encode(task);

            dout.write(reply, 0, reply.length);
            dout.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String text) {
        System.out.println("WebsocketThread-" + thread.getId() + ": " + text);
        try {

            byte[] rawData = text.getBytes();
            byte[] reply = encode(rawData);

            dout.write(reply, 0, reply.length);
            dout.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveMessage() {

        StringTokenizer token;
        String compare;

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
            while (!Thread.currentThread().isInterrupted()) {
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
                }
                token = new StringTokenizer(line, "/.../");
                compare = token.nextElement().toString();
                System.out.println(compare);

                switch (compare) {
                    case "connect":
                        connect();
                        break;
                    case "task":
                        sendTask();
                        break;
                    case "tick":
                        server.setImage();
                        break;
                    case "frame":
                        fm.stop();
                        System.out.println("Total time frame (real): " + fm.getResult());
                        break;
                    default:
                        plot(line);
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

    private void connect() {
        connected = true;
        sendMessage("Connect success");
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
        sendMessage(task.getY());
        sendMessage(task.getxMove());
        sendMessage(task.getyMove());
        sendMessage(task.getZoom());
        sendMessage(task.getItr());

    }

    private void plot(String compare) throws IOException {
        int x;
        int y;
        int itr;
        int colorItr = 20;
        x = Integer.parseInt(compare);
        y = Integer.parseInt(reader.readLine());
        itr = Integer.parseInt(reader.readLine());
        server.setRGB(x, y, itr | (itr << colorItr));
        bm.stop();
    }

    private void close() {
        System.out.println(Thread.currentThread().getName() + ": Connection Closing...");
        try {
            reader.close();
            System.out.println("BufferedReader closed: " + reader.toString());
            writer.close();
            System.out.println("OutputStream closed: " + writer.toString());
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
        System.out.println("ServerThread beendet");

    }
}