package com.example.studienprojekt_android;

import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

public class CpuInfo {

    /**
     * http://www.java2s.com/Code/Android/Hardware/GetCPUFrequencyCurrent.htm
     * berechnet CPU-Usage
     */
    public static double getCPUFrequencyCurrent(){
        double sum = 0.0;
        for(int i = 0; i < getNumCores(); i++) {
            try {
                int currentFrequency = readSystemFileAsInt("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq");
                int minFrequency = readSystemFileAsInt("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_min_freq");
                int maxFrequency = readSystemFileAsInt("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq");
                double percentSingle = Math.round((currentFrequency-minFrequency)/(maxFrequency*1.0) * 1000) / 10.0;
                Log.d("CpuInfo", i + "(currentFrequency): " + currentFrequency);
                Log.d("CpuInfo", i + "(minFrequency):" + minFrequency);
                Log.d("CpuInfo", i + "(maxFrequency): " + maxFrequency);
                Log.d("CpuInfo", i + "(percent): " + percentSingle);
                sum += percentSingle;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        double sumPercent = Math.round(sum/(getNumCores()*1.0) * 100) / 100.0;
        Log.d("CpuInfo", "sum: " + sumPercent);
        return sumPercent;
    }
    private static int readSystemFileAsInt(final String pSystemFile){
        InputStream in;
        try {
            final Process process = new ProcessBuilder(new String[] { "/system/bin/cat", pSystemFile }).start();

            in = process.getInputStream();
            final String content = readFully(in);
            return Integer.parseInt(content);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    public static String readFully(final InputStream pInputStream){
        final StringBuilder sb = new StringBuilder();
        final Scanner sc = new Scanner(pInputStream);
        while(sc.hasNextLine()) {
            sb.append(sc.nextLine());
        }
        return sb.toString();
    }



    /**
     * getNumCores()
     * Ruft die Anzahl der in diesem Gerät verfügbaren Kerne auf allen Prozessoren ab.
     * Benötigt: Fähigkeit, das Dateisystem unter "/sys/devices/system/ cpu" zu durchsuchen
     * @return Die Anzahl der Kerne oder 1, wenn das Ergebnis nicht erhalten werden konnte
     */
    public static int getNumCores() {
        // Private Klasse, um nur CPU-Geräte in der Verzeichnisliste anzuzeigen
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept (File pathname) {
                // Überprüfe, ob der Dateiname "cpu" ist, gefolgt von einer einstelligen Zahl
                return Pattern.matches("cpu[0-9]+", pathname.getName());
            }
        }
        try{
            // Verzeichnis mit CPU-Informationen abrufen
            File dir = new File ("/sys/devices/system/cpu/");
            // Filtern, um nur die Geräte aufzulisten, die uns interessieren
            File[] files = dir.listFiles(new CpuFilter());
            // Anzahl der Kerne zurückgeben (virtuelle CPU-Geräte)
            //cpu = files.length;
            return files.length;
        } catch (Exception e) {
            // Ausnahme drucken
            Log.d("CPU-Count:", "Failed");
            e.printStackTrace();
            // Standardmäßig wird 1 Core zurückgegeben
            //cpu = 1;
            return 1;
        }
    }
}
