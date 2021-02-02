//Lars Klee

package com.example.studienprojekt_android;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.system.Os;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class FirstFragment extends Fragment {

    //start screen
    private EditText editDeviceName;
    private EditText editIP_1, editIP_2, editIP_3, editIP_4;
    private EditText editPort;
    Button btnConnect, btnDelete;

    static Fragment fragment;
    static String currentFragment;

    static boolean quit;
    static boolean badInternetConnection;

    //internet connection
    static String ip_1;
    static String ip_2;
    static String ip_3;
    static String ip_4;
    static boolean connected = false;
    static boolean correctIP = false;
    static boolean correctPort = false;
    static String ip = "";  //192.168.178.45
    static int port = -1;   //5000
    static String status = "";

    //checking the internet connection
    static boolean internetConnected = false;
    static boolean wifiConnected = false;
    static boolean mobileConnected = false;

    static String deviceName;

    /**
     * end()
     */
    public static void end(){
        connected = false;
        correctPort = false;
        correctIP = false;
        ip = "";
        ip_1 = "";
        ip_2 = "";
        ip_3 = "";
        ip_4 = "";
        port = -1;
        status = "";
        Client.stopClient();
    }

    /**
     * getFragment()
     * @param f Fragment
     */
    public static void getFragment(Fragment f){
        fragment = f;
    }//*/

    /**
     * onCreateView()
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        requireActivity().setTitle("Mandelbrot Main Screen");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.first_fragment, container, false);
    }

    /**
     * onViewCreated()
     * @param view @NonNull View
     * @param savedInstanceState Bundle
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // start screen
        editDeviceName = view.findViewById(R.id.editDeviceName);
        editIP_1 = view.findViewById(R.id.editIP_1);
        editIP_2 = view.findViewById(R.id.editIP_2);
        editIP_3 = view.findViewById(R.id.editIP_3);
        editIP_4 = view.findViewById(R.id.editIP_4);
        editPort = view.findViewById(R.id.editPort);
        btnConnect = view.findViewById(R.id.btnConnect);
        btnDelete = view.findViewById(R.id.btnDelete);

        btnConnect.setOnClickListener(view1 -> {
            Log.d("WifiCheckTask", "after btnConnect is clicked");
            Log.d("btnConnect", "after btnConnect is clicked");

            // gets the device brand, the device model, the android version as number and its specific code as a literal
            String reqString = Build.MANUFACTURER
                    + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                    + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
            if(editDeviceName.getText().toString().equals("")){
                deviceName = reqString + "/../";
            }else{
                deviceName = editDeviceName.getText().toString() + "/../(" + reqString + ")";
            }
            Log.d("name", deviceName);
            ConnectTask ct = new ConnectTask();
            ct.execute();
            Log.d("btnConnect", "after ct.execute");
        });

        btnDelete.setOnClickListener(view2 ->{
            String strIp1 = "192";      // ""
            String strIp2 = "168";      // ""
            String strIp3 = "178";      // ""
            String strIp4 = "45";       // ""
            String strPort = "5000";    // ""
            editDeviceName.setText("");
            editIP_1.setText(strIp1);
            editIP_2.setText(strIp2);
            editIP_3.setText(strIp3);
            editIP_4.setText(strIp4);
            editPort.setText(strPort);
            Toast toast = Toast.makeText(getActivity(), "Delete", Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    /**
     * class ConnectClass()
     * Background-Thread
     */
    @SuppressLint("StaticFieldLeak")
    class ConnectTask extends AsyncTask<Void, Void, Void> {
        /**
         * onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            Log.d("main-thread", "ct (onPreExecute()): " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
            status = "";
            try {
                //checks internet connection
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    ConnectivityManager connMgr = requireActivity().getSystemService(ConnectivityManager.class);
                    Network currentNetwork = Objects.requireNonNull(connMgr).getActiveNetwork();
                    Log.d("currentNetwork", "" + currentNetwork);

                    // The NetworkCapabilities object encapsulates information about the network transports and their capabilities
                    NetworkCapabilities caps = connMgr.getNetworkCapabilities(currentNetwork);

                    if (Objects.requireNonNull(caps).hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        //connected to the internet
                        internetConnected = true;
                        //connected to wifi
                        wifiConnected = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                        //connected to mobile data
                        mobileConnected = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                    }else {
                        //not connected to the internet
                        status = "no internet connection";
                        internetConnected = false;
                        wifiConnected = false;
                        mobileConnected = false;
                    }
                }
            }catch(Exception e){
                status = "no internet connection";
                internetConnected = false;
                wifiConnected = false;
                mobileConnected = false;
                correctPort = false;
                correctIP = false;
                e.printStackTrace();
            }
            if(internetConnected && wifiConnected){
                try {
                    ip_1 = editIP_1.getText().toString();
                    ip_2 = editIP_2.getText().toString();
                    ip_3 = editIP_3.getText().toString();
                    ip_4 = editIP_4.getText().toString();
                    int str1 = Integer.parseInt(ip_1);
                    int str2 = Integer.parseInt(ip_2);
                    int str3 = Integer.parseInt(ip_3);
                    int str4 = Integer.parseInt(ip_4);
                    correctIP = str1 >= 0 && str1 <= 255
                            && str2 >= 0 && str2 <= 255
                            && str3 >= 0 && str3 <= 255
                            && str4 >= 0 && str4 <= 255;
                } catch (NumberFormatException e) {
                    correctIP = false;
                    ip_1 = "-1";
                    ip_2 = "-1";
                    ip_3 = "-1";
                    ip_4 = "-1";
                    e.printStackTrace();
                }//*/
                if(!correctIP){
                    status = "incorrect ip (0.0.0.0 - 255.255.255.255)";
                }
                ip = ip_1 + "." + ip_2 + "." + ip_3 + "." + ip_4;

                try {
                    port = Integer.parseInt(editPort.getText().toString());
                    correctPort = (port >= 0 && port <= 65535);     //65535 max port number
                } catch (Exception e) {
                    correctPort = false;
                    port = -1;
                    e.printStackTrace();
                }
                if(!correctPort){
                    if(status.equals("")) {
                        status = "incorrect port (0 - 65535)";
                    }else{
                        status += "\nincorrect port (0 - 65535)";
                    }
                }
            }else{
                ip_1 = "-1";
                ip_2 = "-1";
                ip_3 = "-1";
                ip_4 = "-1";
                ip = ip_1 + "." + ip_2 + "." + ip_3 + "." + ip_4;
                port = -1;
                correctIP = false;
                correctPort = false;
            }
        }

        /**
         * doInBackground()
         * @param voids Void...
         * @return Void
         */
        @Override
        protected Void doInBackground(Void... voids) {
            //we create a TCPClient object
            if(correctIP && correctPort) {
                new Client(ip, port).run();//, getActivity());
            }else{
                connected = false;
            }
            return null;
        }

        /**
         * onPostExecute()
         * @param aVoid Void
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("main-thread", "ct (onPostExecute()): " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
            Log.d("correctIP", correctIP + "(" + ip +")");
            Log.d("correctPort", correctPort + "(" + port +")");
            Log.d("ServerConnected", "" + connected);
            Log.d("internetConnected", "" + internetConnected);
            Log.d("wifiConnected", "" + wifiConnected);
            Log.d("mobileConnected", "" + mobileConnected);

            if(connected){
                NavHostFragment.findNavController(FirstFragment.this).navigate(R.id.action_firstFragment_to_secondFragment);
                currentFragment = "secondFragment";
                Log.d("currentFragment", currentFragment);
                status = "connection to " + ip + ", " + port + " established";
                new WifiCheckTask().execute();
            }else{
                if(!wifiConnected){
                    status = "no internet connection via WiFi";
                }else {
                    status = "connection to ip: " + ip + ", port: " + port + " could not be established";
                }
            }
            Toast.makeText(getActivity(), status, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * class WifiCheckTask()
     * Background-Thread
     */
    @SuppressLint("StaticFieldLeak")
    class WifiCheckTask extends AsyncTask<Void, Void, Void>{
        String statusWifi = "";
        long received;
        long transmitted;

        /**
         * cpuUsage()
         * TODO cpuUsage()
         */
        private void cpuUsage(){
            Log.d("main-thread", "cpuUsage() begin: " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
            requireActivity().runOnUiThread(() -> {
                Log.d("main-thread", "cpuUsage() runOn: " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
                SecondFragment.txtCpuUsage.setText("");
                SecondFragment.txtCpuUsage.append("CPU Usage: " + CpuInfo.getCPUFrequencyCurrent() + "%");
            });
        }

        /**
         * memoryUsage()
         * TODO memoryUsage()
         */
        private String memoryUsage(){
            ActivityManager actManager = (ActivityManager) requireActivity().getSystemService(ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            Objects.requireNonNull(actManager).getMemoryInfo(memInfo);
            long availMemory = memInfo.availMem;
            double availInMB = Math.round((availMemory / 1024.0 / 1024.0) * 100) / 100.0;
            long totalMemory = memInfo.totalMem;
            double totalInMB = Math.round((totalMemory / 1024.0 / 1024.0) * 100) / 100.0;
            double usedInMB = Math.round((totalInMB-availInMB) * 100) / 100.0;

            /*requireActivity().runOnUiThread(() -> {
                SecondFragment.txtMemoryUsage.setText("");
                SecondFragment.txtMemoryUsage.append("Memory Usage: \n" + usedInMB + "/" + totalInMB + " MB");
            });//*/
            return usedInMB + "/" + totalInMB;
        }

        /**
         * networkUsage()
         */
        private double[] networkUsage(){
            double[] networkUsage = new double[2];
            Log.d("main-thread", "networkUsage() begin: " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
            int uid = android.os.Process.myUid();
            BufferedReader reader;
            long rxBytes = 0L; // number of bytes received by the given UID since device boot
            try {
                reader = new BufferedReader(new FileReader("/proc/uid_stat/" + uid + "/tcp_rcv"));
                rxBytes = Long.parseLong(reader.readLine());
                reader.close();
            }catch (FileNotFoundException e) {
                rxBytes = TrafficStats.getUidRxBytes(uid);
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("getUidRxBytes", "" + rxBytes);

            long txBytes = 0L; //number of bytes transmitted since device boot
            try {
                reader = new BufferedReader(new FileReader("/proc/uid_stat/" + uid + "/tcp_snd"));
                txBytes = Long.parseLong(reader.readLine());
                reader.close();
            }catch (FileNotFoundException e) {
                txBytes = TrafficStats.getUidTxBytes(uid);
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("getUidTxBytes", "" + txBytes);

            long finalRxBytes = rxBytes - received;
            long finalTxBytes = txBytes - transmitted;
            double send = Math.round((finalTxBytes/1024.0) * 100.0) / 100.0;
            double read = Math.round((finalRxBytes/1024.0) * 100.0) / 100.0;
            /*requireActivity().runOnUiThread(() -> {
                SecondFragment.txtNetworkUsageSend.setText("");
                SecondFragment.txtNetworkUsageRead.setText("");
                Log.d("main-thread", "networkUsage() runOn: " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
                SecondFragment.txtNetworkUsageSend.append("send: " + send + " KB/s");
                SecondFragment.txtNetworkUsageRead.append("read: " + read + " KB/s");
            });//*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                uid = Os.getuid();
                TrafficStats.setThreadStatsUid(uid);
                TrafficStats.clearThreadStatsUid();
                Log.d("getUid", "clear");
            }//*/
            received = rxBytes;
            transmitted = txBytes;

            networkUsage[0] = send;
            networkUsage[1] = read;

            return networkUsage;
        }

        /**
         * wifiStrength()
         * checks wifi strength
         * @return level int
         */
        private int wifiStrength(){
            // how dBm values for received Wireless Signal power (wifi.getRssi()):
            //      Excellent >-50 dBm
            //      Good -50 to -60 dBm
            //      Fair -60 to -70 dBm
            //      Weak < -70 dBm
            WifiManager wifiManager = (WifiManager) requireActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
            int numberOfLevels = 5;
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            Log.d("wifiManager", "level: " + level);
            Log.d("wifiManager", "dBm: " + wifiInfo.getRssi());
            Log.d("wifiManager", wifiInfo.getFrequency() + " MHz"); // MHz
            Log.d("wifiManager", wifiInfo.getLinkSpeed() + " Mbps"); // Mbps
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.d("wifiManager", wifiInfo.getRxLinkSpeedMbps() + " Mbps (Rx)"); // receive
                Log.d("wifiManager", wifiInfo.getTxLinkSpeedMbps() + " Mbps (Tx)"); // transmit
            }
            return level;
        }

        /**
         * check()
         * checks wifi-connection, cpuUsage(), memoryUsage() and networkUsage()
         */
        private void check(){
            if(connected) {
                try {
                    Thread.sleep(1000);
                    Log.d("main-thread", "wifiCheck(): " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
                    Log.d("WifiCheckTask", "after Thread sleep");
                    ConnectivityManager connMgr = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        Network currentNetwork = Objects.requireNonNull(connMgr).getActiveNetwork();
                        Log.d("currentNetwork", "" + currentNetwork);
                        Log.d("currentNetwork", "" + connMgr.isActiveNetworkMetered());

                        // The NetworkCapabilities object encapsulates information about the network transports and their capabilities
                        NetworkCapabilities caps = connMgr.getNetworkCapabilities(currentNetwork);

                        if (Objects.requireNonNull(caps).hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                            //connected to the internet
                            internetConnected = true;
                            //connected to wifi
                            wifiConnected = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                            //connected to mobile data
                            mobileConnected = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);

                        //CPU Usage
                            //cpuUsage();

                        //Memory Usage
                            //memoryUsage();

                        //Network Usage
                            double[] networkUsage = networkUsage();
                            requireActivity().runOnUiThread(() -> {
                                //CPU Usage
                                SecondFragment.txtCpuUsage.setText("");
                                SecondFragment.txtCpuUsage.append("CPU Usage: " + CpuInfo.getCPUFrequencyCurrent() + "%");
                                //Memory Usage
                                SecondFragment.txtMemoryUsage.setText("");
                                SecondFragment.txtMemoryUsage.append("Memory Usage: \n" + memoryUsage() + " MB");
                                //Network Usage
                                SecondFragment.txtNetworkUsageSend.setText("");
                                SecondFragment.txtNetworkUsageRead.setText("");
                                SecondFragment.txtNetworkUsageSend.append("send: " + networkUsage[0] + " KB/s");
                                SecondFragment.txtNetworkUsageRead.append("read: " + networkUsage[1] + " KB/s");
                            });

                        //check, wifiStrength
                            if(wifiStrength() >= 2) {
                                badInternetConnection = false;
                                check();
                            }else{
                                badInternetConnection = true;
                            }
                        }/*else {
                            //not connected to the internet
                            status = "no internet connection";
                            internetConnected = false;
                            wifiConnected = false;
                            mobileConnected = false;
                        }//*/
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * doInBackground()
         * @param voids Void...
         * @return Void
         */
        @Override
        protected Void doInBackground(Void... voids) {
            check();
            Log.d("WifiCheckTask", "after check()");
            return null;
        }

        /**
         * onPostExecute()
         * @param aVoid Void
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("main-thread", "wct (onPostExecute()): " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
            if(wifiConnected){
                if(quit){
                    statusWifi = "user has clicked quit";
                    quit = false;
                }else{
                    if(badInternetConnection){
                        statusWifi = "bad internet connection";
                    }else {
                        statusWifi = "server connection lost";
                    }
                }
            }else {
                statusWifi = "internet connection lost";
            }
            Log.d("WifiCheckTask", statusWifi);
            SecondFragment.quit();
            Reader.stopReader();
            Log.d("currentFragment", currentFragment);
            try {
                if(currentFragment.equals("secondFragment")) {
                    NavHostFragment.findNavController(fragment).navigate(R.id.action_secondFragment_to_firstFragment);
                    currentFragment = "firstFragment";
                    SecondFragment.viewCreated = false;
                }
            }catch(Exception e){
                Log.d("Error", "" + e.getClass());
                e.printStackTrace();
            }
            if(!statusWifi.equals("")) {
                Toast.makeText(getActivity(), statusWifi, Toast.LENGTH_SHORT).show();
            }
            Log.d("currentFragment", currentFragment);
        }
    }
}

