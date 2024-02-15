package com.mpi.wildlab.serialmonitor;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.*;
import java.util.*;

public class Main {
    public static String SOFTWARE_VERSION = "1.1";
    static SerialPort activePort;
    static SerialPort[] ports = SerialPort.getCommPorts();
    public static String EXIT_STRING = "exit";

    public static int modeSelect() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Log.dNoLog("--- SELECT YOUR TAG:");
        Log.dNoLog("\t[ 0 ] DOWNLOAD DATA FROM FLEATAG (PLEASE CONNECT PROGRAMMINGBOARD VIA USB FIRST)");
        Log.dNoLog("\t[ 1 ] TINYFOX DEBUGGING");
        Log.dNoLog("\t[ 2 ] WILDFI GATEWAY SERIAL MONITOR");
        Log.dNoLog("\t[ 3 ] WILDFI TAG SERIAL MONITOR");
        Log.dNoLog("\t[ 4 ] TICKTAG SERIAL MONITOR");
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
                (codePoint == '>') ||
                (codePoint == '<') ||
                (codePoint == '|') ||
                (codePoint == ';') ||
                (codePoint == '\'') ||
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

    public static void wildFiGatewayCommandList() {
        Log.dNoLog("--- COMMAND LIST:");
        Log.dNoLog("----- GATEWAY_FOR_MOVEMENT_LOGGER:");
        Log.dNoLog("------- n: COMMAND_BYTE_NOTHING");
        Log.dNoLog("------- f: COMMAND_BYTE_FORCE_TRACKING");
        Log.dNoLog("------- a: COMMAND_BYTE_ACTIVATE");
        Log.dNoLog("------- d: COMMAND_BYTE_DEACTIVATE");
        Log.dNoLog("------- c: COMMAND_BYTE_CHANGE_CONFIG");
        Log.dNoLog("------- m: COMMAND_BYTE_MAG_CALIBRATION");
        Log.dNoLog("------- s: COMMAND_BYTE_DO_NOT_SEND");
        Log.dNoLog("------- t: COMMAND_BYTE_TIME_RESYNC");
        Log.dNoLog("------- w: COMMAND_BYTE_TIME_SYNC_ACTIVATION");
        Log.dNoLog("------- x: COMMAND_BYTE_ACTIVATE_WHEN_NO_GW");
        Log.dNoLog("----- GATEWAY_FOR_PROXIMITY_DETECTION:");
        Log.dNoLog("------- n: PROXIMITY_COMMAND_NOTHING");
        Log.dNoLog("------- s: PROXIMITY_COMMAND_DO_NOT_SEND");
        Log.dNoLog("------- a: PROXIMITY_COMMAND_ACTIVATE");
        Log.dNoLog("------- d: PROXIMITY_COMMAND_DEACTIVATE");
        Log.dNoLog("------- c: PROXIMITY_COMMAND_CHANGE_CONFIG");
        Log.dNoLog("------- r: PROXIMITY_COMMAND_FULL_RESET");
        Log.dNoLog("------- m: PROXIMITY_COMMAND_MAG_CALIB");
        Log.dNoLog("------- w: PROXIMITY_COMMAND_RESYNC_TIME_BY_WIFI");
        Log.dNoLog("------- 2: PROXIMITY_COMMAND_ACTIVATE_AT_06_00");
        Log.dNoLog("------- 3: PROXIMITY_COMMAND_ACTIVATE_AT_12_00");
        Log.dNoLog("------- 4: PROXIMITY_COMMAND_ACTIVATE_AT_15_00");
        Log.dNoLog("------- 5: PROXIMITY_COMMAND_ACTIVATE_AT_20_00");
        Log.dNoLog("------- f: PROXIMITY_COMMAND_FIRST_SYNC_TIME_IN_ACTIVATION");
        Log.dNoLog("----- GENERAL GATEWAY COMMANDS:");
        Log.dNoLog("------- 0: RESET GATEWAY");
        Log.dNoLog("------- q: STOP GATEWAY");
        Log.dNoLog("------- 1: AUTO COMMAND CHANGE TO NOTHING");
        Log.dNoLog("------- 9: SILENT MODE");
    }

