//Lars Klee

package com.example.studienprojekt_android;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Objects;

public class Reader extends Fragment implements Runnable{
    private final Connect connect;
    private final FragmentActivity fragmentActivity;
    private final FirstFragment firstFragment;
    private final SecondFragment secondFragment;
    private final CpuInfo cpuInfo;
    private final TextView txtAnzClients;

    private ArrayList<Object> taskList = new ArrayList<>();

    private boolean task;

    private String packageComplete = "";
    private int anzPackage = 0;
    private int width = 0;
    private int height = 0;
    private String[] bytes = new String[]{"0"};

    private static BufferedReader bufferedReader;
    private static DataInputStream dataInputStream;
    private static Socket socket;

    private int anzTask = 0;

    private static Thread plotPointsThread;

    private Object taskObj = null;
    private String response = null;

    private int anzClients;
    private int anzRunning;
    private int anzThreads;

    // time measurement
    long start;
    long finish;
    long timeElapsed;
    long timeTotal;
    int anzTotalPackage;
    ArrayList<Object> listTime = new ArrayList<>();

    /************** Getter **************/
    public int getAnzClients() {
        return anzClients;
    }

    //Used to load the 'native-lib library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * Constructor of {@code Reader}
     * @param socket Socket
     * @param fragmentActivity FragmentActivity
     * @param txtAnzClients TextView
     * @param firstFragment FirstFragment
     * @param secondFragment SecondFragment
     * @param cpuInfo CpuInfo
     */
    public Reader(Connect connect, Socket socket, FragmentActivity fragmentActivity, TextView txtAnzClients, FirstFragment firstFragment, SecondFragment secondFragment, CpuInfo cpuInfo){
        this.connect = connect;
        Reader.socket = socket;
        this.fragmentActivity = fragmentActivity;
        this.txtAnzClients = txtAnzClients;
        this.firstFragment = firstFragment;
        this.secondFragment = secondFragment;
        this.cpuInfo = cpuInfo;
        initializeNativeLib();
        Client.sendMessage("type", "Android");
    }

