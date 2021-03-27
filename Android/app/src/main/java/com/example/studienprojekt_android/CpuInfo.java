package com.example.studienprojekt_android;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

public class CpuInfo {

    /**
     * calculates CPU-Usage
     * http://www.java2s.com/Code/Android/Hardware/GetCPUFrequencyCurrent.htm
     * http://www.java2s.com/Code/Android/Hardware/GetCPUFrequencyMin.htm
     * http://www.java2s.com/Code/Android/Hardware/GetCPUFrequencyMax.htm
     */
    public double getCPUFrequencyCurrent(){
        double sum = 0.0;
        for(int i = 0; i < getNumCores(); i++) {
            try {
                int currentFrequency = readSystemFileAsInt("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq");
                int minFrequency = readSystemFileAsInt("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_min_freq");
                int maxFrequency = readSystemFileAsInt("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq");
                double percentSingle = Math.round((currentFrequency-minFrequency)/(maxFrequency*1.0) * 1000) / 10.0;
                sum += percentSingle;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return Math.round(sum/(getNumCores()*1.0) * 100) / 100.0; // sumPercent
    }
    private int readSystemFileAsInt(final String pSystemFile){
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
    private String readFully(final InputStream pInputStream){
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
    private int getNumCores() {
        // Private Klasse, um nur CPU-Geräte in der Verzeichnisliste anzuzeigen
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept (File pathname) {
                // Überprüfe, ob der Dateiname "cpu" ist, gefolgt von einer Zahl
                return Pattern.matches("cpu[0-9]+", pathname.getName());
            }
        }
        try{
            // Verzeichnis mit CPU-Informationen abrufen
            File dir = new File ("/sys/devices/system/cpu/");
            // Filtern, um nur die Geräte aufzulisten, die uns interessieren
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
            // Standardmäßig wird 1 Core zurückgegeben
            return 1;
        }
    }
}
