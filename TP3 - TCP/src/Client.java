import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

/**
 * Created by up201304205 on 23-02-2016.
 */
public class Client {
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

        /* Open socket */
        int port = Integer.parseInt(args[2]);
            Socket socket;
        try {
            socket = new Socket(args[1], port);
        } catch (IOException e) {
            printLog(SOCKETOPENERROR, args[2]);
            return;
        }

        /* Process args and prepare message */

        String message = args[3] + " " + args[4]; /* OPERATION operand*/
        if (args.length == 6) /* Register */
            message += " "  + args[5]; /* Add the owner in case of register */

        /* Send message */
        PrintWriter out;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
            out.flush();
        } catch (IOException e) {
            printLog(SENDPACKETERROR, message);
            return;
        }

        /* Receive answer and print log accordingly */

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String answer = in.readLine();
            System.out.println(answer);
            socket.close();
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
                System.out.println("Error in opening TCP socket in port " + arg + ".");
                break;
            case SOCKETREADERROR:
                System.out.println("Error in reading from TCP socket in port " + arg + ".");
                break;

        }
    }
}
