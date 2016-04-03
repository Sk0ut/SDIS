package communication.message;

import communication.Message;
import communication.MessageParser;
import general.MalformedMessageException;

import java.io.IOException;


public class ChunkMessage extends Message {

    private final static String IDENTIFIER = "CHUNK";
    private final static String VERSION = "1.0";

    public static class Parser extends MessageParser {
        @Override
        public Message parse(byte[] messageBytes) throws IOException, MalformedMessageException {
            splitMessage(messageBytes);

            if (header.size() != 1)
                throw new MalformedMessageException("Wrong number of lines for subprotocol " + IDENTIFIER + " version " + VERSION);

            String[] headerLine = header.get(0);

            if (headerLine.length != 5)
                throw new MalformedMessageException("Wrong number of arguments for the CHUNK message: 4 arguments must be present");

            if (!headerLine[0].equalsIgnoreCase(IDENTIFIER))
                throw new MalformedMessageException("Wrong protocol");

            /* Validate version */
            if (!headerLine[1].equalsIgnoreCase(VERSION))
                throw new MalformedMessageException("Version must follow the following format: <n>.<m>");

            if (!validSenderId(headerLine[2]))
                throw new MalformedMessageException("Sender ID must be an Integer");

            /* Validate file ID */
            if (!validFileId(headerLine[3]))
                throw new MalformedMessageException("Metadata ID must be hashed with the SHA-256 cryptographic function");
            
            /* Validate chunk No */
            if (!validChunkNo(headerLine[4]))
                throw new MalformedMessageException("Chunk Number must be an integer smaller than 1000000");

            return new ChunkMessage(headerLine[2], headerLine[3], headerLine[4], body);
        }
    }

    public ChunkMessage(String senderId, String fileId, String chunkNo, byte[] body) {
        super(IDENTIFIER, VERSION, senderId, fileId, chunkNo, "", body);
    }
}