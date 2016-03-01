import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by up201304205 on 01-03-2016.
 */
public class Server {
    public static void main(String[] args) throws IOException{
        int port = Integer.parseInt(args[2]);
        MulticastSocket socket = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName(args[1]);
        socket.joinGroup(group);

        while(true){
            byte[] buf = new byte[1024];
            DatagramPacket data = new DatagramPacket(buf, buf.length);
            socket.receive(data);
            String msg = new String(data.getData()).trim();
            System.out.println(msg);
        }
    }
}
