package com.example.studienprojekt_android;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private final Connect connect;
    private final String ip;
    private final int port;

    private static PrintWriter mBufferOut;

    private static Socket socket;

    /************** Getter **************/
    public Socket getSocket() {
        return socket;
    }

    /**
     * Constructor of {@code Client}
     * @param connect Connect
     * @param ip IP-Address
     * @param port Port-Number
     */
    public Client(Connect connect, String ip, int port){
        this.connect = connect;
        this.ip = ip;
        this.port = port;
    }

    /**
     * run()
     */
    public void run() {
        try {
            InetAddress serverAdr = InetAddress.getByName(ip);
            socket = new Socket(serverAdr, port);
            connect.setConnected(true);
        } catch (Exception e) {
            connect.setConnected(false);
            Log.e("Socket_Error", "" + e);
        }
    }

    /**
     * sendMessageRunnable()
     * @param type String
     * @param content String
     * @return runnable Runnable
     */
    private Runnable sendMessageRunnable(String type, String content){
        return () -> {
            try {
                String message = type + "/.../" + content;
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mBufferOut.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    /**
     * sendMessage()
     * Sends the message entered by client to the server
     * @param type String
     * @param content String
     */
    public synchronized void sendMessage(String type, String content) {
        new Thread(sendMessageRunnable(type, content)).start();
    }

    /**
     * sendMessageRunnable()
     * @param message String
     * @return runnable Runnable
     */
    private Runnable sendMessageRunnable(String message){
        return () -> {
            try {
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mBufferOut.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    /**
     * sendMessage()
     * Sends the message entered by client to the server
     * @param message String
     */
    public synchronized void sendMessage(String message) {
        new Thread(sendMessageRunnable(message)).start();
    }

    /**
     * stopClient()
     * Close the connection and release the members
     */
    public void stopClient() {
        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
            mBufferOut = null;
        }
        try {
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

