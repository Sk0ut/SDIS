package communication.message;

import communication.Message;
import communication.MessageParser;
import general.MalformedMessageException;

import java.io.*;

/**
 * Created by Flávio on 29/03/2016.
 */
public class GetChunkMessage extends Message {

    private final static String IDENTIFIER = "GETCHUNK";
    private final static String VERSION = "1.0";

    public static class Parser extends MessageParser {
        @Override
        public Message parse(byte[] messageBytes) throws IOException, MalformedMessageException {
            splitMessage(messageBytes);
            if (header.length != 5)
                throw new MalformedMessageException("Wrong number of arguments for the GETCHUNK message: 4 arguments must be present");

            if (!header[0].equalsIgnoreCase(IDENTIFIER))
                throw new MalformedMessageException("Wrong protocol");

            /* Validate version */
            if (!header[1].equalsIgnoreCase(VERSION))
                throw new MalformedMessageException("Version must follow the following format: <n>.<m>");

            if (!validSenderId(header[2]))
                throw new MalformedMessageException("Sender ID must be an Integer");

            /* Validate file ID */
            if (!validFileId(header[3])){
                throw new MalformedMessageException("File ID must be hashed with the SHA-256 cryptographic function");
            }
            
            /* Validate chunk No */
            if (!validChunkNo(header[4])){
                throw new MalformedMessageException("Chunk Number must be an integer smaller than 1000000");
            }

            if (body.length != 0){
                throw new MalformedMessageException("There must be no body present");
            }

            return new GetChunkMessage(header[2], header[3], header[4]);
        }
    }

    public GetChunkMessage(String senderId, String fileId, String chunkNo) {
        super(IDENTIFIER, VERSION, senderId, fileId, chunkNo, "", new byte[] {});
    }
}