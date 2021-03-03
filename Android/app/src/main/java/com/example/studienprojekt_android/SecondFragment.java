//Lars Klee

package com.example.studienprojekt_android;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class SecondFragment extends Fragment {
    private final Object ob = new Object();
    private final FirstFragment firstFragment = new FirstFragment();

    private CpuInfo cpuInfo;

    //"second" screen
    private Button btnStart, btnPause, btnQuit;
    private TextView txtNetworkUsage, txtAnzThreads, txtAnzClients, txtStatus, txtName;
    //private TextView txtCpuUsage, txtMemoryUsage, txtNetworkUsageSend, txtNetworkUsageRead;
    @SuppressLint("StaticFieldLeak")
    static TextView txtCpuUsage, txtMemoryUsage, txtNetworkUsageSend, txtNetworkUsageRead;

    private static Connect connect;
    private static Thread connectThread;

    private Reader reader;

    private static Thread receiveThread;
    private static Thread[] receiveThreadArray = new Thread[2];

    private int anzThreads;

    private static boolean started;
    private boolean viewCreated = false;
    private boolean stop;

    private static String status = "";

    /************** Getter **************/
    public boolean getStop(){
        return stop;
    }
    public boolean getViewCreated() {
        return viewCreated;
    }
    public boolean getStarted() {
        return started;
    }
    public String getStatus() {
        return status;
    }
    public TextView getTxtCpuUsage() {
        return txtCpuUsage;
    }
    public TextView getTxtMemoryUsage() {
        return txtMemoryUsage;
    }
    public TextView getTxtNetworkUsageSend() {
        return txtNetworkUsageSend;
    }
    public TextView getTxtNetworkUsageRead() {
        return txtNetworkUsageRead;
    }
    public static Thread getReceiveThread() {
        return receiveThread;
    }
    public Reader getReader(){
        return reader;
    }
    public Object getObject(){
        return ob;
    }

    /************** Setter **************/
    public void setViewCreated(boolean viewCreated) {
        this.viewCreated = viewCreated;
    }
    public void setStatus(String status){
        SecondFragment.status = status;
    }

    //Used to load the 'native-lib library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * onCreateView()
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        requireActivity().setTitle("Mandelbrot Second Fragment");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.second_fragment, container, false);
    }

    /**
     * onViewCreated()
     * @param view @NonNull View
     * @param savedInstanceState Bundle
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cpuInfo = firstFragment.getCpuInfo();

        initializeView(view);

        initializeReader();

        initializeButton();

        viewCreated = true;
    }

    /**
     * initializeView()
     * @param view View
     */
    private void initializeView(View view){
        btnStart = view.findViewById(R.id.btnStart);
        btnPause = view.findViewById(R.id.btnPause);
        btnQuit = view.findViewById(R.id.btnQuit);
        txtStatus = view.findViewById(R.id.txtStatus);
        txtName = view.findViewById(R.id.txtName);
        txtCpuUsage = view.findViewById(R.id.txtCpuUsage);
        txtMemoryUsage = view.findViewById(R.id.txtMemoryUsage);
        txtNetworkUsage = view.findViewById(R.id.txtNetworkUsage);
        txtNetworkUsageSend = view.findViewById(R.id.txtNetworkUsageSend);
        txtNetworkUsageRead = view.findViewById(R.id.txtNetworkUsageRead);
        txtAnzThreads = view.findViewById(R.id.txtAnzThreads);
        txtAnzClients = view.findViewById(R.id.txtAnzClients);

        setAnzThreads();
        setDeviceName();
        txtStatus.setText(R.string.no_calculation_running);
    }

    /**
     * setDeviceName()
     */
    private void setDeviceName(){
        String deviceName;
        String[] bytes = firstFragment.getDeviceName().split("/../");
        if(bytes.length == 1){
            deviceName = bytes[0];
        }else {
            deviceName = bytes[0] + " " + bytes[1];
        }
        txtName.setText(R.string.device_name_str);
        txtName.append("\n" + deviceName);
    }

    /**
     * setAnzThreads()
     */
    private void setAnzThreads(){
        if(cpuInfo.getNumCores() <= 4){
            anzThreads = 1;
        }else{
            anzThreads = (cpuInfo.getNumCores() - 4);
        }
        txtAnzThreads.append(" " + anzThreads);
    }

    /**
     * initializeReader()
     */
    private void initializeReader(){
        reader = new Reader(firstFragment.getConnect(), Client.getSocket(), getActivity(), txtAnzClients, firstFragment, SecondFragment.this, cpuInfo);
        receiveThread = new Thread(reader);
        receiveThread.setName("receiveThread_" + receiveThread.getId());
        receiveThread.start();
        setInterrupt(receiveThread.isInterrupted());
    }

    /**
     * initializeButton()
     */
    private void initializeButton(){
        startButton();
        pauseButton();
        quitButton();
    }

    /**
     * startButton()
     */
    private void startButton(){
        btnStart.setOnClickListener(v -> {
            stop = false;
            setStop(stop);
            if(btnStart.getText().equals("Start")) {
                if(!started) {
                    Client.sendMessage("task");
                    txtStatus.setText(R.string.calculation_started);
                    started = true;
                    btnStart.setText(R.string.running);
                }
            }else{
                if(btnStart.getText().equals("Running...")){
                    txtStatus.setText(R.string.calculation_already_running);
                }else {
                    if (!started) {
                        btnPause.setText(R.string.pause);
                        Client.sendMessage("task");
                        txtStatus.setText(R.string.calculation_resumed);
                        started = true;
                        btnStart.setText(R.string.running);
                    } else {
                        txtStatus.setText(R.string.calculation_already_resumed);
                    }
                }
            }
        });
    }

    /**
     * pauseButton()
     */
    private void pauseButton(){
        btnPause.setOnClickListener(v -> {
            //stop = true;
            //setStop(stop);
            if(started) {
                btnPause.setText(R.string.paused);
                txtStatus.setText(R.string.calculation_paused);
                btnStart.setText(R.string.resume);
                started = false;
            }else{
                txtStatus.setText(R.string.no_calculation_running);
            }
        });
    }

    /**
     * quitButton()
     */
    private void quitButton(){
        btnQuit.setOnClickListener(v -> {
            if(started) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Warning");
                alertDialog.setMessage("You are about to quit the calculation.\nContinue?");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "YES",
                        (dialog, which) -> {
                            Client.sendMessage("close", firstFragment.getDeviceName());
                            status = "user has clicked quit";
                            quit();
                            dialog.dismiss();
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
            }else{
                Client.sendMessage("close", firstFragment.getDeviceName());
                status = "user has clicked quit";
                quit();
            }
            setStop(stop);
            setInterrupt(receiveThread.isInterrupted());
        });
    }

    /**
     * quit()
     */
    public void quit(){
        firstFragment.setCurrentFragment("firstFragment");
        firstFragment.setQuit(true);
        viewCreated = false;
        stop = true;
        receiveThread.interrupt();
        started = false;
        firstFragment.quit();
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void setStop(boolean stop);

    public native void setInterrupt(boolean interrupt);
}

