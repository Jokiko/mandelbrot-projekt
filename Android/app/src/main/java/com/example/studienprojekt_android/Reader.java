//Lars Klee

package com.example.studienprojekt_android;

import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Reader extends Fragment implements Runnable{

    TextView txtAnzClients;

    private static BufferedReader is;
    private static Socket socket;

    static Thread[] threads;
    static Thread calculateThread;
    ExecutorService executorService;
    Executor[] executor;
    Runnable[] runnable;

    static int moveX, moveY;

    public static int anzClients;
    static int count;
    static int anzThreads;
    private double factor;
    private boolean calculate;
    private String type;
    private String currentType;

    FragmentActivity activity;

    ArrayList<Object> listTime = new ArrayList<>();

    //Used to load the 'native-lib library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * Reader()
     * @param s Socket
     * @param fa FragmentActivity
     */
    Reader(Socket s, FragmentActivity fa, TextView textView){
        socket = s;
        activity = fa;
        txtAnzClients = textView;
        Client.sendMessage("type", "Android");
    }

    /**
     * receiveMessage()
     */
    private synchronized void receiveMessage(){
        Log.d("main-thread", "Reader.receiveMessage(): " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
        try {
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while(!SecondFragment.receiveThread.isInterrupted()) {
            //while(!Client.receiveThread.isInterrupted()) {
                Log.d("Response", "vor String response");
                String response;
                try {
                    response = is.readLine();
                } catch (NullPointerException e) {
                    response = null;
                    e.printStackTrace();
                }
                Log.d("Response", "" + response);
                if (response != null) {
                    String[] bytes = response.split("/.../");

                    Log.d("Response (bytes[0])", bytes[0]);

                    switch (bytes[0]) {
// type
                        case "type":
                            Client.sendMessage("connect", FirstFragment.deviceName);
                            FirstFragment.connected = true;
                            break;
// anzClients
                        case "anzClients":
                            anzClients = Integer.parseInt(bytes[1]);
                            getAnzClients(anzClients);
                            if(SecondFragment.viewCreated) {
                                activity.runOnUiThread(new Thread(() -> {
                                    //SecondFragment.txtAnzClients.setText("");
                                    //SecondFragment.txtAnzClients.append("Number of Clients: " + anzClients);
                                    txtAnzClients.setText("");
                                    txtAnzClients.append("Number of Clients: " + anzClients);
                                }));
                            }
                            if(CpuInfo.getNumCores() <= 4){
                                anzThreads = 1;
                            }else{
                                anzThreads = (CpuInfo.getNumCores() - 4);
                            }
                            Log.d("executorService", "anzThreads: " + anzThreads);
                            executorService = Executors.newFixedThreadPool(anzThreads);
                            Log.d("executorService", "" + executorService);
                            executor = new Executor[anzThreads];
                            Log.d("executor", "" + Arrays.toString(executor));
                            runnable = new Runnable[anzThreads];
                            Log.d("runnable", "" + Arrays.toString(runnable));//*/
                            break;
// yourNumber
                        case "yourNumber":
                            getYourNumber(Integer.parseInt(bytes[1]));
                            break;
// size
                        case "size":
                            Log.d("Response", "size");
                            int width = Integer.parseInt(bytes[1]);
                            getWidth(width);
                            Log.d("Width", "" + width);
                            int height = Integer.parseInt(bytes[2]);
                            getHeight(height);
                            calculate = true;
                            type = "plot";
                            currentType = type;
                            break;
// pauseChange
                        /*case "pauseChange":
                            if(threads != null) {
                                for (int k = 0; k < anzThreads; k++) {
                                    threads[k].interrupt();
                                }
                            }
                            if(calculateThread != null) {
                                calculateThread.interrupt();
                            }
                            anzClients--;
                            calculate = true;
                            break;//*/
// pause
                        case "pause":
                            if(threads != null) {
                                for (int k = 0; k < anzThreads; k++) {
                                    threads[k].interrupt();
                                }
                            }//*/
                            if(calculateThread != null) {
                                calculateThread.interrupt();
                            }//*/
                            calculate = false;
                            type = "pause";
                            break;
// resumeChange
                        /*case "resumeChange":
                            if(threads != null) {
                                for (int k = 0; k < anzThreads; k++) {
                                    threads[k].interrupt();
                                }
                            }
                            if(calculateThread != null) {
                                calculateThread.interrupt();
                            }
                            anzClients++;
                            calculate = true;
                            SecondFragment.started = true;
                            break;//*/
// resume
                        case "resume":
                            calculate = true;
                            SecondFragment.started = true;
                            if(currentType.equals("restart")){
                                type = "restartResume";
                            }else {
                                type = currentType;
                            }
                            break;
// restart
                        case "restart":
                            if(threads != null) {
                                for (int k = 0; k < anzThreads; k++) {
                                    threads[k].interrupt();
                                }
                            }//*/
                            if(calculateThread != null) {
                                calculateThread.interrupt();
                            }//*/
                            SecondFragment.started = true;
                            type = "restart";
                            currentType = type;
                            break;
// rectangle
                        case "rectangle":
                            if(threads != null) {
                                for (int k = 0; k < anzThreads; k++) {
                                    threads[k].interrupt();
                                }
                            }//*/
                            if(calculateThread != null) {
                                calculateThread.interrupt();
                            }//*/
                            moveX = Integer.parseInt(bytes[1]);
                            moveY = Integer.parseInt(bytes[2]);
                            factor = Double.parseDouble(bytes[3]);
                            calculate = true;
                            SecondFragment.started = true;
                            type = "rectangle";
                            currentType = type;
                            break;
// zoomIn
                        case "zoomIn":
                            if(threads != null) {
                                for (int k = 0; k < anzThreads; k++) {
                                    threads[k].interrupt();
                                }
                            }//*/
                            if(calculateThread != null) {
                                calculateThread.interrupt();
                            }//*/
                            factor = Double.parseDouble(bytes[1]);
                            calculate = true;
                            SecondFragment.started = true;
                            type = "zoomIn";
                            currentType = type;
                            break;
// zoomOut
                        case "zoomOut":
                            if(threads != null) {
                                for (int k = 0; k < anzThreads; k++) {
                                    threads[k].interrupt();
                                }
                            }//*/
                            if(calculateThread != null) {
                                calculateThread.interrupt();
                            }//*/
                            factor = Double.parseDouble(bytes[1]);
                            calculate = true;
                            SecondFragment.started = true;
                            type = "zoomOut";
                            currentType = type;
                            break;
// Up
                        case "Up":
                            if(threads != null) {
                                for (int k = 0; k < anzThreads; k++) {
                                    threads[k].interrupt();
                                }
                            }//*/
                            if(calculateThread != null) {
                                calculateThread.interrupt();
                            }//*/
                            factor = Double.parseDouble(bytes[1]);
                            calculate = true;
                            SecondFragment.started = true;
                            type = "Up";
                            currentType = type;
                            break;
// Down
                        case "Down":
                            if(threads != null) {
                                for (int k = 0; k < anzThreads; k++) {
                                    threads[k].interrupt();
                                }
                            }//*/
                            if(calculateThread != null) {
                                calculateThread.interrupt();
                            }//*/
                            factor = Double.parseDouble(bytes[1]);
                            calculate = true;
                            SecondFragment.started = true;
                            type = "Down";
                            currentType = type;
                            break;
// Left
                        case "Left":
                            if(threads != null) {
                                for (int k = 0; k < anzThreads; k++) {
                                    threads[k].interrupt();
                                }
                            }//*/
                            if(calculateThread != null) {
                                calculateThread.interrupt();
                            }//*/
                            factor = Double.parseDouble(bytes[1]);
                            calculate = true;
                            SecondFragment.started = true;
                            type = "Left";
                            currentType = type;
                            break;
// Right
                        case "Right":
                            if(threads != null) {
                                for (int k = 0; k < anzThreads; k++) {
                                    threads[k].interrupt();
                                }
                            }//*/
                            if(calculateThread != null) {
                                calculateThread.interrupt();
                            }//*/
                            factor = Double.parseDouble(bytes[1]);
                            calculate = true;
                            SecondFragment.started = true;
                            type = "Right";
                            currentType = type;
                            break;
// close
                        case "close":
                            if(threads != null) {
                                for (int k = 0; k < anzThreads; k++) {
                                    threads[k].interrupt();
                                }
                            }//*/
                            if(calculateThread != null) {
                                calculateThread.interrupt();
                            }//*/
                            calculate = false;
                            anzClients--;
                            getAnzClients(anzClients);
                            SecondFragment.quit();
                            stopReader();
                            break;
// disconnect
                        case "disconnect":
                            if(threads != null) {
                                for (int k = 0; k < anzThreads; k++) {
                                    threads[k].interrupt();
                                }
                            }//*/
                            if(calculateThread != null) {
                                calculateThread.interrupt();
                            }//*/
                            FirstFragment.connected = false;
                            anzClients = 0;
                            getAnzClients(anzClients);
                            SecondFragment.quit();
                            stopReader();
                            break;
// check
                        case "check":
                            Client.sendMessage("check", "");
                            //new Thread(() -> Client.sendMessage("check", "")).start();
                            break;
                        default:
                            calculate = false;
                            break;
                    }
                    if(threads != null){
                        for(int k = 0; k < anzThreads; k++){
                            getThreadInterrupted(threads[k].isInterrupted());
                            Log.d("Threads", "threads[" + k + "]: " + threads[k].isInterrupted());
                        }
                    }//*/
                    if(calculateThread != null) {
                        getThreadInterrupted(calculateThread.isInterrupted());
                    }//*/
                    Log.d("Response", "calculate: " + calculate);
                    if(calculate) {
                        Log.d("anzThreads", "" + anzThreads);
                        /*Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                for(int i = 0; i < anzThreads; i++){
                                    count = i;
                                    if(bytes[0].equals("rectangle")){
                                        pointsPlotRectangle(type, factor, moveX, moveY, count, anzThreads);
                                        Log.d("rectangle", moveX + ", " + moveY);
                                    }else {
                                        pointsPlot(type, factor, count, anzThreads);
                                    }
                                }
                            }
                        };
                        executorService.execute(runnable);
                        Log.d("executorService", "" + executorService);//*/

                    //executor
                        /*anzThreads = 2;
                        for(int i = 0; i < anzThreads; i++){
                            Log.d("executor_beginn", "i: " + i);
                            executor[i] = new Executor() {
                                @Override
                                public void execute(Runnable runnable) {
                                    new Thread(runnable).start();
                                }
                            };
                            count = i;
                            //runnable[i] = new Runnable() {
                            executor[i].execute(new Runnable() {
                                @Override
                                public void run() {
                                    long start = System.nanoTime();
                                    if(bytes[0].equals("rectangle")){
                                        pointsPlotRectangle(type, factor, moveX, moveY, count, anzThreads);
                                        Log.d("rectangle", moveX + ", " + moveY);
                                    }else {
                                        pointsPlot(type, factor, count, anzThreads);
                                    }
                                    long finish = System.nanoTime();
                                    long timeElapsed = finish - start;
                                    Log.d("timeElapsed", "" + (timeElapsed/1000000000) + " sec");
                                }
                            });
                            Log.d("executor_ende", "i: " + i);
                        }
                        for(int i = 0; i < anzThreads; i++){
                            executor[i].execute(runnable[i]);
                        }//*/

                    //executorService
                        /*ExecutorService executorService = Executors.newFixedThreadPool(anzThreads);
                        executorService.execute(new Runnable(){
                            @Override
                            public void run(){
                                //for(int count = 0; count < anzThreads; count++) {
                                    int count = 0;
                                    pointsPlot(type, factor, count, anzThreads);
                                    Log.d("ThreadPool_", "count: " + count + "; " + executorService);
                                //}
                            }
                        });///*/

                    //calculateThread
                        count = 0;
                        anzThreads = 1;
                        calculateThread = new Thread(() -> {
                            long start = System.nanoTime();
                            Log.d("calculateThread", "before plot(); " + type);
                            getThreadInterrupted(calculateThread.isInterrupted());
                            if(bytes[0].equals("rectangle")){
                                pointsPlotRectangle(type, factor, moveX, moveY, count, anzThreads);
                                Log.d("rectangle", moveX + ", " + moveY);
                            }else {
                                pointsPlot(type, factor, count, anzThreads);
                            }
                            long finish = System.nanoTime();
                            long timeElapsed = finish - start;
                            Log.d("timeElapsed", "" + (timeElapsed/1000000000) + " sec");
                            //getThreadInterrupted(calculateThread.isInterrupted());
                            Log.d("calculateThread", "after plot(); count: " + count);
                        });
                        calculateThread.start();
                        SecondFragment.started = true;//*/

                    //threads[]
                        /*threads = new Thread[anzThreads];
                        for(int i = 0; i < anzThreads; i++) {
                            count = i;
                            threads[i] = new Thread(() -> {
                                long start = System.nanoTime();
                                Log.d("calculateThread", "before plot(); " + type);
                                //for(int k = 0; k < anzThreads; k++){
                                //    getThreadInterrupted(threads[k].isInterrupted());
                                //}
                                if(bytes[0].equals("rectangle")){
                                    pointsPlotRectangle(type, factor, moveX, moveY, count, anzThreads);
                                    Log.d("rectangle", moveX + ", " + moveY);
                                }else {
                                    pointsPlot(type, factor, count, anzThreads);
                                }
                                //for(int k = 0; k < anzThreads; k++){
                                //    getThreadInterrupted(threads[k].isInterrupted());
                                //}
                                Log.d("calculateThread", "after plot(); count: " + count);
                                long finish = System.nanoTime();
                                long timeElapsed = finish - start;
                                Log.d("timeElapsed", "" + (timeElapsed/1000000000) + " sec");
                                listTime.add(timeElapsed/1000000000);
                            });
                            threads[i].start();
                            SecondFragment.started = true;
                            Log.d("for-loop", "" + count);
                        }
                        for(int k = 0; k < anzThreads; k++){
                            try {
                                threads[k].join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        long tempMax = (long) listTime.get(0);
                        for(int k = 1; k < listTime.size(); k++){
                            if(tempMax < (long) listTime.get(k)){
                                tempMax = (long) listTime.get(k);
                            }
                        }
                        listTime.clear();
                        Log.d("timeElapsed", "tempMax: " + tempMax + " sec");//*/
                    }
                }else{
                    Log.d("Server Error", "Connection lost");
                    SecondFragment.quit();
                    stopReader();
                }
            }
        } catch (IOException ioe) {
            Log.d("Server Error", "Connection lost");
            SecondFragment.quit();
            stopReader();
            ioe.printStackTrace();
        }
    }

    /**
     * stopReader()
     */
    public static void stopReader() {
        try {
            SecondFragment.receiveThread.interrupt();
            //Client.receiveThread.interrupt();
            /*if(threads != null) {
                for (Thread thread : threads) {
                    thread.interrupt();
                }
            }//*/
            is.close();
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        Log.d("Client stop", "stopReader()");
    }

    /**
     * run()
     */
    @Override
    public void run(){
        Log.d("main-thread", "Reader.run(): " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
        receiveMessage();
    }

    /**
     * wird in C++ benötigt, um die berechneten Punkte an den Server zu schicken
     * @param type String (unterschiedliche Typen, damit man in der Ausgabe besser unterscheiden kann was jetzt berechent wird)
     * @param x int (x-Koordiante)
     * @param y int (y-Koordinate)
     * @param itr int (Farbwert)
     */
    public void sendPlotPoints(String type, int x, int y, int itr){
        Client.sendMessage(type, "" + x + "/.../" + y + "/.../" + itr);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void getWidth(int width);
    public native void getHeight(int height);
    public native void getYourNumber(int yourNumber);
    public native void getAnzClients(int anzClients);

    public native void getThreadInterrupted(boolean interrupted);

    public native void pointsPlot(String type, double factor, int count, int anzThreads);
    public native void pointsPlotRectangle(String type, double factor, int moveX, int moveY, int count, int anzThreads);
}

