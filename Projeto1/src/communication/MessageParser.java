package communication;

import general.MalformedMessageException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class MessageParser {

    protected List<String[]> header;
    protected byte[] body;


    public abstract Message parse(byte [] messageBytes) throws IOException, MalformedMessageException;
    protected void splitMessage(byte[] messageBytes) throws IOException {

        header = new LinkedList<>();

        int i = 0;
        for (int j = 0; j < messageBytes.length - 1; ++j) {
            if ((messageBytes[j] == Message.CR) && (messageBytes[j+1] == Message.LF)){
                if (j == i) {
                    i = j + 2;
                    break;
                }
                header.add(new String(Arrays.copyOfRange(messageBytes, i, j), StandardCharsets.US_ASCII)
                        .trim().replaceAll("\\s+", " ").split(" "));
                i = j + 2;
            }
        }

        body = Arrays.copyOfRange(messageBytes, i, messageBytes.length);
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
