package com.endpoints;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by up201304205 on 23-02-2016.
 */
public class Server {
    public static final int USAGE = 0;
    public static final int SOCKETOPENERROR = 1;
    public static final int SOCKETREADERROR = 2;
    public static final int PLATEALREADYEXISTSERROR = 3;
    public static final int DATABASELENGTH = 4;
    public static final int PLATENOTFOUND = 5;
    public static final int PLATEFOUND = 6;
    public static final int COMMANDUNKNOWN = -2;

    public static final int MAX_PACKET_SIZE = 274;
    public static final String NOT_FOUND = "NOT_FOUND";

    private Map<String, String> plates = new HashMap<>(); /* Plate, owner */

    public void main(String[] args){
        int port;
        DatagramSocket socket;

        if(args.length != 2)
            printLog(USAGE, "");

        port = Integer.parseInt(args[1]);
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            printLog(SOCKETOPENERROR, args[1]);
            return;
        }

        byte[] buf = new byte[MAX_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, MAX_PACKET_SIZE);

        while(true){
            try {
                socket.receive(packet);
                socket.send(new DatagramPacket(parsePacketInfo(packet), packet.getLength()));
            } catch (IOException e) {
                printLog(SOCKETREADERROR, args[1]);
                return;
            }
        }
    }

    private byte[] parsePacketInfo(DatagramPacket packet) {
        String message = new String(packet.getData());
        byte[] ret;
        String[] args = message.split(" ");

        if((args[0].equals("REGISTER") || args[0].equals("Register") || args[0].equals("register")) &&
                args.length == 3){
            Integer registerResult = registerPlate(args[1], args[2]);
            if(registerResult == -1)
                printLog(PLATEALREADYEXISTSERROR, registerResult.toString());
            else
                printLog(DATABASELENGTH, registerResult.toString());
            return new byte[]{(byte)registerResult.intValue()};
        }
        else if(args[0].equals("LOOKUP") || args[0].equals("Lookup") || args[0].equals("lookup") &&
                args.length == 2){
            String lookupResult = lookupPlate(args[1]);
            if(lookupResult.equals(NOT_FOUND))
                printLog(PLATENOTFOUND, args[1]);
            else
                printLog(PLATEFOUND, lookupResult);
            return lookupResult.getBytes();
        }
        else {
            printLog(COMMANDUNKNOWN, message);
            return new byte[]{(byte)COMMANDUNKNOWN};
        }
    }

    private String lookupPlate(String plate) {
        if(plates.containsKey(plate))
            return plates.get(plate);
        else return NOT_FOUND;
    }

    private int registerPlate(String plate, String owner) {
        if(plates.containsKey(plate))
            return -1;
        else{
            plates.put(plate, owner);
            return plates.size();
        }
    }

    private void printLog(int number, String arg) {
        switch(number){
            case USAGE:
                System.out.println("Usage: java Server <port number>");
                break;
            case SOCKETOPENERROR:
                System.out.println("Error in opening UDP socket in port " + arg + ".");
                break;
            case SOCKETREADERROR:
                System.out.println("Error in reading from UDP socket in port " + arg + ".");
                break;
            case PLATEALREADYEXISTSERROR:
                System.out.println("The plate " + arg + " already exists in the database.");
                break;
            case DATABASELENGTH:
                System.out.println("Plate read. Database length: " + arg + ".");
                break;
            case PLATENOTFOUND:
                System.out.println("Plate " + arg + " was not found in the database.");
                break;
            case PLATEFOUND:
                System.out.println("Plate was found in the database and belongs to " + arg + ".");
                break;
            case COMMANDUNKNOWN:
                System.out.println("Unrecognized command: " + arg);
                break;
            default:
                System.out.println("An unknown error occured.");
                break;
        }
    }
}
