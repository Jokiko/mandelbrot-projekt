package com.example.studienprojekt_android;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FirstFragment extends Fragment {
    private final CpuInfo cpuInfo = new CpuInfo();

    private SecondFragment secondFragment;

    private static Connect connect;
    private static Thread connectThread;

    //start screen
    private EditText editDeviceName;
    private EditText editIP_1;
    private EditText editIP_2;
    private EditText editIP_3;
    private EditText editIP_4;
    private EditText editPort;
    private Button btnConnect, btnDelete;

    private String currentFragment;

    private boolean quit;

    private static String deviceName;

    /************** Getter **************/
    public boolean getQuit(){
        return quit;
    }
    public String getDeviceName() {
        return deviceName;
    }
    public String getCurrentFragment() {
        return currentFragment;
    }
    public EditText getEditIP_1() {
        return editIP_1;
    }
    public EditText getEditIP_2() {
        return editIP_2;
    }
    public EditText getEditIP_3() {
        return editIP_3;
    }
    public EditText getEditIP_4() {
        return editIP_4;
    }
    public EditText getEditPort(){
        return editPort;
    }
    public Client getClient(){
        return connect.getClient();
    }

    /************** Setter **************/
    public void setQuit(boolean quit) {
        this.quit = quit;
    }
    public void setCurrentFragment(String currentFragment) {
        this.currentFragment = currentFragment;
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

        secondFragment = new SecondFragment();

        initializeView(view);

        initializeButton();
    }

    /**
     * initializeView()
     * @param view View
     */
    private void initializeView(View view){
        // start screen
        editDeviceName = view.findViewById(R.id.editDeviceName);
        editIP_1 = view.findViewById(R.id.editIP_1);
        editIP_2 = view.findViewById(R.id.editIP_2);
        editIP_3 = view.findViewById(R.id.editIP_3);
        editIP_4 = view.findViewById(R.id.editIP_4);
        editPort = view.findViewById(R.id.editPort);
        btnConnect = view.findViewById(R.id.btnConnect);
        btnDelete = view.findViewById(R.id.btnDelete);
    }

    /**
     * initializeButton()
     */
    private void initializeButton(){
        connectButton();
        deleteButton();
    }

    /**
     * connectButton()
     */
    private void connectButton(){
        btnConnect.setOnClickListener(v -> {
            // gets the device brand, the device model, the android version as number and its specific code as a literal
            String reqString = Build.MANUFACTURER
                    + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                    + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
            if(editDeviceName.getText().toString().equals("")){
                deviceName = reqString;
            }else{
                deviceName = editDeviceName.getText().toString() + " (" + reqString + ")";
            }
            initializeConnect();
        });
    }

    /**
     * initializeConnect()
     */
    private void initializeConnect(){
        connect = new Connect(getActivity(), this, secondFragment, cpuInfo);
        connectThread = new Thread(connect);
        connectThread.start();
    }

    /**
     * deleteButton()
     */
    private void deleteButton(){
        btnDelete.setOnClickListener(v ->{
            String strPort = "5000";
            editDeviceName.setText("");
            editIP_1.setText("");
            editIP_2.setText("");
            editIP_3.setText("");
            editIP_4.setText("");
            editPort.setText(strPort);
            Toast toast = Toast.makeText(getActivity(), "Delete", Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    /**
     * quit()
     */
    public void quit(){
        connect.quit();
        connectThread.interrupt();
    }
}

