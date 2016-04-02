package subprotocol;

/**
 * Created by Afonso on 02/04/2016.
 */
public class ReclaimInitiator {
    private final String localId;
    private final long space;

    public ReclaimInitiator(long space, String localId) {
        this.space = space;
        this.localId = localId;
    }
}
