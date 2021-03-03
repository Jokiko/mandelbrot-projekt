package com.example.studienprojekt_android;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Objects;

public class Connect implements Runnable{
    private final FragmentActivity fragmentActivity;
    private final FirstFragment firstFragment;
    private final SecondFragment secondFragment;
    private final CpuInfo cpuInfo;
    private WifiCheck wifiCheck;
    private static Thread wifiCheckThread;

    private static Client client;

    private FragmentManager fragmentManager;

    //internet connection
    private String status = "";
    private String ip_1;
    private String ip_2;
    private String ip_3;
    private String ip_4;
    private String ip = "";  //192.168.178.45
    private int port = -1;   //5000
    private boolean correctIP = false;
    private boolean correctPort = false;
    private boolean connected = false;

    //checking the internet connection
    private boolean internetConnected = false;
    private boolean wifiConnected = false;
    private boolean mobileConnected = false;

    /************** Getter **************/
    public boolean getInternetConnected() {
        return internetConnected;
    }
    public boolean getWifiConnected() {
        return wifiConnected;
    }
    public boolean getMobileConnected() {
        return mobileConnected;
    }
    public Thread getWifiCheckThread() {
        return wifiCheckThread;
    }
    public WifiCheck getWifiCheck(){
        return wifiCheck;
    }
    public Client getClient() {
        return client;
    }
    public boolean getConnected(){
        return connected;
    }

    /************** Setter **************/
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    public void setInternetConnected(boolean internetConnected) {
        this.internetConnected = internetConnected;
    }
    public void setWifiConnected(boolean wifiConnected) {
        this.wifiConnected = wifiConnected;
    }
    public void setMobileConnected(boolean mobileConnected) {
        this.mobileConnected = mobileConnected;
    }

    /**
     * Constructor of {@code Connect}
     * @param fragmentActivity FragmentActivity
     * @param firstFragment FirstFragment
     * @param secondFragment SecondFragment
     * @param cpuInfo CpuInfo
     */
    public Connect(FragmentActivity fragmentActivity, FirstFragment firstFragment, SecondFragment secondFragment, CpuInfo cpuInfo){
        this.fragmentActivity = fragmentActivity;
        this.firstFragment = firstFragment;
        this.secondFragment = secondFragment;
        this.cpuInfo = cpuInfo;
        //Log.d("main-thread", "Connect (constructor): " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
    }

    /**
     * run()
     */
    @Override
    public void run() {
        preConnect();
        //we create a TCPClient object
        if(correctIP && correctPort) {
            client = new Client(this, ip, port);
            client.run();
        }else{
            connected = false;
        }
        postConnect();
    }

    /**
     * preConnect()
     */
    private void preConnect(){
        //Log.d("main-thread", "Connect (preConnect): " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
        status = "";
        try {
            //checks internet connection
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                ConnectivityManager connMgr = fragmentActivity.getSystemService(ConnectivityManager.class);
                Network currentNetwork = Objects.requireNonNull(connMgr).getActiveNetwork();

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
                ip_1 = firstFragment.getEditIP_1().getText().toString();
                ip_2 = firstFragment.getEditIP_2().getText().toString();
                ip_3 = firstFragment.getEditIP_3().getText().toString();
                ip_4 = firstFragment.getEditIP_4().getText().toString();
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
                port = Integer.parseInt(firstFragment.getEditPort().getText().toString());
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
     * postConnect()
     */
    private void postConnect(){
        //Log.d("main-thread", "Connect (postConnect): " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
        Log.d("Connect_", "connected: " + connected);
        if(connected){
            fragmentManager = fragmentActivity.getSupportFragmentManager();
            NavHostFragment.findNavController(Objects.requireNonNull(fragmentManager.getPrimaryNavigationFragment())).navigate(R.id.action_firstFragment_to_secondFragment);
            firstFragment.setCurrentFragment("secondFragment");
            status = "connection to " + ip + ", " + port + " established";

            wifiCheck = new WifiCheck(this, fragmentActivity, firstFragment, secondFragment, cpuInfo);
            wifiCheckThread = new Thread(wifiCheck);
            wifiCheckThread.start();
        }else{
            if(!wifiConnected){
                status = "no internet connection via WiFi";
            }else {
                if(status.equals("")) {
                    status = "connection to ip: " + ip + ", port: " + port + " could not be established";
                }
            }
        }
        fragmentActivity.runOnUiThread(() -> Toast.makeText(fragmentActivity, status, Toast.LENGTH_SHORT).show());
    }

    /**
     * quit()
     */
    public void quit(){
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
        wifiCheckThread.interrupt();
        client.stopClient();
    }
}
