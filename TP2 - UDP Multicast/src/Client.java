import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by up201304205 on 01-03-2016.
 */
public class Client {
    public static void main(String[] args) throws IOException {
        /* Validate args */

        if (!(args.length == 4 || args.length == 5)) {
            printUsage();
            return;
        }
        if (!(args[0].equals("lookup")) || args[0].equals("register")) {
            printUsage();
            return;
        }

        int port = Integer.parseInt(args[1]);
        MulticastSocket socket = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName(args[0]);
        socket.joinGroup(group);
        String message = "";
        message += (args[2] + " ");
        message += args[3];

        if(args.length == 5)
            message += (" " + args[4]);

        DatagramPacket data = new DatagramPacket(message.getBytes(), 0, message.length(), group, port);
        socket.send(data);
        socket.close();
    }

    private static void printUsage() {
        System.out.println("Usage: <mcast_addr> <mcast_port> <oper> <opnds> ");
        System.out.println("<mcast_addr> is the IP address of the multicast group used by the server to advertise its service;\n" +
                "<mcast_port> is the port number of the multicast group used by the server to advertise its service;\n" +
                "<oper> is ''register'' or ''lookup'', depending on the operation to invoke;\n" +
                "<opnd> * is the list of operands of the specified operation:\n" +
                "<plate number> <owner name>, for register;\n" +
                "<plate number>, for lookup.");
    }
}
