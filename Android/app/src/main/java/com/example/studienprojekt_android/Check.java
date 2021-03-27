package com.example.studienprojekt_android;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class Check implements Runnable{
    private final Connect connect;
    private final FragmentActivity fragmentActivity;
    private final CpuInfo cpuInfo;
    private final FirstFragment firstFragment;
    private final SecondFragment secondFragment;

    private FragmentManager fragmentManager;

    private long received;
    private long transmitted;

    private boolean badInternetConnection;

    /**
     * Constructor of {@code Check}
     * @param connect Connect
     * @param fragmentActivity FragmentActivity
     * @param firstFragment FirstFragment
     * @param secondFragment SecondFragment
     * @param cpuInfo CpuInfo
     */
    public Check(Connect connect, FragmentActivity fragmentActivity, FirstFragment firstFragment, SecondFragment secondFragment, CpuInfo cpuInfo){
        this.connect = connect;
        this.fragmentActivity = fragmentActivity;
        this.firstFragment = firstFragment;
        this.secondFragment = secondFragment;
        this.cpuInfo = cpuInfo;
    }

    /**
     * run()
     */
    @Override
    public void run() {
        check();
        afterCheck();
    }

    /**
     * memoryUsage()
     * checks memoryUsage
     * @return usedInMB + "/" + totalInMB as String
     */
    private String memoryUsage(){
        ActivityManager activityManager = (ActivityManager) fragmentActivity.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        Objects.requireNonNull(activityManager).getMemoryInfo(memInfo);
        double availInMB = Math.round((memInfo.availMem / 1024.0 / 1024.0) * 100) / 100.0;
        double totalInMB = Math.round((memInfo.totalMem / 1024.0 / 1024.0) * 100) / 100.0;
        double usedInMB = Math.round((totalInMB-availInMB) * 100) / 100.0;

        return usedInMB + "/" + totalInMB;
    }

    /**
     * networkUsage()
     * checks networkUsage
     * @return send, read as double[]
     */
    private double[] networkUsage(){
        double[] networkUsage = new double[2];
        int uid = android.os.Process.myUid();
        BufferedReader bufferedReader;

        long rxBytes = 0L; // number of bytes received by the given UID since device boot
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/uid_stat/" + uid + "/tcp_rcv"));
            rxBytes = Long.parseLong(bufferedReader.readLine());
            bufferedReader.close();
        }catch (FileNotFoundException e) {
            rxBytes = TrafficStats.getUidRxBytes(uid);
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        long txBytes = 0L; //number of bytes transmitted since device boot
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/uid_stat/" + uid + "/tcp_snd"));
            txBytes = Long.parseLong(bufferedReader.readLine());
            bufferedReader.close();
        }catch (FileNotFoundException e) {
            txBytes = TrafficStats.getUidTxBytes(uid);
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        // convert into kb
        double send = Math.round(((txBytes - transmitted)/1024.0) * 100.0) / 100.0;
        double read = Math.round(((rxBytes - received)/1024.0) * 100.0) / 100.0;

        received = rxBytes;
        transmitted = txBytes;

        networkUsage[0] = send;
        networkUsage[1] = read;

        return networkUsage;
    }

    /**
     * wifiStrength()
     * checks wifi strength
     * @return level as int
     */
    private int wifiStrength(){
        // how dBm values for received Wireless Signal power (wifi.getRssi()):
        //      Excellent >-50 dBm
        //      Good -50 to -60 dBm
        //      Fair -60 to -70 dBm
        //      Weak < -70 dBm
        WifiManager wifiManager = (WifiManager) fragmentActivity.getApplicationContext().getSystemService(WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels); // level
    }

    /**
     * check()
     * checks wifi-connection and calls cpuUsage(), memoryUsage(), networkUsage() and wifiStrength()
     */
    private void check(){
        if(connect.getConnected()) {
            try {
                Thread.sleep(1000);
                ConnectivityManager connMgr = (ConnectivityManager) fragmentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    Network currentNetwork = Objects.requireNonNull(connMgr).getActiveNetwork();

                    // The NetworkCapabilities object encapsulates information about the network transports and their capabilities
                    NetworkCapabilities caps = connMgr.getNetworkCapabilities(currentNetwork);

                    if (Objects.requireNonNull(caps).hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        //connected to the internet
                        connect.setInternetConnected(true);
                        //connected to wifi
                        connect.setWifiConnected(caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));

                        //Network Usage
                        double[] networkUsage = networkUsage();
                        fragmentActivity.runOnUiThread(() -> {
                            //CPU Usage
                            secondFragment.getTxtCpuUsage().setText("");
                            secondFragment.getTxtCpuUsage().append("CPU Usage: " + cpuInfo.getCPUFrequencyCurrent() + "%");
                            //Memory Usage
                            secondFragment.getTxtMemoryUsage().setText("");
                            secondFragment.getTxtMemoryUsage().append("Memory Usage: \n" + memoryUsage() + " MB");
                            //Network Usage
                            secondFragment.getTxtNetworkUsageSend().setText("");
                            secondFragment.getTxtNetworkUsageRead().setText("");
                            secondFragment.getTxtNetworkUsageSend().append("send: " + networkUsage[0] + " KB/s");
                            secondFragment.getTxtNetworkUsageRead().append("read: " + networkUsage[1] + " KB/s");
                        });

                        //check, wifiStrength
                        if(wifiStrength() >= 2) {
                            badInternetConnection = false;
                            if(!connect.getCheckThread().isInterrupted()) {
                                check();
                            }
                        }else{
                            badInternetConnection = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * afterCheck()
     */
    private void afterCheck() {
        quit();
        changeView();
        createToast(setStatus());
    }

    /**
     * quit()
     */
    private void quit(){
        secondFragment.quit();
        secondFragment.getReader().quit();
    }

    /**
     * changeView()
     */
    private void changeView(){
        try {
            if(firstFragment.getCurrentFragment().equals("secondFragment")) {
                fragmentManager = fragmentActivity.getSupportFragmentManager();
                if(!firstFragment.getQuit()) {
                    fragmentActivity.runOnUiThread(() ->
                            NavHostFragment.findNavController(Objects.requireNonNull(fragmentManager.getPrimaryNavigationFragment())).navigate(R.id.action_secondFragment_to_firstFragment)
                    );
                }
                firstFragment.setCurrentFragment("firstFragment");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * setStatus()
     * @return statusWifi as String
     */
    private String setStatus() {
        String statusWifi;
        if(connect.getWifiConnected()){
            if(badInternetConnection){
                statusWifi = "bad internet connection";
            }else {
                statusWifi = "server connection lost";
            }
        }else {
            statusWifi = "internet connection lost";
        }
        if(!secondFragment.getStatus().equals("")){
            statusWifi = secondFragment.getStatus();
            secondFragment.setStatus("");
        }
        return statusWifi;
    }

    /**
     * createToast()
     * @param statusWifi String
     */
    private void createToast(String statusWifi){
        fragmentActivity.runOnUiThread(() ->
                Toast.makeText(fragmentActivity, statusWifi, Toast.LENGTH_SHORT).show()
        );
    }
}
