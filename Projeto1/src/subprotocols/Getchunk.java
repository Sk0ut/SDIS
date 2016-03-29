package subprotocols;

import general.Logger;
import general.MalformedMessageException;
import general.SubProtocol;

/**
 * Created by afonso on 26-03-2016.
 */
public class Getchunk extends SubProtocol {
    private float version;
    private int senderId;
    private String fileId;
    private int chunkNo;

    public boolean messageOwner(String id) {
        return id.equalsIgnoreCase("GETCHUNK");
    }

    public void validateArgs(String[] args) throws MalformedMessageException {
        if (args.length != 4)
            throw new MalformedMessageException("Wrong number of arguments for the GETCHUNK protocol: 4 arguments must be present");

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
    }

    public void execute(String[] args) {
        Logger.getInstance().printLog(this.toString());
    }

    public String toString() {
        return "GETCHUNK - Version: " + version + " Sender ID: " + senderId + " File ID: " + fileId + " Chunk No " + chunkNo;
    }
}
