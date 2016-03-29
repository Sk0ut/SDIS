package general;

import java.io.IOException;

/**
 * Created by afonso on 26-03-2016.
 */
public abstract class SubProtocolListener {
    public abstract void processMessage(byte[] args) throws MalformedMessageException, IOException;
}
