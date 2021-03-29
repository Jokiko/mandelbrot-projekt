package com.example.studienprojekt_android;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SecondFragment extends Fragment {
    private final FirstFragment firstFragment = new FirstFragment();

    private Client client;

    //"second" screen
    private Button btnStart, btnPause, btnQuit;
    private TextView txtStatus, txtName;
    @SuppressLint("StaticFieldLeak")
    static TextView txtCpuUsage, txtMemoryUsage, txtNetworkUsageSend, txtNetworkUsageRead;

    private static Thread readerThread;
    private static Reader reader;

    private static boolean started;
    private boolean stop;

    private static String status = "";

    private String name = "";

    /************** Getter **************/
    public boolean getStop(){
        return stop;
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
    public Thread getReaderThread() {
        return readerThread;
    }
    public Reader getReader(){
        return reader;
    }
    public String getName(){
        return name;
    }

    /************** Setter **************/
    public void setStatus(String status){
        SecondFragment.status = status;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        client = firstFragment.getClient();

        initializeView(view);

        initializeReader();

        initializeButton();
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
        txtNetworkUsageSend = view.findViewById(R.id.txtNetworkUsageSend);
        txtNetworkUsageRead = view.findViewById(R.id.txtNetworkUsageRead);

        setDeviceName();
        txtStatus.setText(R.string.no_calculation_running);
    }

    /**
     * setDeviceName()
     */
    private void setDeviceName(){
        String deviceName;
        StringBuilder sb = new StringBuilder();
        deviceName = firstFragment.getDeviceName();
        String [] nameArray = deviceName.split(" ");
        for(String string : nameArray){
            sb.append(string).append("_");
        }
        name = sb.toString();
        txtName.setText(R.string.device_name_str);
        txtName.append("\n" + deviceName);
    }

    /**
     * initializeReader()
     */
    private void initializeReader(){
        reader = new Reader(this, firstFragment.getClient());
        readerThread = new Thread(reader);
        readerThread.setName("readerThread_" + readerThread.getId());
        readerThread.start();
        setInterrupt(readerThread.isInterrupted());
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
            setStop(false);
            if(btnStart.getText().equals("Start")) {
                if(!started) {
                    client.sendMessage("task");
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
                        client.sendMessage("task");
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
                alertDialog.setTitle(Html.fromHtml("<font color='#FF7F27'>Warning</font>"));
                alertDialog.setMessage("You are about to quit the calculation.\nContinue?");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "YES",
                        (dialog, which) -> {
                            status = "user has clicked quit";
                            quit();
                            dialog.dismiss();
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
            }else{
                status = "user has clicked quit";
                quit();
            }
        });
    }

    /**
     * quit()
     */
    public void quit(){
        setStop(stop);
        setInterrupt(readerThread.isInterrupted());
        firstFragment.setCurrentFragment("firstFragment");
        firstFragment.setQuit(true);
        stop = true;
        readerThread.interrupt();
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

