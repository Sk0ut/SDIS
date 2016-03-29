package general;

import java.io.IOException;

/**
 * Created by afonso on 26-03-2016.
 */
public abstract class SubProtocolListener {
    private String localId;

    public SubProtocolListener(String localId) {
        this.localId = localId;
    }

    public abstract void processMessage(byte[] args) throws MalformedMessageException, IOException;

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }
}
