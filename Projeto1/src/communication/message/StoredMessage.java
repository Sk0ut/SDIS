package communication.message;

import communication.Message;
import communication.MessageParser;

import java.io.*;

/**
 * Created by Flávio on 29/03/2016.
 */
public class StoredMessage extends Message {
    public static class Parser implements MessageParser {
        public Parser() {

        }

        @Override
        public Message parse(byte[] messageBytes) throws IOException {
            InputStream in = new ByteArrayInputStream(messageBytes);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String header = bufferedReader.readLine().trim().replaceAll("\\s+", " ");
            bufferedReader.skip(1);

            byte [] buffer = new byte [64 * 1024];
            int size = in.read(buffer);

            byte [] body;
            if (size == -1) {
                body = new byte[] {};
            } else {
                body = new byte[size];
                System.arraycopy(buffer, 0, body, 0, size);
            }



            return null;
        }
    }

    public StoredMessage(String senderId, String fileId, String chunkNo) {
        super("STORED", "1.0", senderId, fileId, chunkNo, "", new byte[] {});
    }
}
