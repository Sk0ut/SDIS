package communication;

import java.io.IOException;

/**
 * Created by Flávio on 29/03/2016.
 */
public interface MessageParser {
    public Message parse(byte [] messageBytes) throws IOException;
}
