package com.example.studienprojekt_android;

import android.util.Log;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class Reader extends Fragment implements Runnable{
    private final SecondFragment secondFragment;
    private final Client client;
    private final Socket socket;

    private final ArrayList<Object> taskList = new ArrayList<>();

    private BufferedReader bufferedReader;
    private DataInputStream dataInputStream;

    private Object taskObj = null;
    private String response = null;
    private String[] string;

    private boolean task;
    private int anzTask = 0;

    private String packageComplete = "";
    private int width = 0;
    private int height = 0;

    /**
     * Constructor of {@code Reader}
     * @param secondFragment SecondFragment
     * @param client Client
     */
    public Reader(SecondFragment secondFragment, Client client){
        this.secondFragment = secondFragment;
        this.client = client;
        this.socket = this.client.getSocket();
        this.client.sendMessage("type", "Android/.../" + secondFragment.getName());
    }

    /**
     * run()
     */
    @Override
    public void run(){
        receiveMessage();
    }

    /**
     * receiveMessage()
     */
    private void receiveMessage(){
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dataInputStream = new DataInputStream(socket.getInputStream());
            while(!secondFragment.getReaderThread().isInterrupted()) {
                try {
                    if(!task){
                        response = bufferedReader.readLine();
                    }
                } catch (NullPointerException e) {
                    response = null;
                    e.printStackTrace();
                }
                if (response != null || taskObj != null) {
                    if(response != null) {
                        string = response.split("/.../");
                    }

                    switch (string[0]) {
// First contact successful
                        case "First contact successful":
                            typeMethod();
                            break;
// Connect success
                        case "Connect success":
                            connectSuccess(string[1], string[2]);
                            break;
// task
                        case "task":
                            task();
                            break;
// endTask
                        case "endTask":
                            endTask();
                            break;
// noTask
                        case "noTask":
                            noTask();
                            break;
// check
                        case "check":
                            checkMethod();
                            break;
                        default:
                            break;
                    }
                }else{
                    Log.e("Server_Error", "Connection lost");
                    quit();
                }
            }
            quit();
        } catch (IOException ioe) {
            quit();
            ioe.printStackTrace();
        }
    }

    /**
     * typeMethod()
     */
    private void typeMethod() {
        client.sendMessage("connect");
    }

    /**
     * connectSuccess()
     * @param widthStr String
     * @param heightStr String
     */
    private void connectSuccess(String widthStr, String heightStr){
        width = Integer.parseInt(widthStr);
        height = Integer.parseInt(heightStr);
        setSize(width, height);
    }

    /**
     * task()
     */
    private void task(){
        task = true;
        getVariables();
    }

    /**
     * getVariables()
     */
    private void getVariables(){
        if(task) {
            client.sendMessage("s");
            if (anzTask == 0 || anzTask == 4) {
                try {
                    taskObj = dataInputStream.readInt();
                }catch(Exception ioe){
                    ioe.printStackTrace();
                }
            } else {
                try{
                    taskObj = dataInputStream.readDouble();
                }catch(Exception ioe){
                    ioe.printStackTrace();
                }
            }
            taskList.add(taskObj);
            if (anzTask == 4) {
                response = "endTask/.../";
            }
            anzTask++;
        }
    }

    /**
     * endTask()
     */
    private void endTask(){
        anzTask = 0;
        task = false;
        try {
            int y = (int) taskList.get(0);
            double xMove = (double) taskList.get(1);
            double yMove = (double) taskList.get(2);
            double zoom = (double) taskList.get(3);
            int itr = (int) taskList.get(4);
            taskList.clear();

            if(!secondFragment.getStop() && !secondFragment.getReaderThread().isInterrupted()) {
                pointsPlot(y, xMove, yMove, zoom, itr);
                sendCompletePackage();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * sendCompletePackage()
     */
    private void sendCompletePackage(){
        if(!secondFragment.getStop()) {
            packageComplete = getPackage();
            packageComplete += "tick";
            if (secondFragment.getStarted()) {
                packageComplete += "\ntask";
            }
            client.sendMessage(packageComplete);
            packageComplete = "";
            secondFragment.setStop(secondFragment.getStop());
            resetPackage();
        }
    }

    /**
     * noTask()
     */
    private void noTask(){
        if(!secondFragment.getStop() && secondFragment.getStarted()) {
            client.sendMessage("task");
        }
    }

    /**
     * checkMethod()
     */
    private void checkMethod() {
        client.sendMessage("check");
    }

    /**
     * quit()
     */
    public void quit() {
        secondFragment.setStop(true);
        secondFragment.setInterrupt(true);
        secondFragment.quit();
        task = false;
        try {
            secondFragment.getReaderThread().interrupt();
            bufferedReader.close();
            dataInputStream.close();
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    private native void setSize(int width, int height);

    private native void pointsPlot(int y, double xMove, double yMove, double zoom, int itr);

    private native String getPackage();

    private native void resetPackage();
}

