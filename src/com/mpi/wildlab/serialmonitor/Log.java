package com.mpi.wildlab.serialmonitor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Log {
    public static BufferedWriter writer = null;
    public static final String DATE_FORMAT_NOW = "yyyyMMdd_HHmmss";
    public static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }
    public static void init(String logFileName) {
        String logFileNameComplete = now() + "_" + logFileName;
        try {
            writer = new BufferedWriter(new FileWriter(logFileNameComplete, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void d(String a) {
        if(writer != null) {
            try {
                writer.write(a);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void dNoLog(String a, String b) {
        System.out.println(a + ": " + b);
    }
    public static void dNoLog(String b) {
        System.out.println(b);
    }
    public static void deinit() {
        if(writer != null) {
            try {
                writer.close();
                writer = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
