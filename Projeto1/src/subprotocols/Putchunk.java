package subprotocols;

import general.Logger;
import general.MalformedMessageException;
import general.SubProtocol;

import java.util.Arrays;

public class Putchunk extends SubProtocol {
    private float version;
    private int senderId;
    private String fileId;
    private int chunkNo;
    private int replicationDeg;
    private byte[] chunk;

    public boolean messageOwner(String id) {
        return id.equalsIgnoreCase("PUTCHUNK");
    }

    public void validateArgs(String[] args) throws MalformedMessageException {
        if (args.length != 6)
            throw new MalformedMessageException("Wrong number of arguments for the PUTCHUNK protocol: 6 arguments must be present");

        /* Validate version */
        if(args[0].length() != 3 ||
                !Character.isDigit(args[0].charAt(0)) || args[0].charAt(1) != '.' ||
                !Character.isDigit(args[0].charAt(2)))
            throw new MalformedMessageException("Version must follow the following format: <n>.<m>");
        else {
            try {
                version = Float.parseFloat(args[0]);
            } catch (NumberFormatException e) {
                throw new MalformedMessageException("Version must be a float");
            }
        }

        /* Validate sender ID */
        try {
            senderId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e){
            throw new MalformedMessageException("Sender ID must be an Integer");
        }

        /* Validate file ID */
        if (args[2].length() != 64){
            throw new MalformedMessageException("File ID must be hashed with the SHA-256 cryptographic function");
        }
        for(int i = 0; i < args[2].length(); ++i){
            if (Character.digit(args[2].charAt(i), 16) == -1) {
                throw new MalformedMessageException("File ID must be hashed with the SHA-256 cryptographic function");
            }
        }
        fileId = args[2];

        /* Validate chunk number */
        if (args[3].length() > 6)
            throw new MalformedMessageException("Chunk Number must be smaller than 1000000");
        try {
            chunkNo = Integer.parseInt(args[3]);
        } catch (NumberFormatException e){
            throw new MalformedMessageException("Chunk Number must be an Integer");
        }

        /* Validate replication degree */
        if (args[4].length() != 1)
            throw new MalformedMessageException("Replication Degree must be a single digit");
        try {
            replicationDeg = Integer.parseInt(args[4]);
        } catch (NumberFormatException e){
            throw new MalformedMessageException("Replication Degree must be an Integer");
        }

        /* Validate the body */
        if (args[5].length() < 2){
            throw new MalformedMessageException("The PUTCHUNK protocol body must be preceded of two <CRLF> sequences");
        }
        if (args[5].charAt(0) != '\r' || args[6].charAt(1) != '\n' || args[6].charAt(2) != '\r' || args[6].charAt(3) != '\n') {
            throw new MalformedMessageException("The PUTCHUNK protocol body must be preceded of two <CRLF> sequences");
        }
        chunk = args[5].substring(2).getBytes();
    }

    public void execute(String[] args) {
        Logger.getInstance().printLog(this.toString());
    }

    public String toString() {
        return "PUTCHUNK - Version: " + version + " Sender ID: " + senderId + " File ID: " + fileId + " Chunk No " +
                chunkNo + " Replication Deg: " + replicationDeg + "\nBody: " + Arrays.toString(chunk);

    }
}
