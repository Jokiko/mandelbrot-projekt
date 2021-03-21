package src.Server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

    private int x = 0;
    private int y = 0;
    private int itr = 0;
    private int failsafe;
    private int plotCount = 0;
    private boolean overflow = false;

    public WebsocketThread(Socket socket, Server server) {

        this.socket = socket;
        this.server = server;
        this.thread = new Thread(this);

        connected = false;
        disconnected = false;

        initializeStreams();
        sendMessage("size/.../"+server.getMANDELBROT_PANEL_WIDTH()+"/.../"+server.getMANDELBROT_PANEL_HEIGHT());
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

        }
        catch (SocketException exception){
            System.out.println("SocketExpection in sendMessage");
        }
        catch (Exception e) {
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
        byte[] b = new byte[8000];//incoming buffer
        byte[] message =null;//buffer to assemble message in
        byte[] masks = new byte[4];
        boolean isSplit=false;//has a message been split over a read
        int length = 0; //length of message
        int totalRead =0; //total read in message so far
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int len = 0;//length of bytes read from socket
                try {
                    len = is.read(b);
                } catch (IOException e) {
                    break;
                }
                if (len != -1) {
                    boolean more = false;
                    int totalLength = 0;
                    do {
                        int j = 0;
                        int i = 0;
                        if (!isSplit) {
                            byte rLength = 0;
                            int rMaskIndex = 2;
                            int rDataStart = 0;
                            // b[0] assuming text
                            byte data = b[1];
                            byte op = (byte) 127;
                            rLength = (byte) (data & op);
                            length = (int) rLength;
                            if (rLength == (byte) 126) {
                                rMaskIndex = 4;
                                length = Byte.toUnsignedInt(b[2]) << 8;
                                length += Byte.toUnsignedInt(b[3]);
                            } else if (rLength == (byte) 127)
                                rMaskIndex = 10;
                            for (i = rMaskIndex; i < (rMaskIndex + 4); i++) {
                                masks[j] = b[i];
                                j++;
                            }

                            rDataStart = rMaskIndex + 4;

                            message = new byte[length];
                            totalLength = length + rDataStart;
                            for (i = rDataStart, totalRead = 0; i<len && i < totalLength; i++, totalRead++) {
                                message[totalRead] = (byte) (b[i] ^ masks[totalRead % 4]);
                            }


                        }else {
                            for (i = 0; i<len && totalRead<length; i++, totalRead++) {
                                message[totalRead] = (byte) (b[i] ^ masks[totalRead % 4]);
                            }
                            totalLength=i;
                        }


                        if (totalRead<length) {
                            isSplit=true;
                        }else {
                            isSplit=false;
                            //System.out.println(new String(message));
                            line = new String(message,  StandardCharsets.UTF_8);
                            b = new byte[8000];
                        }

                       /* if (totalLength < len) {
                            more = true;
                            for (i = totalLength, j = 0; i < len; i++, j++)
                                b[j] = b[i];
                            len = len - totalLength;
                            System.out.println("a running");
                        }else
                            more = false;*/
                    } while (more);
                } else
                    break;

                System.out.println(line);

                switch (line) {
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
                    case "s":
                        return;
                    case "plot":
                        plotCount = 1;
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
        int getY =  task.getY();
        sendMessage(String.valueOf(getY));
        receiveMessage();
        double xMove =  task.getxMove();
        sendMessage(String.valueOf(xMove));
        receiveMessage();
        double yMove = task.getyMove();
        sendMessage(String.valueOf(yMove));
        receiveMessage();
        double zoom = task.getZoom();
        sendMessage(String.valueOf(zoom));
        receiveMessage();
        int itr = task.getItr();
        System.out.println("Iterationen: "+ itr);
        sendMessage(String.valueOf(itr));

    }

    private synchronized void plot(String compare) throws IOException {
        int colorItr = 20;
        /*if(compare.equals("")){
            switch(plotCount) {
                //verlorene Nachricht ist "task"
                case 0:
                    if (x ==server.getMANDELBROT_PANEL_WIDTH()-1 || (overflow && x == 0)){
                        server.setImage();
                        sendTask();
                        overflow = false;
                        System.out.println("Sende Ersatztask");
                    }
                    break;
                //verlorene Nachricht ist x-Wert
                case 1:
                    if(x < server.getMANDELBROT_PANEL_WIDTH()-1){
                        x++;
                        overflow = false;
                    }
                    else {
                        x = 0;
                        overflow = true;
                    }

                    System.out.println("Schummel x bei: " + x);
                    plotCount++;
                    break;
                //verlorenene Nachricht ist y-Wert
                case 2:
                    if(x == 0 && overflow) y++;
                    System.out.println("Schummel y bei: " + y);
                    plotCount++;
                    break;
                //verlorene Nachricht ist itr-Wert
                case 3:
                    System.out.println("failsafe ist: "+ failsafe);
                    server.setRGB(x, y, failsafe | (failsafe << colorItr));
                    plotCount = 0;
            }
            return;
        }
        switch(plotCount) {
            case 1:
                x = Integer.parseInt(compare);
                plotCount++;
                break;
            case 2:
                y = Integer.parseInt(compare);
                plotCount++;
                break;

            case 3:
                itr = Integer.parseInt(compare);
                failsafe = itr;
                server.setRGB(x, y, itr | (itr << colorItr));
                plotCount = 0;

        }*/
        if(compare.equals("")||compare.equals("end")){
            System.out.println("Leer");
        }
        /*String[] plotti = compare.split("/.../");
        try {
            x = Integer.parseInt(plotti[0]);
            y = Integer.parseInt(plotti[1]);
            itr = Integer.parseInt(plotti[2]);
            server.setRGB(x, y, itr | (itr << colorItr));
        }
        catch(NumberFormatException nFe){
            System.out.println("NumberFormatExpecption");
        }*/
        String[] plotti = compare.split("/.../");
        try {
            for (int i = 0; i < 1500; i = i+3) {
                x = Integer.parseInt(plotti[i]);
                y = Integer.parseInt(plotti[i+1]);
                itr = Integer.parseInt(plotti[i+2]);
                server.setRGB(x, y, itr | (itr << colorItr));
            }
            server.setImage();
            sendTask();
        }
        catch(NumberFormatException nFe) {
            System.out.println("NumberFormatExpecption");
        }
        bm.stop();
        /*if (!compare.equals("")) {
            System.out.println("compare:");
            System.out.println(compare);
            BufferedReader reader = new BufferedReader(new StringReader(compare));
            System.out.println(reader.readLine());
            y = Integer.parseInt(reader.readLine());
            String ite = reader.readLine();
            x = 0;
            while (ite != null) {
                System.out.println(ite);
            itr = Integer.parseInt(ite);
            server.setRGB(x, y, itr | (itr << colorItr));
            x++;
                ite = reader.readLine();

            }
        }*/
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