    public static void wildFiGateway() {
        showAllPorts();
        Log.dNoLog("--- AUTO CONNECTING TO WILDFI GATEWAY");
        int i = 0;
        for(SerialPort port : ports) {
            if(port.getDescriptivePortName().contains("CH340")) {
                Log.dNoLog("--- USING " + i + ": " + port.getDescriptivePortName() + " / " + port.getPortDescription());
                if(startPort(i, 115200, true)) {
                    Log.dNoLog("--- SUCCESSFULLY CONNECTED!");
                    Log.dNoLog("--- TYPE 'b' PLUS ENTER TO SHOW COMMAND MENUE, TYPE '" + EXIT_STRING + "' PLUS ENTER TO QUIT AND STORE DATA");

                    while(true) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                        String line = "";
                        try {
                            line = br.readLine();
                        } catch(Exception e) { }
                        if(line.equals(EXIT_STRING)) { break; }
                        else if(line.equals("b")) {
                            wildFiGatewayCommandList();
                        }
                        else {
                            if(line.length() > 0) {
                                //line = line + "\n\r";
                                activePort.writeBytes(line.getBytes(), line.length());
                            }
                        }
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
        Log.dNoLog("--- ERROR, WILDFI GATEWAY (CH340), ARE YOU SURE IT IS CONNECTED?");
    }

    public static void wildFi() {
        showAllPorts();
        Log.dNoLog("--- AUTO CONNECTING TO WILDFI TAG");
        int i = 0;
        for(SerialPort port : ports) {
            if(port.getPortDescription().contains("CP2102N") || port.getDescriptivePortName().contains("CP210")) {
                Log.dNoLog("--- USING " + i + ": " + port.getDescriptivePortName() + " / " + port.getPortDescription());
                if(startPort(i, 115200, true)) {
                    Log.dNoLog("--- SUCCESSFULLY CONNECTED!");
                    Log.dNoLog("--- TYPE '" + EXIT_STRING + "' PLUS ENTER TO QUIT AND STORE DATA");

                    while(true) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                        String line = "";
                        try {
                            line = br.readLine();
                        } catch(Exception e) { }
                        if(line.equals(EXIT_STRING)) { break; }
                        else if(line.equals("w")) {
                            if(line.length() > 0) {
                                //line = line + "\n\r";
                                activePort.writeBytes(line.getBytes(), line.length());
                            }
                        }
                        else {
                            if(line.length() > 0) {
                                line = line + "\n\r";
                                activePort.writeBytes(line.getBytes(), line.length());
                            }
                        }
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
        Log.dNoLog("--- ERROR, WILDFI GATEWAY (CH340), ARE YOU SURE IT IS CONNECTED?");
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
                    Log.dNoLog("--- NOTE: TYPE '" + EXIT_STRING + "' PLUS ENTER TO QUIT AND STORE DATA");
                    Log.dNoLog("--- ... WAITING FOR TAG DATA ...");

                    while(true) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                        String line = "";
                        try {
                            line = br.readLine();
                        } catch(Exception e) { }
                        if(line.equals(EXIT_STRING)) { break; }
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

    public static void tickTag() {
        showAllPorts();
        Log.dNoLog("--- AUTO CONNECTING TO PROGRAMMING BOARD");
        int i = 0;
        for(SerialPort port : ports) {
            if(port.getPortDescription().contains("CP2102N") || port.getDescriptivePortName().contains("CP210")) {
                Log.dNoLog("--- USING " + i + ": " + port.getDescriptivePortName() + " / " + port.getPortDescription());
                if(startPort(i, 9600, true)) {
                    Log.dNoLog("--- SUCCESSFULLY CONNECTED!");
                    //Log.dNoLog("--- ATTACH FLEATAG TO CLAMP (MIND ORIENTATION), THEN PRESS DOWNLOAD BUTTON FOR A FEW SECONDS UNTIL FLEATAG STARTS TO BLINK");
                    //Log.dNoLog("--- NOTE: DOWNLOAD BUTTON WILL NOT WORK IF TAG IS DISCHARGED (WAIT A BIT UNTIL CHARGED)");
                    Log.dNoLog("--- NOTE: TYPE '" + EXIT_STRING + "' PLUS ENTER TO QUIT AND STORE DATA");
                    Log.dNoLog("--- ... WAITING FOR TAG DATA (PRESS BUTTON ON UIB TO ENTER MENUE) ...");

                    while(true) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                        String line = "";
                        try {
                            line = br.readLine();
                        } catch(Exception e) { }
                        if(line.equals(EXIT_STRING)) { break; }
                        else {
                            if(line.length() > 0) {
                                line = line + "\n\r";
                                activePort.writeBytes(line.getBytes(), line.length());
                            }
                        }
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
        boolean doLineBreak = true;
        if(selectedPort >= 0) {
            if(startPort(selectedPort, baud, onlyAllowedChars)) {
                Log.dNoLog("--- SUCCESSFULLY CONNECTED! LISTENING NOW! TYPE '" + EXIT_STRING + "' PLUS ENTER TO QUIT AND STORE DATA.");
                Log.dNoLog("--- TYPE 'lb' PLUS ENTER TO TOGGLE APPENDING LINE BREAKS");

                while(true) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    String line = "";
                    try {
                        line = br.readLine();
                    } catch(Exception e) { }
                    if(line.equals(EXIT_STRING)) { break; }
                    if(line.equals("lb")) {
                        doLineBreak = !doLineBreak;
                        Log.dNoLog("--- LINE BREAKS: " + doLineBreak);
                    }
                    else {
                        if(line.length() > 0) {
                            if(inputActivated) {
                                if(doLineBreak) line = line + "\n\r";
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
            } else if (mode == 2) {
                Log.init("WildFiGatewayLog.txt");
                wildFiGateway();
            } else if (mode == 3) {
                Log.init("WildFiTagLog.txt");
                wildFi();
            } else if (mode == 4) {
                Log.init("TickTagLog.txt");
                tickTag();
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
