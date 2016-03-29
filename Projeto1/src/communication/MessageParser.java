package communication;

import general.MalformedMessageException;

import java.io.*;

/**
 * Created by Flávio on 29/03/2016.
 */
public abstract class MessageParser {

    protected String[] header;
    protected byte[] body;

    /* TODO <CRLF> */
    public abstract Message parse(byte [] messageBytes) throws IOException, MalformedMessageException;
    protected void splitMessage(byte[] messageBytes) throws IOException {
        InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(messageBytes));
        BufferedReader bufferedReader = new BufferedReader(in);
        header = bufferedReader.readLine().trim().replaceAll("\\s+", " ").split(" ");
        bufferedReader.readLine();
        char [] buffer = new char [64 * 1024];
        int size = bufferedReader.read(buffer, 0, buffer.length);
        if (size == -1) {
            body = new byte[] {};
        } else {
            body = new byte[size];
            System.arraycopy(new String(buffer).getBytes("UTF-8"), 0, body, 0, body.length);
        }
    }

    protected boolean validVersion(String version) {
        return version.matches("^\\d.\\d$");
    }

    protected boolean validSenderId(String senderId) {
        return senderId.matches("^\\d+$");
    }

    protected boolean validFileId(String fileId) {
        return fileId.matches("^[0-9a-fA-F]{64}$");
    }

    protected boolean validChunkNo(String chunkNo) {
        return chunkNo.matches("^\\d{0,6}$");
    }

    protected boolean validReplicationDeg(String replicationDeg) {
        return replicationDeg.matches("^\\d$");
    }
}
