import java.io.IOException;
import java.net.*;

/**
 * Created by up201304205 on 23-02-2016.
 */
public class Client {
    public static final int MAX_PACKET_SIZE = 274;
    private static final int USAGE = 1;
    private static final int SENDPACKETERROR = 2;
    private static final int SOCKETOPENERROR = 3;
    private static final int SOCKETREADERROR = 4;

    public static void main(String[] args) {
        /* Validate data */
        if(args.length < 4){
            printLog(USAGE, "");
            return;
        }

        /*if(!(
                ((args[3].equals("REGISTER") || args[3].equals("Register") || args[3].equals("register")) && args.length == 6)
                ||
                ((args[3].equals("LOOKUP") || args[3].equals("Lookup") || args[3].equals("lookup")) && args.length == 5))){
            printLog(USAGE, "");
            return;
        }*/

        /* Open socket */
        int port = Integer.parseInt(args[2]);
        DatagramSocket socket;
        InetAddress address;
        try {
            address = InetAddress.getByName(args[1]);
            socket = new DatagramSocket();
        } catch (SocketException | UnknownHostException e) {
            System.out.println(e.getMessage());
            //printLog(SOCKETOPENERROR, args[2]);
            return;
        }

        /* Process args and prepare message */

        String message = args[3] + " " + args[4]; /* OPERATION operand*/
        if (args.length == 6) /* Register */
            message += " "  + args[5]; /* Add the owner in case of register */

        byte[] buf = message.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);

        /* Send message */
        try {
            socket.send(packet);
        } catch (IOException e) {
            printLog(SENDPACKETERROR, message);
            return;
        }

        /* Receive answer and print log accordingly */

        try {
            socket.receive(packet);
            String answer = new String(packet.getData());
            System.out.println(answer);
        } catch (IOException e) {
            printLog(SOCKETREADERROR, args[2]);
        }



    }

    private static void printLog(int number, String arg) {
        switch(number){
            case USAGE:
                System.out.println("java Client <host_name> <port_number> <oper> <opnd>*");
                break;
            case SENDPACKETERROR:
                System.out.println("Error in sending command " + arg + ".");
                break;
            case SOCKETOPENERROR:
                System.out.println("Error in opening UDP socket in port " + arg + ".");
                break;
            case SOCKETREADERROR:
                System.out.println("Error in reading from UDP socket in port " + arg + ".");
                break;

        }
    }
}
