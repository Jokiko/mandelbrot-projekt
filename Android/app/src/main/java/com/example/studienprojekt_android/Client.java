package com.example.studienprojekt_android;

import android.os.Looper;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private static final String TAG = Client.class.getSimpleName();
    private static String SERVER_IP;
    private static int SERVER_PORT;
    private static PrintWriter mBufferOut;

    static Socket socket;

    /*public Reader mReader;
    public static Thread receiveThread;
    FragmentActivity activity;//*/

    /**
     * Client()
     * @param ip IP-Address
     * @param port Port-Number
     */
    Client(String ip, int port){//, FragmentActivity fa){
        SERVER_IP = ip;
        SERVER_PORT = port;
        //activity = fa;
    }

    /**
     * sendMessage()
     * Sends the message entered by client to the server
     * @param type String
     * @param content String
     */
    public synchronized static void sendMessage(String type, String content) {
        Runnable runnable = () -> {
            try {
                String message = type + "/.../" + content;
                Log.d(TAG, "Sending: " + message);
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mBufferOut.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * stopClient()
     * Close the connection and release the members
     */
    public static void stopClient() {
        //sendMessage("close", "");
        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }
        mBufferOut = null;
        try {
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        Log.d("Client stop", "stopClient()");
    }

    /**
     * run()
     */
    public void run() {
        Log.d("main-thread", "Client.run(): " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
        try {
            InetAddress serverAdr = InetAddress.getByName(SERVER_IP);
            Log.d("TCP Client", "C: Connecting...");
            socket = new Socket(serverAdr, SERVER_PORT);
            FirstFragment.connected = true;
        } catch (Exception e) {
            FirstFragment.connected = false;
            Log.e("TCP", "C: Error", e);
        }
        /*if(FirstFragment.connected){
            mReader = new Reader(socket, activity);
            receiveThread = new Thread(mReader);
            receiveThread.start();
            Log.d("Reader","Reader.run()");
        }//*/
    }
}

