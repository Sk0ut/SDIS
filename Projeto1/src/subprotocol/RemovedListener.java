package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.RemovedMessage;
import general.Logger;
import general.MalformedMessageException;
import general.MulticastChannel;
import general.Subprotocol;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by afonso on 26-03-2016.
 */
public class RemovedListener extends Subprotocol implements Observer {
    private static final MessageParser parser = new RemovedMessage.Parser();
    private final MulticastChannel mc;


    public RemovedListener(String localId, MulticastChannel mc) {
        super(localId);
        this.mc = mc;
    }

    public void start() {
        mc.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        Message msg = null;
        try {
            msg = parser.parse((byte[]) arg);
            Logger.getInstance().printLog(msg.getHeader());
        } catch (IOException | MalformedMessageException e) {
            e.printStackTrace();
        }
    }
}
