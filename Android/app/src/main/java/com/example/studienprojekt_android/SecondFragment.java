//Lars Klee

package com.example.studienprojekt_android;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class SecondFragment extends Fragment {

    //"second" screen
    @SuppressLint("StaticFieldLeak")
    static TextView txtCpuUsage, txtMemoryUsage, txtNetworkUsageSend, txtNetworkUsageRead;

    Button btnStart, btnPause, btnQuit;
    TextView txtNetworkUsage, txtAnzThreads, txtAnzClients, txtStatus, txtName;

    static boolean started;

    Reader mReader;
    static Thread receiveThread;

    int anzThreads;

    //Fragment f;

    static boolean viewCreated = false;

    //Used to load the 'native-lib library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * quit()
     */
    public static void quit(){
        started = false;
        FirstFragment.end();
        endCalculation();
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
        //f = SecondFragment.this;

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

        FirstFragment.getFragment(SecondFragment.this);

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

        if(!viewCreated) {
            txtAnzClients.setText(getString(R.string.number_clients));
            txtAnzClients.append(" " + Reader.anzClients);
        }

        mReader = new Reader(Client.socket, getActivity(), txtAnzClients);
        receiveThread = new Thread(mReader);
        receiveThread.start();
        Log.d("Reader","Reader.run()");

        String deviceName;
        String[] bytes = FirstFragment.deviceName.split("/../");
        if(bytes.length == 1){
            deviceName = bytes[0];
        }else {
            deviceName = bytes[0] + " " + bytes[1];
        }
        txtName.setText(R.string.device_name_str);
        txtName.append("\n" + deviceName);

        txtStatus.setText(R.string.no_calculation_running);

        Log.d("getNumCores", "" + CpuInfo.getNumCores());
        if(CpuInfo.getNumCores() <= 4){
            anzThreads = 1;
        }else{
            anzThreads = (CpuInfo.getNumCores() - 4);
        }
        txtAnzThreads.append(" " + anzThreads);
        Log.d("AnzThreads", "" + anzThreads);

        btnStart.setOnClickListener(v -> {
            if(btnStart.getText().equals("Start")) {
                if(!started) {
                    Client.sendMessage("start", "");
                    startCalculation();
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
                        Client.sendMessage("resume", "");
                        txtStatus.setText(R.string.calculation_resumed);
                        resumeCalculation();
                        started = true;
                        btnStart.setText(R.string.running);
                    } else {
                        txtStatus.setText(R.string.calculation_already_resumed);
                    }
                }
            }
        });

        btnPause.setOnClickListener(v -> {
            Log.d("btnPause" , "started: " + started);
            if(started) {
                btnPause.setText(R.string.paused);
                Client.sendMessage("pause", "");
                pauseCalculation();
                txtStatus.setText(R.string.calculation_paused);
                btnStart.setText(R.string.resume);
                started = false;
            }else{
                txtStatus.setText(R.string.no_calculation_running);
            }
        });

        btnQuit.setOnClickListener(view1 -> {
            if(started) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Warning");
                alertDialog.setMessage("You are about to quit the calculation.\nContinue?");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "YES",
                        (dialog, which) -> {
                            Client.sendMessage("close", FirstFragment.deviceName);
                            quit();
                            dialog.dismiss();
                            NavHostFragment.findNavController(SecondFragment.this)
                                    .navigate(R.id.action_secondFragment_to_firstFragment);
                            FirstFragment.currentFragment = "firstFragment";
                            FirstFragment.quit = true;
                            viewCreated = false;
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
            }else{
                Client.sendMessage("close", FirstFragment.deviceName);
                FirstFragment.end();
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_secondFragment_to_firstFragment);
                FirstFragment.currentFragment = "firstFragment";
                FirstFragment.quit = true;
                viewCreated = false;
            }
        });

        viewCreated = true;
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void startCalculation();
    public static native void pauseCalculation();
    public native void resumeCalculation();
    public static native void endCalculation();
}

