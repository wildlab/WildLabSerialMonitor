package com.mpi.wildlab.serialmonitor;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.*;
import java.util.*;

public class Main {
    public static String SOFTWARE_VERSION ="1.0";
    static SerialPort activePort;
    static SerialPort[] ports = SerialPort.getCommPorts();

    public static int modeSelect() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Log.dNoLog("--- SELECT YOUR TAG:");
        Log.dNoLog("\t[ 0 ] DOWNLOAD DATA FROM FLEATAG (PLEASE CONNECT PROGRAMMINGBOARD VIA USB FIRST)");
        Log.dNoLog("\t[ 1 ] TINYFOX DEBUGGING");
        //Log.dNoLog("\t[ 1 ] WildFi tag serial monitor");
        //Log.dNoLog("\t[ 1 ] WildFi gateway serial monitor");
        Log.dNoLog("\t[ 7 ] GENERIC SERIAL MONITOR, 9600 BAUD");
        Log.dNoLog("\t[ 8 ] GENERIC SERIAL MONITOR, 115200 BAUD");
        Log.dNoLog("\t[ 9 ] EXIT");

        Log.dNoLog("--> ENTER SELECTION AND PRESS ENTER: ");
        int i = 0;
        try {
            i = Integer.parseInt(br.readLine());
        } catch(Exception e) {
            Log.dNoLog("--- INVALID FORMAT");
            return -1;
        }
        return i;
    }

    public static int showAllPorts() {
        int i = 0;
        Log.dNoLog("--- AVAILABLE COM PORTS:");
        ports = SerialPort.getCommPorts();
        for(SerialPort port : ports) {
            Log.dNoLog("\t" + i + ": " + port.getDescriptivePortName() + " / " + port.getPortDescription());
            i++;
        }
        return i;
    }

    public static int showAndSelectPort() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int i = 0;
        int selected = 0;
        Log.dNoLog("--- AVAILABLE COM PORTS:");
        ports = SerialPort.getCommPorts();
        for(SerialPort port : ports) {
            Log.dNoLog("\t[ " + i + " ] " + port.getDescriptivePortName() + " / " + port.getPortDescription());
            i++;
        }

        if(i == 0) {
            Log.dNoLog("--- NO COM PORTS FOUND, DID YOU CONNECT YOUR DEVICE?");
            return -1;
        }

        Log.dNoLog("--> ENTER SELECTION AND PRESS ENTER: ");
        try {
            selected = Integer.parseInt(br.readLine());
        } catch(Exception e) {
            Log.dNoLog("--- INVALID FORMAT");
            return -1;
        }

        if(selected >= i) {
            Log.dNoLog("--- INVALID INPUT");
            return -1;
        }
        if(selected < 0) {
            Log.dNoLog("--- INVALID INPUT");
            return -1;
        }

        return selected;
    }

    public static void deleteEmptyTxtFiles() {
        File dir = new File(".");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if(child.getName().endsWith(".txt")) {
                    if(child.length() == 0) {
                        child.delete();
                    }
                }
            }
        }
    }

    public static boolean isAllowedChar(char codePoint) {
        return (codePoint >= 65 && codePoint <= 90) ||
                (codePoint >= 97 && codePoint <= 122) ||
                (codePoint >= 48 && codePoint <= 57) ||
                (codePoint == '\n') ||
                (codePoint == ' ') ||
                (codePoint == '*') ||
                (codePoint == '#') ||
                (codePoint == '+') ||
                (codePoint == ':') ||
                (codePoint == '_') ||
                (codePoint == '.') ||
                (codePoint == '(') ||
                (codePoint == ')') ||
                (codePoint == ',') ||
                (codePoint == '-') ||
                (codePoint == '!') ||
                (codePoint == '/') ||
                (codePoint == '[') ||
                (codePoint == ']') ||
                (codePoint == '&') ||
                (codePoint == '=') ||
                (codePoint == '%') ||
                (codePoint == '@') ||
                (codePoint == '?')
                ;
    }

    public static boolean startPort(int portIndex, int baud, boolean onlyAllowedChars) {
        activePort = ports[portIndex];

        if (!activePort.openPort()) {
            return false;
        }

        activePort.setParity(SerialPort.NO_PARITY);
        activePort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        activePort.setNumDataBits(8);
        activePort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        activePort.clearRTS();
        activePort.clearDTR();

        activePort.addDataListener(new SerialPortDataListener() {

            @Override
            public void serialEvent(SerialPortEvent event) {
                int size = event.getSerialPort().bytesAvailable();
                byte[] buffer = new byte[size];
                event.getSerialPort().readBytes(buffer, size);

                String incomingText = "";
                for(byte b:buffer) {
                    if(onlyAllowedChars) {
                        if (isAllowedChar((char) b)) {
                            System.out.print((char) b);
                            incomingText = incomingText + ((char) b);
                        } else {
                            if (((char) b) != '\r') {
                                System.out.print("[IGNORED]");
                            }
                        }
                    }
                    else {
                        if (((char) b) != '\r') {
                            System.out.print((char) b);
                            incomingText = incomingText + ((char) b);
                        }
                    }
                }
                Log.d(incomingText);
            }

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }
        });
        activePort.setBaudRate(baud);

        return true;
    }

    public static void endPort() {
        if(activePort != null) activePort.closePort();
    }

    public static void fleaTag() {
        showAllPorts();
        Log.dNoLog("--- AUTO CONNECTING TO PROGRAMMING BOARD");
        int i = 0;
        for(SerialPort port : ports) {
            if(port.getPortDescription().contains("CP2102N") || port.getDescriptivePortName().contains("CP210")) {
                Log.dNoLog("--- USING " + i + ": " + port.getDescriptivePortName() + " / " + port.getPortDescription());
                if(startPort(i, 57600, true)) {
                    Log.dNoLog("--- SUCCESSFULLY CONNECTED!");
                    Log.dNoLog("--- ATTACH FLEATAG TO CLAMP (MIND ORIENTATION), THEN PRESS DOWNLOAD BUTTON FOR A FEW SECONDS UNTIL FLEATAG STARTS TO BLINK");
                    Log.dNoLog("--- NOTE: DOWNLOAD BUTTON WILL NOT WORK IF TAG IS DISCHARGED (WAIT A BIT UNTIL CHARGED)");
                    Log.dNoLog("--- NOTE: PRESS '9' PLUS ENTER TO QUIT AND STORE DATA");
                    Log.dNoLog("--- ... WAITING FOR TAG DATA ...");

                    while(true) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                        int j = 0;
                        try {
                            j = Integer.parseInt(br.readLine());
                        } catch(Exception e) { }
                        if(j == 9) { break; }
                    }

                    endPort();
                    Log.dNoLog("--- PORT CLOSED!");

                    return;
                }
                else {
                    endPort();
                    Log.dNoLog("--- ERROR, COULD NOT CONNECT, TRY RE-PLUG USB");
                    return;
                }
            }
            i++;
        }
        Log.dNoLog("--- ERROR, PROGRAMMINGBOARD NOT FOUND, ARE YOU SURE IT IS CONNECTED?");
    }

    public static void genericSerialMonitor(int baud, boolean onlyAllowedChars, boolean inputActivated) {
        int selectedPort = showAndSelectPort();
        if(selectedPort >= 0) {
            if(startPort(selectedPort, baud, onlyAllowedChars)) {
                Log.dNoLog("--- SUCCESSFULLY CONNECTED! LISTENING NOW! PRESS '9' PLUS ENTER TO QUIT AND STORE DATA.");

                while(true) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    String line = "";
                    try {
                        line = br.readLine();
                    } catch(Exception e) { }
                    if(line.equals("9")) { break; }
                    else {
                        if(line.length() > 0) {
                            if(inputActivated) {
                                line = line + "\n\r";
                                activePort.writeBytes(line.getBytes(), line.length());
                            }
                        }
                    }
                }

                endPort();
                Log.dNoLog("--- PORT CLOSED!");
            }
            else {
                endPort();
                Log.dNoLog("--- ERROR, COULD NOT CONNECT, TRY RE-PLUG USB");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Log.dNoLog("-------------------------------------------------");
        Log.dNoLog("--- WILD LAB SERIAL MONITOR V"+SOFTWARE_VERSION+" ---");
        Log.dNoLog("-------------------------------------------------");

        deleteEmptyTxtFiles();
        while(true) {
            int mode = modeSelect();
            if (mode == 0) {
                Log.init("FleaTagData.txt");
                fleaTag();
            } else if (mode == 1) {
                Log.init("TinyFoxDebugging.txt");
                genericSerialMonitor(9600, true, false);
            } else if(mode == 7) {
                Log.init("SerialMonitorLog9600.txt");
                genericSerialMonitor(9600, true, true);
            } else if(mode == 8) {
                Log.init("SerialMonitorLog115200.txt");
                genericSerialMonitor(115200, true, true);
            } else {
                break;
            }
            Log.dNoLog("-------------------------------------------------");
            Log.deinit();
        }
        Log.deinit();
        Log.dNoLog("-------------------------------------------------");
        Log.dNoLog("------------------- FINISHED --------------------");
        Log.dNoLog("-------------------------------------------------");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

}
