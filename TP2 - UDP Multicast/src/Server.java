import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by up201304205 on 01-03-2016.
 */
public class Server {
    public static final int SOCKETOPENERROR = 1;
    public static final int SOCKETREADERROR = 2;
    public static final int PLATEALREADYEXISTSERROR = 3;
    public static final int DATABASELENGTH = 4;
    public static final int PLATENOTFOUND = 5;
    public static final int PLATEFOUND = 6;
    public static final int COMMANDUNKNOWN = -1;
    public static final int MAX_PACKET_SIZE = 274;
    public static final String NOT_FOUND = "NOT_FOUND";

    private static Map<String, String> plates = new HashMap<>(); /* Plate, owner */

    public static void main(String[] args) throws IOException{
        int port = Integer.parseInt(args[2]);
        MulticastSocket socket = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName(args[1]);
        socket.joinGroup(group);

        while(true){
            byte[] buf = new byte[1024];
            DatagramPacket data = new DatagramPacket(buf, buf.length);
            socket.receive(data);
            byte[] parsedData = parsePacketInfo(data);
            socket.send(new DatagramPacket(parsedData,parsedData.length));
            String log = Arrays.toString(parsedData);
            System.out.println(new String(log));
        }
    }



    private static byte[] parsePacketInfo(DatagramPacket packet) {
        String message = new String(packet.getData());
        String[] args = message.split(" ");

        if((args[0].equals("REGISTER") || args[0].equals("Register") || args[0].equals("register")) &&
                args.length == 3){
            Integer registerResult = registerPlate(args[1], args[2]);
            if(registerResult == -1)
                printLog(PLATEALREADYEXISTSERROR, registerResult.toString());
            else
                printLog(DATABASELENGTH, registerResult.toString());
            message = registerResult + '\n' + args[1] + ' ' + args[2];
            return message.getBytes();
        }
        else if(args[0].equals("LOOKUP") || args[0].equals("Lookup") || args[0].equals("lookup") &&
                args.length == 2){
            String lookupResult = lookupPlate(args[1]);
            if(lookupResult.equals(NOT_FOUND)) {
                printLog(PLATENOTFOUND, args[1]);
                message = "-1\n" + args[1];
            }
            else {
                printLog(PLATEFOUND, lookupResult);
                message = plates.size() + '\n' + args[1] + ' ' + lookupResult;
            }
            return message.getBytes();
        }
        else {
            printLog(COMMANDUNKNOWN, message);
            return new byte[]{(byte)COMMANDUNKNOWN};
        }
    }

    private static String lookupPlate(String plate) {
        if(plates.containsKey(plate))
            return plates.get(plate);
        else return NOT_FOUND;
    }

    private static int registerPlate(String plate, String owner) {
        if(plates.containsKey(plate))
            return -1;
        else{
            plates.put(plate, owner);
            return plates.size();
        }
    }

    private static void printLog(int number, String arg) {
        switch(number){
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
