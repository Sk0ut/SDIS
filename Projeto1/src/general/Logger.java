package general;

import communication.Peer;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by afonso on 26-03-2016.
 */
public class Logger {
    private static Logger instance = null;
    private PrintWriter writer = null;

    private Logger() {
        try {
            new File("logs").mkdir();
            DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
            Date date = new Date();
            String logFileName = "logs/dbs_id" + Peer.senderId + "_" + dateFormat.format(date) + ".log";
            writer = new PrintWriter(logFileName, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getInstance(){
        if (instance == null)
            instance = new Logger();
        return instance;
    }

    public void printLog(String line){
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss - ");
        Date date = new Date();
        String log = dateFormat.format(date) + line +'\n';
        writer.write(log);
        writer.flush();
        System.out.print(log);
    }
}