    /**
     * receiveMessage()
     */
    private synchronized void receiveMessage(){
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dataInputStream = new DataInputStream(socket.getInputStream());
            while(!SecondFragment.getReceiveThread().isInterrupted()) {
                try {
                    if(!task){
                        response = bufferedReader.readLine();
                    }
                } catch (NullPointerException e) {
                    response = null;
                    e.printStackTrace();
                }
                //Log.d("Response", "response: " + response + "; taskObj != null: " + (taskObj != null));
                if (response != null || taskObj != null) {
                    if(response != null) {
                        bytes = response.split("/.../");
                    }

                    switch (bytes[0]) {
// First contact successful
                        case "First contact successful":
                            typeMethod();
                            break;
// Connect success
                        case "Connect success":
                            connectSuccess(bytes[1], bytes[2]);
                            break;
// task
                        case "task":
                            task();
                            break;
// endTask
                        case "end Task":
                            endTask();
                            break;
// noTask
                        case "noTask":
                            noTask();
                            break;
// anzClients
                        case "anzClients":
                            anzClientsMethod(bytes[1]);
                            break;
// close
                        case "close":
                            closeMethod();
                            break;
// disconnect
                        case "disconnect":
                            disconnectMethod();
                            break;
// check
                        case "check":
                            checkMethod();
                            break;
                        default:
                            break;
                    }
                }else{
                    Log.d("Server-Error", "Connection lost");
                    quit();
                }
            }
            end();
            quit();
        } catch (IOException ioe) {
            end();
            quit();
            ioe.printStackTrace();
        }
    }

    /**
     * typeMethod()
     */
    private void typeMethod() {
        Client.sendMessage("connect");
    }

    /**
     * connectSuccess()
     * @param widthStr String
     * @param heightStr String
     */
    private void connectSuccess(String widthStr, String heightStr){
        Log.d("Response", "width: " + width + "; height: " + height);
        width = Integer.parseInt(widthStr);
        height = Integer.parseInt(heightStr);
        getSize(width, height);
    }

    /**
     * task()
     */
    private void task(){
        getVariables();
        task = true;
    }

    /**
     * getVariables()
     */
    private void getVariables(){
        if(task) {
            Client.sendMessage("s");
            if (anzTask == 0 || anzTask == 4) {
                try {
                    taskObj = dataInputStream.readInt();
                    Log.d("taskObj", "int: " + taskObj);
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }
            } else {
                try{
                    taskObj = dataInputStream.readDouble();
                    Log.d("taskObj", "double: " + taskObj);
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }
            }
            taskList.add(taskObj);
            if (anzTask == 4) {
                response = "end Task/.../";
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
        int y =  (int) taskList.get(0);
        double xMove = (double) taskList.get(1);
        double yMove = (double) taskList.get(2);
        double zoom = (double) taskList.get(3);
        //int itr = (int) taskList.get(4);
        int itr = 200;
        taskList.clear();

        //plotPointsThread = new Thread(runPlotPoints(y, xMove, yMove, zoom ,itr));
        //plotPointsThread.start();
        /*if(!secondFragment.getStop() && !SecondFragment.getReceiveThread().isInterrupted()) {
            start = System.nanoTime();
            pointsPlot(y, xMove, yMove, zoom, itr);
        }//*/
    }

    /**
     * noTask()
     */
    private void noTask(){
        if(!secondFragment.getStop()) {
            timeTotal = 0;
            anzTotalPackage = 0;
            Client.sendMessage("task");
        }
    }

    /**
     * anzClientsMethod()
     * @param string String
     */
    private void anzClientsMethod(String string) {
        anzClients = Integer.parseInt(string);
        if (anzClients > 1) {
            if (plotPointsThread != null) {
                plotPointsThread.interrupt();
            }
        }
        if(secondFragment.getViewCreated()) {
            fragmentActivity.runOnUiThread(new Thread(() -> {
                txtAnzClients.setText("");
                txtAnzClients.append("Clients: " + anzRunning + "/" + anzClients);
            }));
        }
        if(cpuInfo.getNumCores() <= 4){
            anzThreads = 1;
        }else{
            anzThreads = (cpuInfo.getNumCores() - 4);
        }
    }

    /**
     * closeMethod()
     */
    private void closeMethod() {
        if(plotPointsThread != null) {
            plotPointsThread.interrupt();
        }
        anzClients--;
        if(secondFragment.getViewCreated()) {
            fragmentActivity.runOnUiThread(new Thread(() -> {
                txtAnzClients.setText("");
                txtAnzClients.append("Clients: " + anzRunning + "/" + anzClients);
            }));
        }
    }

    /**
     * disconnectMethod()
     */
    private void disconnectMethod() {
        if(plotPointsThread != null) {
            plotPointsThread.interrupt();
        }
        connect.setConnected(false);
        anzClients = 0;
        anzRunning = 0;
        secondFragment.quit();
        quit();
    }

    /**
     * checkMethod()
     */
    private void checkMethod() {
        Client.sendMessage("check");
    }

    /**
     * end()
     */
    private void end(){
        secondFragment.setStop(true);
        secondFragment.setInterrupt(true);
        secondFragment.quit();
        task = false;
    }

    /**
     * quit()
     */
    public static void quit() {
        try {
            SecondFragment.getReceiveThread().interrupt();
            bufferedReader.close();
            dataInputStream.close();
            socket.close();
            if(plotPointsThread != null) {
                plotPointsThread.interrupt();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * run()
     */
    @Override
    public void run(){
        receiveMessage();
    }

    /**
     * initializePackage()
     * @param x int
     * @param y int
     * @param tmp int
     */
    public synchronized void initializePackage(int x, int y, int tmp){
        if(!secondFragment.getStop()) {
            //Log.d("Response", "initializePackage: " + x + ", " + y + ", " + tmp);
            if(anzPackage != 0){
                packageComplete += "\n";
            }
            packageComplete += "" + x;
            packageComplete += "\n";
            packageComplete += "" + y;
            packageComplete += "\n";
            packageComplete += "" + tmp;
            //Log.d("Response", "anzPackage " + anzPackage +" < " + width);
            /*if (anzPackage < width) {
                packageComplete += "\n";
            } else {
                packageComplete += "\ntick";
            }//*/
            anzPackage++;
        }
    }

    /**
     * sendCompletePackage()
     */
    private synchronized void sendCompletePackage(){
        if(!secondFragment.getStop()) {
            //Log.d("Response", "sendCompletePackage: " + packageComplete);
            packageComplete += "\ntick";
            if (secondFragment.getStarted()) {
                //packageComplete += "\ntask";
            }
            Client.sendMessage(packageComplete);
            packageComplete = "";
            anzPackage = 0;
            secondFragment.setStop(secondFragment.getStop());
        }
    }

    /**
     * sendPlotPointsFinished()
     */
    public synchronized void sendPlotPointsFinished(){
        if(!secondFragment.getStop()) {
            sendCompletePackage();
            finish = System.nanoTime();
            timeElapsed = finish - start;
            Log.d("plot", "Name: " + Thread.currentThread().getName() + "; timePackage: " + (timeElapsed / 1000000000.0) + " sec");
            timeTotal += timeElapsed;
            Log.d("plot", "Name: " + Thread.currentThread().getName() + "; timePackageTotal: " + (timeTotal / 1000000000.0) + " sec");
            anzTotalPackage++;
            Log.d("plot", "Name: " + Thread.currentThread().getName() + "; anzTotalPackage: " + anzTotalPackage);
        }
    }

    /**
     * runPlotPoints()
     * @param y int
     * @param xMove double
     * @param yMove double
     * @param zoom double
     * @param itr int
     * @return Runnable
     */
    private Runnable runPlotPoints(int y, double xMove, double yMove, double zoom, int itr){
        return () -> pointsPlot(y, xMove, yMove, zoom, itr);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    private native void initializeNativeLib();

    private native void getSize(int width, int height);

    private native void pointsPlot(int y, double MoveX, double MoveY, double zoom, int itr);
}

