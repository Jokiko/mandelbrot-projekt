package com.example.studienprojekt_android;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

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

    private Thread checkThread;

    private Client client;

    //internet connection
    private String status = "";
    private String ip_1;
    private String ip_2;
    private String ip_3;
    private String ip_4;
    private String ip = "";
    private int port = -1;
    private boolean correctIP = false;
    private boolean correctPort = false;
    private boolean connected = false;

    //checking the internet connection
    private boolean internetConnected = false;
    private boolean wifiConnected = false;

    /************** Getter **************/
    public boolean getWifiConnected() {
        return wifiConnected;
    }
    public boolean getConnected(){
        return connected;
    }
    public Thread getCheckThread() {
        return checkThread;
    }
    public Client getClient(){
        return client;
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
        status = "";
        checkInternetConnection();
        if(internetConnected && wifiConnected){
            getIP();
            getPort();
        }else{
            incorrectIP_Port();
        }
    }

    /**
     * checkInternetConnection()
     */
    private void checkInternetConnection(){
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
                }else {
                    //not connected to the internet
                    status = "no internet connection";
                    internetConnected = false;
                    wifiConnected = false;

                }
            }
        }catch(Exception e){
            status = "no internet connection";
            internetConnected = false;
            wifiConnected = false;
            correctPort = false;
            correctIP = false;
            e.printStackTrace();
        }
    }

    /**
     * getIP()
     */
    private void getIP(){
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
    }

    /**
     * getPort()
     */
    private void getPort(){
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
    }

    /**
     * incorrectIP_Port()
     */
    private void incorrectIP_Port() {
        ip_1 = "-1";
        ip_2 = "-1";
        ip_3 = "-1";
        ip_4 = "-1";
        ip = ip_1 + "." + ip_2 + "." + ip_3 + "." + ip_4;
        port = -1;
        correctIP = false;
        correctPort = false;
    }

    /**
     * postConnect()
     */
    private void postConnect(){
        if(connected){
            FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
            NavHostFragment.findNavController(Objects.requireNonNull(fragmentManager.getPrimaryNavigationFragment())).navigate(R.id.action_firstFragment_to_secondFragment);
            firstFragment.setCurrentFragment("secondFragment");
            status = "connection to " + ip + ", " + port + " established";

            initializeCheck();
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
     * initializeCheck()
     */
    private void initializeCheck(){
        Check check = new Check(this, fragmentActivity, firstFragment, secondFragment, cpuInfo);
        checkThread = new Thread(check);
        checkThread.start();
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
        checkThread.interrupt();
        client.stopClient();
    }
}
