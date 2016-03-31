package communication;

import general.MalformedMessageException;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Flávio on 29/03/2016.
 */
public abstract class MessageParser {

    protected List<String[]> header;
    protected byte[] body;


    public abstract Message parse(byte [] messageBytes) throws IOException, MalformedMessageException;
    protected void splitMessage(byte[] messageBytes) throws IOException {
        InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(messageBytes));
        BufferedReader bufferedReader = new BufferedReader(in);
        header = new LinkedList<>();
        String headerLine = bufferedReader.readLine();
        while (!headerLine.isEmpty()){
            header.add(headerLine.trim().replaceAll("\\s+", " ").split(" "));
            headerLine = bufferedReader.readLine();
        }

        char [] buffer = new char [64 * 1024];
        int size = bufferedReader.read(buffer, 0, buffer.length);
        if (size == -1) {
            body = new byte[] {};
        } else {
            body = new byte[size];
            System.arraycopy(new String(buffer).getBytes("UTF-8"), 0, body, 0, body.length);
        }
    }

    public static boolean validVersion(String version) {
        return version.matches("^\\d.\\d$");
    }

    public static boolean validSenderId(String senderId) {
        return senderId.matches("^\\d+$");
    }

    public static boolean validFileId(String fileId) {
        return fileId.matches("^[0-9a-fA-F]{64}$");
    }

    public static boolean validChunkNo(String chunkNo) {
        return chunkNo.matches("^\\d{0,6}$");
    }

    public static boolean validReplicationDeg(String replicationDeg) {
        return replicationDeg.matches("^\\d$");
    }
}
