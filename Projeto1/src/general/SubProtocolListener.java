package general;

import java.util.Observer;

/**
 * Created by afonso on 26-03-2016.
 */
public abstract class SubProtocolListener implements Observer {
    protected String localId;
    protected MulticastChannelManager mcm;

    public MulticastChannelManager getMcm() {
        return mcm;
    }

    public void setMcm(MulticastChannelManager mcm) {
        this.mcm = mcm;
    }

    public SubProtocolListener(String localId, MulticastChannelManager mcm) {
        this.localId = localId;
        this.mcm = mcm;

        mcm.addObserver(this);
    }
    public String getLocalId() {
        return localId;
    }
    public void setLocalId(String localId) {
        this.localId = localId;
    }
}
