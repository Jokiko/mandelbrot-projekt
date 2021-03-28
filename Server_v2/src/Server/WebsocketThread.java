package src.Server;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import src.Mandelbrot.Task;
public class WebsocketThread implements Runnable {
    private final Socket socket;
    private final Server server;
    private BufferedReader reader;
    private PrintWriter writer;
    private DataOutputStream dout;
    private static InputStream is = null;
    private final Thread thread;
    private Task task;
    private boolean disconnected;
    private boolean connected;

    public WebsocketThread(Socket socket, Server server, String name) {
        this.socket = socket;
        this.server = server;
        this.thread = new Thread(this);
        this.thread.setName("Thread_" + name);
        connected = false;
        disconnected = false;
        initializeStreams();
        sendMessage("size/.../"+server.getMandelbrotWidth()+"/.../"+server.getMandelbrotHeight());
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
    //encode aus https://stackoverflow.com/questions/43163592/standalone-websocket-server-without-jee-application-server
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

    private void sendMessage(String text) {
        if(!text.equals("noTask")) System.out.println("WebsocketThread-" + thread.getId() + ": " + text);
        try {

            byte[] rawData = text.getBytes();
            byte[] reply = encode(rawData);
            dout.write(reply, 0, reply.length);
            dout.flush();
        } catch (SocketException exception) {
            exception.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Lesen aus https://stackoverflow.com/questions/43163592/standalone-websocket-server-without-jee-application-server
    private void receiveMessage() {
        try {
            is = socket.getInputStream();

        } catch (Exception e) {
            System.out.println("Error in serverThreadWebSocket");
        }
        String line = null;
        byte[] b = new byte[8000];
        byte[] message =null;
        byte[] masks = new byte[4];
        boolean isSplit=false;
        int length = 0;
        int totalRead =0;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int len = 0;
                try {
                    len = is.read(b);
                } catch (IOException e) {
                    break;
                }
                // Dekodierung der WebSocket Nachricht
                if (len != -1) {
                    boolean more = false;
                    int totalLength = 0;

                    int j = 0;
                    int i = 0;
                    if (!isSplit) {
                        byte rLength = 0;
                        int rMaskIndex = 2;
                        int rDataStart = 0;
                        //Kein Check notwendig, da von Text ausgegangen wird
                        byte data = b[1];
                        byte op = (byte) 127;
                        // Check, wo die Laenge geschrieben ist, wenn <=125 dann ist das die Laenge
                        rLength = (byte) (data & op);
                        length = (int) rLength;
                        // wenn 126, dann steht in den naechsten 16 bit die Laenge
                        if (rLength == (byte) 126) {
                            rMaskIndex = 4;
                            length = Byte.toUnsignedInt(b[2]) << 8;
                            length += Byte.toUnsignedInt(b[3]);
                        }
                        // wenn 127, dann steht in den naechsten 64 bit die Laenge
                        else if (rLength == (byte) 127)
                            rMaskIndex = 10;
                        //auf die Laenge folgt ein 4 bit langer mask key, der zum dekodieren benoetigt wird
                        for (i = rMaskIndex; i < (rMaskIndex + 4); i++) {
                            masks[j] = b[i];
                            j++;
                        }
                        // auf den masking key folgen die verschluesselten Daten
                        rDataStart = rMaskIndex + 4;
                        message = new byte[length];
                        totalLength = length + rDataStart;
                        // Entschluesslung der Daten
                        for (i = rDataStart, totalRead = 0; i<len && i < totalLength; i++, totalRead++) {
                            message[totalRead] = (byte) (b[i] ^ masks[totalRead % 4]);
                        }
                    }else {
                        for (i = 0; i<len && totalRead<length; i++, totalRead++) {
                            message[totalRead] = (byte) (b[i] ^ masks[totalRead % 4]);
                        }
                    }
                    if (totalRead<length) {
                        isSplit=true;
                    }else {
                        isSplit=false;
                        //System.out.println(new String(message));
                        line = new String(message,  StandardCharsets.UTF_8);
                        b = new byte[8000];
                    }


                } else
                    break;
                //System.out.println(line);
                switch (Objects.requireNonNull(line)) {
                    case "connect":
                        connect();
                        break;
                    case "task":
                        sendTask();
                        break;
                    case "tick":
                        server.setImage();
                        break;
                    case "s":
                        return;
                    case "plot":
                        break;
                    default:
                        plot(line);
                }
            }
        }
        catch (Exception e){
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
            if(task != null){
                server.addToTaskList(task);
            }
            disconnected = true;
            close();
            server.disconnect("WebSocket");
        }
    }
    private void sendTask() {
        task = server.getTask();
        if (task == null) {
            sendMessage("noTask");
            return;
        }
        String infos = "task/.../"+task.getY() +"/.../"+task.getXMove()+"/.../"+task.getYMove()+"/.../"+task.getZoom()+"/.../"+task.getItr();
        sendMessage(infos);
    }
    private synchronized void plot(String compare){
        if(compare.equals("")||compare.equals("end")){
            System.out.println("Leer");
        }
        String[] plotti = compare.split("/.../");
        try {
            for (int i = 0; i < server.getMandelbrotWidth()*3; i = i+3) {
                int x = Integer.parseInt(plotti[i]);
                int y = Integer.parseInt(plotti[i + 1]);
                int itr = Integer.parseInt(plotti[i + 2]);
                server.setRGB(x, y, itr);
            }
            server.setImage();
            task = null;
            sendTask();
        }
        catch(NumberFormatException nFe) {
            System.out.println("NumberFormatException");
            server.addToTaskList(task);
            sendTask();
        }
    }
    private void close() {
        System.out.println(Thread.currentThread().getName() + ": Connection Closing...");
        thread.interrupt();
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
        System.out.println(Thread.currentThread().getName() + " terminated");
    }
}